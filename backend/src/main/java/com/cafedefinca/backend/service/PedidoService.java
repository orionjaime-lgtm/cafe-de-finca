package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.DetallePedidoDto;
import com.cafedefinca.backend.dto.FacturaDto;
import com.cafedefinca.backend.dto.AhorroPlanDto;
import com.cafedefinca.backend.dto.CambioEstadoPreparacionDto;
import com.cafedefinca.backend.dto.CotizacionPedidoDto;
import com.cafedefinca.backend.dto.CotizarPedidoRequestDto;
import com.cafedefinca.backend.dto.NuevoPedidoDto;
import com.cafedefinca.backend.dto.NuevoPedidoItemDto;
import com.cafedefinca.backend.dto.PedidoDto;
import com.cafedefinca.backend.entity.CanalVenta;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.DetallePedido;
import com.cafedefinca.backend.entity.Factura;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.entity.MembresiaCliente;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.entity.PlanMembresia;
import com.cafedefinca.backend.entity.Presentacion;
import com.cafedefinca.backend.entity.TipoMolienda;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.repository.CanalVentaRepository;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.DetallePedidoRepository;
import com.cafedefinca.backend.repository.FacturaRepository;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import com.cafedefinca.backend.repository.MembresiaClienteRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import com.cafedefinca.backend.repository.PlanMembresiaRepository;
import com.cafedefinca.backend.repository.PresentacionRepository;
import com.cafedefinca.backend.repository.TipoMoliendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;

/**
 * Registra pedidos descontando el stock tostado y creando la factura B2B en una transacción.
 */
