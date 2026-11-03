package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevoPedidoDto;
import com.cafedefinca.backend.dto.NuevoPedidoItemDto;
import com.cafedefinca.backend.dto.PedidoDto;
import com.cafedefinca.backend.dto.ResultadoGeneracionDto.PedidoGenerado;
import com.cafedefinca.backend.entity.CanalVenta;
import com.cafedefinca.backend.entity.DetalleSuscripcion;
import com.cafedefinca.backend.entity.HojaRuta;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.entity.Suscripcion;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.repository.CanalVentaRepository;
import com.cafedefinca.backend.repository.HojaRutaRepository;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import com.cafedefinca.backend.repository.SuscripcionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Genera el pedido de UNA suscripción. Cada llamada corre en su propia transacción: si algo falla
 * (por ejemplo, no hay stock) se deshace todo lo de ESA suscripción y las demás no se enteran.
 * Por eso es una clase aparte de GeneradorPedidosService: la transacción nueva solo funciona
 * al llamar a un método desde otra clase.
 */
@Service
public class GeneracionPedidoSuscripcionService {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------

	// Los abonos son de clientes B2B, así que sus pedidos salen por el canal mayorista
	public static final String CANAL_PEDIDOS_ABONO = "Mayorista (B2B)";

	private static final String ESTADO_VENCIDO = "VENCIDO";
	private static final String ESTADO_AGOTADO = "AGOTADO";

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final SuscripcionRepository suscripcionRepository;
	private final PedidoService pedidoService;
	private final PedidoRepository pedidoRepository;
	private final HojaRutaRepository hojaRutaRepository;
	private final CanalVentaRepository canalVentaRepository;
	private final LoteCafeTostadoRepository loteTostadoRepository;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories, el service de pedidos y el reloj)
	// ---------------------------------------------------------------------
	public GeneracionPedidoSuscripcionService(SuscripcionRepository suscripcionRepository,
			PedidoService pedidoService,
			PedidoRepository pedidoRepository,
			HojaRutaRepository hojaRutaRepository,
			CanalVentaRepository canalVentaRepository,
			LoteCafeTostadoRepository loteTostadoRepository,
			Clock reloj) {
		this.suscripcionRepository = suscripcionRepository;
		this.pedidoService = pedidoService;
		this.pedidoRepository = pedidoRepository;
		this.hojaRutaRepository = hojaRutaRepository;
		this.canalVentaRepository = canalVentaRepository;
		this.loteTostadoRepository = loteTostadoRepository;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// GENERAR EL PEDIDO DE UNA SUSCRIPCIÓN
	// ---------------------------------------------------------------------

	/**
	 * Hace, en una sola transacción:
	 * 1. Bloquea la suscripción y vuelve a comprobar que le toque (así, si otra corrida ya la generó
	 *    o la pausaron mientras tanto, no se duplica nada y devuelve vacío).
	 * 2. Elige los lotes y crea el pedido con PedidoService (descuenta stock y emite la factura B2B).
	 * 3. Lo asigna a la hoja de ruta de la fecha de entrega (la crea si no existe) con su orden de visita.
	 * 4. Avanza la próxima fecha del abono.
	 * Si no hay stock lanza StockInsuficienteException y no queda nada guardado ni se avanza la fecha.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public Optional<PedidoGenerado> generarPedido(Integer idSuscripcion) {
		LocalDateTime ahora = LocalDateTime.now(reloj);
		Suscripcion suscripcion = suscripcionRepository.buscarPorIdConBloqueo(idSuscripcion)
				.orElseThrow(() -> new SolicitudInvalidaException("No existe la suscripción " + idSuscripcion + "."));
		LocalDate fechaEntrega = suscripcion.getProximaFechaGeneracion();
		CanalVenta canal;
		PedidoDto pedido;
		Pedido pedidoGuardado;
		HojaRuta hojaRuta;
		int ordenVisita;

		// 1. ¿Todavía le toca?
		if (!suscripcion.debeGenerarPedido(ahora.toLocalDate())) {
			return Optional.empty();
		}

		// 2. Crear el pedido con los precios pactados, reutilizando el service de pedidos
		canal = canalVentaRepository.findByNombreCanal(CANAL_PEDIDOS_ABONO)
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el canal de venta '" + CANAL_PEDIDOS_ABONO + "'."));
		pedido = pedidoService.crear(new NuevoPedidoDto(
				suscripcion.getCliente().getIdCliente(),
				canal.getIdCanalVenta(),
				elegirLotes(suscripcion, ahora)));

		// 3. Asignarlo a la hoja de ruta de la fecha de entrega
		hojaRuta = obtenerHojaRuta(fechaEntrega);
		ordenVisita = pedidoRepository.buscarUltimoOrdenVisita(hojaRuta.getIdHojaRuta()) + 1;
		pedidoGuardado = pedidoRepository.findById(pedido.idPedido()).orElseThrow();
		pedidoGuardado.asignarASuscripcion(idSuscripcion, fechaEntrega);
		pedidoGuardado.asignarARuta(hojaRuta.getIdHojaRuta(), ordenVisita);

		// 4. Avanzar la próxima fecha. El flush hace que cualquier choque con la base (por ejemplo,
		//    un pedido repetido para esa fecha) falle acá y deshaga todo, no al cerrar la transacción.
		suscripcion.avanzarProximaFecha();
		pedidoRepository.flush();

		return Optional.of(new PedidoGenerado(
				idSuscripcion, pedido.idPedido(), fechaEntrega, pedido.montoTotal(),
				hojaRuta.getIdHojaRuta(), ordenVisita));
	}

	// ---------------------------------------------------------------------
	// ELEGIR EL LOTE DE CADA RENGLÓN (FIFO)
	// ---------------------------------------------------------------------

	/**
	 * Para cada renglón del abono toma el lote vendible más antiguo que tenga stock para TODO el renglón.
	 * Va descontando en memoria lo que ya asignó, para no ofrecer dos veces el mismo café dentro del mismo pedido.
	 * PedidoService vuelve a validar y bloquea los lotes, así que si el stock cambió justo en el medio, falla ahí.
	 */
	private List<NuevoPedidoItemDto> elegirLotes(Suscripcion suscripcion, LocalDateTime ahora) {
		List<LoteCafeTostado> vendibles = loteTostadoRepository.buscarEnInventario().stream()
				.filter(lote -> esVendible(lote, ahora))
				.toList();
		Map<Integer, BigDecimal> asignadoPorLote = new HashMap<>();
		List<NuevoPedidoItemDto> items = new ArrayList<>();

		for (DetalleSuscripcion renglon : suscripcion.getDetalles()) {
			BigDecimal gramos = renglon.getPresentacion().getPesoGramos()
					.multiply(BigDecimal.valueOf(renglon.getCantidadEnvases()));
			LoteCafeTostado lote = vendibles.stream()
					.filter(candidato -> libreDe(candidato, asignadoPorLote).compareTo(gramos) >= 0)
					.findFirst()
					.orElseThrow(() -> new StockInsuficienteException(
							"No hay un lote vendible con " + gramos.stripTrailingZeros().toPlainString() + " g libres para "
									+ renglon.getCantidadEnvases() + " x " + renglon.getPresentacion().getNombreEnvase()
									+ " (" + renglon.getTipoMolienda().getNombreMolienda() + ")."));

			asignadoPorLote.merge(lote.getIdLoteTostado(), gramos, BigDecimal::add);
			items.add(new NuevoPedidoItemDto(
					lote.getIdLoteTostado(),
					renglon.getPresentacion().getIdPresentacion(),
					renglon.getTipoMolienda().getIdTipoMolienda(),
					renglon.getCantidadEnvases(),
					renglon.getPrecioUnitario()));
		}

		return items;
	}

	// ---------------------------------------------------------------------
	// REGLA: ¿SE PUEDE VENDER ESTE LOTE AHORA? (las mismas condiciones que exige PedidoService)
	// Terminó el reposo, tiene stock, no está vencido ni agotado.
	// ---------------------------------------------------------------------
	private boolean esVendible(LoteCafeTostado lote, LocalDateTime ahora) {
		boolean terminoReposo = !lote.getFechaDisponibleVenta().isAfter(ahora);
		boolean tieneStock = lote.getStockDisponibleGramos().signum() > 0;
		boolean noVencido = !lote.getFechaCaducidad().isBefore(ahora.toLocalDate())
				&& !ESTADO_VENCIDO.equals(lote.getEstadoDisponibilidad());
		boolean noAgotado = !ESTADO_AGOTADO.equals(lote.getEstadoDisponibilidad());

		return terminoReposo && tieneStock && noVencido && noAgotado;
	}

	// Gramos que le quedan libres a un lote, descontando lo que ya se asignó en este pedido
	private BigDecimal libreDe(LoteCafeTostado lote, Map<Integer, BigDecimal> asignadoPorLote) {
		return lote.getStockDisponibleGramos()
				.subtract(asignadoPorLote.getOrDefault(lote.getIdLoteTostado(), BigDecimal.ZERO));
	}

	// ---------------------------------------------------------------------
	// HOJA DE RUTA DE LA FECHA: la busca y, si no existe, la crea
	// ---------------------------------------------------------------------
	private HojaRuta obtenerHojaRuta(LocalDate fechaEntrega) {
		hojaRutaRepository.crearSiNoExiste(fechaEntrega);
		return hojaRutaRepository.buscarPorFechaConBloqueo(fechaEntrega)
				.orElseThrow(() -> new IllegalStateException("No se pudo crear la hoja de ruta del " + fechaEntrega + "."));
	}
}
