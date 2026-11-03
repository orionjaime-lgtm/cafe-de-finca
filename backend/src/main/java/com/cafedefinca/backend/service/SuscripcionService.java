package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.CambioEstadoSuscripcionDto;
import com.cafedefinca.backend.dto.NuevaSuscripcionDto;
import com.cafedefinca.backend.dto.NuevaSuscripcionItemDto;
import com.cafedefinca.backend.dto.SuscripcionDto;
import com.cafedefinca.backend.dto.SuscripcionItemDto;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.DetalleSuscripcion;
import com.cafedefinca.backend.entity.Presentacion;
import com.cafedefinca.backend.entity.Suscripcion;
import com.cafedefinca.backend.entity.TipoMolienda;
import com.cafedefinca.backend.enums.EstadoSuscripcion;
import com.cafedefinca.backend.enums.FrecuenciaSuscripcion;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.PresentacionRepository;
import com.cafedefinca.backend.repository.SuscripcionRepository;
import com.cafedefinca.backend.repository.TipoMoliendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Abonos recurrentes: alta, listado y cambio de estado (pausar, reactivar o cancelar).
 * Solo los clientes B2B pueden tener un abono. La generación de pedidos vive en GeneradorPedidosService.
 */
@Service
@Transactional(readOnly = true)
public class SuscripcionService {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------
	private static final String TIPO_CLIENTE_ABONO = "B2B";

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final SuscripcionRepository suscripcionRepository;
	private final ClienteRepository clienteRepository;
	private final PresentacionRepository presentacionRepository;
	private final TipoMoliendaRepository tipoMoliendaRepository;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories y el reloj)
	// ---------------------------------------------------------------------
	public SuscripcionService(SuscripcionRepository suscripcionRepository,
			ClienteRepository clienteRepository,
			PresentacionRepository presentacionRepository,
			TipoMoliendaRepository tipoMoliendaRepository,
			Clock reloj) {
		this.suscripcionRepository = suscripcionRepository;
		this.clienteRepository = clienteRepository;
		this.presentacionRepository = presentacionRepository;
		this.tipoMoliendaRepository = tipoMoliendaRepository;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// LISTAR ABONOS
	// ---------------------------------------------------------------------
	public List<SuscripcionDto> listar() {
		return suscripcionRepository.buscarTodasConDetalle().stream().map(this::aDto).toList();
	}

	// ---------------------------------------------------------------------
	// CREAR UN ABONO
	// ---------------------------------------------------------------------

	/**
	 * Valida los datos y guarda el abono ACTIVO con su primera fecha de entrega.
	 * La primera entrega es el primer día de entrega que cae desde la fecha de inicio (o desde hoy,
	 * si la fecha de inicio quedó en el pasado) para no generar pedidos de fechas que ya pasaron.
	 */
	@Transactional
	public SuscripcionDto crear(NuevaSuscripcionDto datos) {
		Cliente cliente = clienteRepository.findById(datos.clienteId())
				.orElseThrow(() -> new SolicitudInvalidaException("No existe el cliente con id " + datos.clienteId() + "."));
		FrecuenciaSuscripcion frecuencia = FrecuenciaSuscripcion.desdeTexto(datos.frecuencia());
		LocalDate hoy = LocalDate.now(reloj);
		LocalDate fechaInicio = datos.fechaInicio() != null ? datos.fechaInicio() : hoy;
		LocalDate primeraEntrega;
		Suscripcion suscripcion;

		// 1. Solo los clientes B2B tienen reparto y abono
		if (!TIPO_CLIENTE_ABONO.equals(cliente.getTipoCliente())) {
			throw new SolicitudInvalidaException("Solo los clientes B2B pueden tener un abono recurrente.");
		}

		// 2. El día de entrega tiene que existir para esa frecuencia
		if (!frecuencia.esDiaValido(datos.diaEntrega())) {
			throw new SolicitudInvalidaException("El día de entrega " + frecuencia + " va de 1 a "
					+ frecuencia.getDiaMaximo() + ", y llegó: " + datos.diaEntrega() + ".");
		}

		// 3. La fecha de fin no puede dejar el abono sin ninguna entrega
		primeraEntrega = frecuencia.primeraFecha(datos.diaEntrega(), fechaInicio.isBefore(hoy) ? hoy : fechaInicio);
		if (datos.fechaFin() != null && datos.fechaFin().isBefore(primeraEntrega)) {
			throw new SolicitudInvalidaException("La fecha de fin es anterior a la primera entrega (" + primeraEntrega + ").");
		}

		// 4. Guardar el abono con sus renglones
		suscripcion = new Suscripcion(cliente, frecuencia, datos.diaEntrega(), fechaInicio, datos.fechaFin(), primeraEntrega);
		for (NuevaSuscripcionItemDto item : datos.items()) {
			Presentacion presentacion = presentacionRepository.findById(item.presentacionId())
					.orElseThrow(() -> new SolicitudInvalidaException(
							"No existe la presentación con id " + item.presentacionId() + "."));
			TipoMolienda molienda = tipoMoliendaRepository.findById(item.tipoMoliendaId())
					.orElseThrow(() -> new SolicitudInvalidaException(
							"No existe el tipo de molienda con id " + item.tipoMoliendaId() + "."));
			suscripcion.agregarRenglon(presentacion, molienda, item.cantidadEnvases(), item.precioUnitario());
		}

		return aDto(suscripcionRepository.save(suscripcion));
	}

	// ---------------------------------------------------------------------
	// CAMBIAR EL ESTADO: PAUSADA (pausar), ACTIVA (reactivar) o CANCELADA (cancelar)
	// ---------------------------------------------------------------------

	/**
	 * Bloquea la suscripción mientras cambia, para no pisarse con una generación de pedidos en curso.
	 * Los cambios no permitidos (por ejemplo, reactivar una cancelada) responden 409.
	 */
	@Transactional
	public SuscripcionDto cambiarEstado(Integer idSuscripcion, CambioEstadoSuscripcionDto datos) {
		Suscripcion suscripcion = suscripcionRepository.buscarPorIdConBloqueo(idSuscripcion)
				.orElseThrow(() -> new SolicitudInvalidaException("No existe la suscripción " + idSuscripcion + "."));
		EstadoSuscripcion destino = EstadoSuscripcion.desdeTexto(datos.estado());

		switch (destino) {
			case PAUSADA -> suscripcion.pausar();
			case ACTIVA -> suscripcion.reactivar(LocalDate.now(reloj));
			case CANCELADA -> suscripcion.cancelar();
		}

		return aDto(suscripcion);
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private SuscripcionDto aDto(Suscripcion suscripcion) {
		List<SuscripcionItemDto> items = suscripcion.getDetalles().stream().map(this::aDto).toList();

		return new SuscripcionDto(
				suscripcion.getIdSuscripcion(),
				suscripcion.getCliente().getIdCliente(),
				suscripcion.getCliente().getNombreRazonSocial(),
				suscripcion.getFrecuencia().name(),
				suscripcion.getDiaEntrega(),
				suscripcion.getFechaInicio(),
				suscripcion.getFechaFin(),
				suscripcion.getProximaFechaGeneracion(),
				suscripcion.getEstado().name(),
				items);
	}

	private SuscripcionItemDto aDto(DetalleSuscripcion detalle) {
		return new SuscripcionItemDto(
				detalle.getPresentacion().getIdPresentacion(),
				detalle.getPresentacion().getNombreEnvase(),
				detalle.getTipoMolienda().getIdTipoMolienda(),
				detalle.getTipoMolienda().getNombreMolienda(),
				detalle.getCantidadEnvases(),
				detalle.getPrecioUnitario());
	}
}