@Service
@Transactional(readOnly = true)
public class PedidoService {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------
	private static final String TIPO_FACTURA = "Factura A";
	private static final long CLAVE_CANDADO_FACTURAS = 7300002L;
	private static final BigDecimal CIEN = new BigDecimal("100");
	private static final Set<String> ESTADOS_PREPARACION = Set.of(
			"PENDIENTE", "EN_PREPARACION", "LISTO", "ENTREGADO", "CANCELADO");

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final ClienteRepository clienteRepository;
	private final CanalVentaRepository canalVentaRepository;
	private final PresentacionRepository presentacionRepository;
	private final TipoMoliendaRepository tipoMoliendaRepository;
	private final LoteCafeTostadoRepository loteTostadoRepository;
	private final PedidoRepository pedidoRepository;
	private final MembresiaClienteRepository membresiaRepository;
	private final PlanMembresiaRepository planRepository;
	private final DetallePedidoRepository detallePedidoRepository;
	private final FacturaRepository facturaRepository;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR
	// ---------------------------------------------------------------------
	public PedidoService(ClienteRepository clienteRepository,
			CanalVentaRepository canalVentaRepository,
			PresentacionRepository presentacionRepository,
			TipoMoliendaRepository tipoMoliendaRepository,
			LoteCafeTostadoRepository loteTostadoRepository,
			PedidoRepository pedidoRepository,
			MembresiaClienteRepository membresiaRepository,
			PlanMembresiaRepository planRepository,
			DetallePedidoRepository detallePedidoRepository,
			FacturaRepository facturaRepository,
			Clock reloj) {
		this.clienteRepository = clienteRepository;
		this.canalVentaRepository = canalVentaRepository;
		this.presentacionRepository = presentacionRepository;
		this.tipoMoliendaRepository = tipoMoliendaRepository;
		this.loteTostadoRepository = loteTostadoRepository;
		this.pedidoRepository = pedidoRepository;
		this.membresiaRepository = membresiaRepository;
		this.planRepository = planRepository;
		this.detallePedidoRepository = detallePedidoRepository;
		this.facturaRepository = facturaRepository;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// CREAR PEDIDO
	// ---------------------------------------------------------------------
	@Transactional
	public PedidoDto crear(NuevoPedidoDto datos) {
		Cliente cliente = clienteRepository.findById(datos.clienteId())
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el cliente con id " + datos.clienteId() + "."));
		CanalVenta canal = canalVentaRepository.findById(datos.canalVentaId())
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el canal de venta con id " + datos.canalVentaId() + "."));
		LocalDateTime ahora = LocalDateTime.now(reloj);
		LocalDate hoy = ahora.toLocalDate();
		List<ItemPreparado> items = prepararItems(datos.items());
		Map<Integer, BigDecimal> gramosPorLote = agruparGramosPorLote(items);
		Map<Integer, LoteCafeTostado> lotes = bloquearLotes(gramosPorLote.keySet());

		validarLotes(lotes, gramosPorLote, ahora, hoy);

		BigDecimal subtotal = items.stream()
				.map(ItemPreparado::subtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		Descuento descuento = descuentoActual(cliente.getIdCliente(), hoy, subtotal);
		BigDecimal montoTotal = subtotal.subtract(descuento.monto());

		gramosPorLote.forEach((id, gramos) -> lotes.get(id).descontarStockDisponible(gramos));
		Pedido pedido = pedidoRepository.saveAndFlush(new Pedido(
				cliente, canal, ahora, montoTotal, descuento.porcentaje(), descuento.monto()));

		List<DetallePedido> detalles = items.stream()
				.map(item -> new DetallePedido(
						pedido,
						lotes.get(item.datos().loteTostadoId()),
						item.presentacion(),
						item.tipoMolienda(),
						item.datos().cantidadEnvases(),
						item.pesoTotalGramos(),
						item.datos().precioUnitario(),
						item.subtotal()))
				.toList();
		detalles = detallePedidoRepository.saveAll(detalles);

		Factura factura = null;
		if ("B2B".equals(cliente.getTipoCliente())) {
			int plazo = cliente.getPlazoCtaCteDias() != null ? cliente.getPlazoCtaCteDias() : 0;
			factura = facturaRepository.save(new Factura(
					pedido,
					TIPO_FACTURA,
					generarNumeroFactura(),
					hoy,
					hoy.plusDays(plazo),
					montoTotal));
		}

		return aDto(pedido, detalles, factura);
	}

	public List<PedidoDto> listar() {
		return pedidoRepository.buscarTodosConDetalle().stream()
				.map(pedido -> aDto(pedido, pedido.getDetalles(), pedido.getFactura()))
				.toList();
	}

	@Transactional
	public PedidoDto cambiarEstadoPreparacion(Integer idPedido, CambioEstadoPreparacionDto datos) {
		String estado = datos.estadoPreparacion().trim().toUpperCase(Locale.ROOT);
		if (!ESTADOS_PREPARACION.contains(estado)) {
			throw new SolicitudInvalidaException("El estado de preparación no es válido: " + datos.estadoPreparacion() + ".");
		}
		Pedido pedido = pedidoRepository.findById(idPedido)
				.orElseThrow(() -> new SolicitudInvalidaException("No existe el pedido con id " + idPedido + "."));
		String estadoAnterior = pedido.getEstadoPreparacion();
		if ("CANCELADO".equals(estado) && !"CANCELADO".equals(estadoAnterior)) {
			Map<Integer, BigDecimal> gramosPorLote = new TreeMap<>();
			for (DetallePedido detalle : pedido.getDetalles()) {
				gramosPorLote.merge(detalle.getLoteTostado().getIdLoteTostado(), detalle.getPesoTotalGramos(), BigDecimal::add);
			}
			Map<Integer, LoteCafeTostado> lotes = bloquearLotes(gramosPorLote.keySet());
			for (Map.Entry<Integer, BigDecimal> entrada : gramosPorLote.entrySet()) {
				lotes.get(entrada.getKey()).reponerStockDisponible(entrada.getValue());
			}
		}
		pedido.cambiarEstadoPreparacion(estado);
		if ("CANCELADO".equals(estado)) pedido.marcarReembolsado();
		return aDto(pedido, pedido.getDetalles(), pedido.getFactura());
	}

	public CotizacionPedidoDto cotizar(CotizarPedidoRequestDto datos) {
		Cliente cliente = clienteRepository.findById(datos.clienteId())
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el cliente con id " + datos.clienteId() + "."));
		BigDecimal subtotal = prepararItems(datos.items()).stream()
				.map(ItemPreparado::subtotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		LocalDate hoy = LocalDate.now(reloj);
		Descuento descuentoActual = descuentoActual(cliente.getIdCliente(), hoy, subtotal);
		List<AhorroPlanDto> ahorros = planRepository.findAllByActivoTrueOrderByIdPlanAsc().stream()
				.map(plan -> aDto(plan, subtotal))
				.toList();
		return new CotizacionPedidoDto(
				subtotal.subtract(descuentoActual.monto()),
				descuentoActual.porcentaje(),
				descuentoActual.monto(),
				ahorros);
	}

	private Descuento descuentoActual(Integer idCliente, LocalDate hoy, BigDecimal subtotal) {
		return membresiaRepository.buscarActivaPorCliente(idCliente, hoy)
				.map(MembresiaCliente::getPlan)
				.map(plan -> new Descuento(
						plan.getPorcentajeDescuento(),
						calcularDescuento(subtotal, plan.getPorcentajeDescuento())))
				.orElse(new Descuento(BigDecimal.ZERO, BigDecimal.ZERO.setScale(2)));
	}

	private AhorroPlanDto aDto(PlanMembresia plan, BigDecimal subtotal) {
		return new AhorroPlanDto(
				plan.getIdPlan(),
				plan.getNombrePlan(),
				plan.getPorcentajeDescuento(),
				calcularDescuento(subtotal, plan.getPorcentajeDescuento()));
	}

	private BigDecimal calcularDescuento(BigDecimal subtotal, BigDecimal porcentaje) {
		return subtotal.multiply(porcentaje).divide(CIEN, 2, RoundingMode.HALF_UP);
	}

	// ---------------------------------------------------------------------
	// VALIDAR Y PREPARAR ITEMS
	// ---------------------------------------------------------------------
	private List<ItemPreparado> prepararItems(List<NuevoPedidoItemDto> datos) {
		List<ItemPreparado> items = new ArrayList<>();
		for (NuevoPedidoItemDto item : datos) {
			Presentacion presentacion = presentacionRepository.findById(item.presentacionId())
					.orElseThrow(() -> new SolicitudInvalidaException(
							"No existe la presentación con id " + item.presentacionId() + "."));
			TipoMolienda molienda = tipoMoliendaRepository.findById(item.tipoMoliendaId())
					.orElseThrow(() -> new SolicitudInvalidaException(
							"No existe el tipo de molienda con id " + item.tipoMoliendaId() + "."));
			BigDecimal pesoTotal = presentacion.getPesoGramos()
					.multiply(BigDecimal.valueOf(item.cantidadEnvases()));
			BigDecimal subtotal = item.precioUnitario()
					.multiply(BigDecimal.valueOf(item.cantidadEnvases()));
			items.add(new ItemPreparado(item, presentacion, molienda, pesoTotal, subtotal));
		}
		return items;
	}

	private Map<Integer, BigDecimal> agruparGramosPorLote(List<ItemPreparado> items) {
		Map<Integer, BigDecimal> gramosPorLote = new LinkedHashMap<>();
		for (ItemPreparado item : items) {
			gramosPorLote.merge(
					item.datos().loteTostadoId(),
					item.pesoTotalGramos(),
					BigDecimal::add);
		}
		return gramosPorLote;
	}

	private Map<Integer, LoteCafeTostado> bloquearLotes(Collection<Integer> ids) {
		List<Integer> idsOrdenados = new TreeSet<>(ids).stream().toList();
		List<LoteCafeTostado> encontrados = loteTostadoRepository.buscarPorIdsConBloqueo(idsOrdenados);
		Map<Integer, LoteCafeTostado> lotes = new HashMap<>();
		for (LoteCafeTostado lote : encontrados) {
			lotes.put(lote.getIdLoteTostado(), lote);
		}
		for (Integer id : idsOrdenados) {
			if (!lotes.containsKey(id)) {
				throw new SolicitudInvalidaException("No existe el lote tostado con id " + id + ".");
			}
		}
		return lotes;
	}

	private void validarLotes(Map<Integer, LoteCafeTostado> lotes, Map<Integer, BigDecimal> gramosPorLote,
			LocalDateTime ahora, LocalDate hoy) {
		for (Map.Entry<Integer, BigDecimal> entrada : gramosPorLote.entrySet()) {
			LoteCafeTostado lote = lotes.get(entrada.getKey());
			BigDecimal gramosPedidos = entrada.getValue();

			if (lote.getStockDisponibleGramos().compareTo(gramosPedidos) < 0) {
				throw new StockInsuficienteException(lote.getStockDisponibleGramos(), gramosPedidos);
			}
			if ("VENCIDO".equals(lote.getEstadoDisponibilidad())
					|| "AGOTADO".equals(lote.getEstadoDisponibilidad())
					|| lote.getFechaCaducidad().isBefore(hoy)) {
				throw new SolicitudInvalidaException(
						"El lote tostado con id " + entrada.getKey() + " está vencido o agotado.");
			}
			if (lote.getFechaDisponibleVenta().isAfter(ahora)) {
				throw new SolicitudInvalidaException(
						"El lote tostado con id " + entrada.getKey() + " todavía está en reposo.");
			}
		}
	}

	// ---------------------------------------------------------------------
	// NÚMERO DE FACTURA
	// ---------------------------------------------------------------------
	private String generarNumeroFactura() {
		facturaRepository.bloquearGeneracionDeNumero(CLAVE_CANDADO_FACTURAS);
		long siguiente = facturaRepository.buscarNumeroMaximo(TIPO_FACTURA) + 1;
		return String.format(Locale.ROOT, "%04d", siguiente);
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private PedidoDto aDto(Pedido pedido, List<DetallePedido> detalles, Factura factura) {
		List<DetallePedidoDto> items = detalles.stream()
				.map(detalle -> new DetallePedidoDto(
						detalle.getLoteTostado().getIdLoteTostado(),
						detalle.getPresentacion().getIdPresentacion(),
						detalle.getTipoMolienda().getIdTipoMolienda(),
						detalle.getCantidadEnvases(),
						detalle.getPesoTotalGramos(),
						detalle.getPrecioUnitario(),
						detalle.getSubtotal()))
				.toList();
		FacturaDto facturaDto = factura == null ? null : new FacturaDto(
				factura.getIdFactura(),
				factura.getTipoComprobante(),
				factura.getNumeroComprobante(),
				factura.getFechaEmision(),
				factura.getFechaVencimiento(),
				factura.getMontoTotal());
		return new PedidoDto(
				pedido.getIdPedido(),
				pedido.getCliente().getIdCliente(),
				pedido.getCanalVenta().getIdCanalVenta(),
				pedido.getFechaHoraPedido(),
				pedido.getEstadoPreparacion(),
				pedido.getEstadoCobro(),
				pedido.getMontoTotal(),
				pedido.getPorcentajeDescuento(),
				pedido.getMontoDescuento(),
				items,
				facturaDto);
	}

	private record Descuento(BigDecimal porcentaje, BigDecimal monto) {
	}

	private record ItemPreparado(
			NuevoPedidoItemDto datos,
			Presentacion presentacion,
			TipoMolienda tipoMolienda,
			BigDecimal pesoTotalGramos,
			BigDecimal subtotal) {
	}
}
