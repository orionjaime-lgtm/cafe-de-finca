package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ResultadoGeneracionDto;
import com.cafedefinca.backend.dto.ResultadoGeneracionDto.PedidoGenerado;
import com.cafedefinca.backend.dto.ResultadoGeneracionDto.SuscripcionOmitida;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.repository.SuscripcionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Genera los pedidos de todos los abonos a los que les toca (ACTIVOS, con la entrega vencida).
 * Lo usan la tarea programada de todos los días y el endpoint POST /api/suscripciones/generar.
 * Cada abono se procesa por separado: si uno falla (por ejemplo, sin stock) queda registrado como omitido
 * y los demás siguen. Volver a correrlo no duplica nada: un abono ya generado avanzó su fecha y ya no le toca.
 * No tiene transacción propia a propósito: cada abono abre la suya en GeneracionPedidoSuscripcionService.
 */
@Service
public class GeneradorPedidosService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private static final Logger LOG = LoggerFactory.getLogger(GeneradorPedidosService.class);

	// Mensaje para el caso inesperado: el detalle técnico va al registro del servidor, no a quien consulta
	private static final String MOTIVO_ERROR_INESPERADO = "Error inesperado: se reintenta en la próxima corrida.";

	private final SuscripcionRepository suscripcionRepository;
	private final GeneracionPedidoSuscripcionService generacionPorSuscripcion;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el repository, el generador por abono y el reloj)
	// ---------------------------------------------------------------------
	public GeneradorPedidosService(SuscripcionRepository suscripcionRepository,
			GeneracionPedidoSuscripcionService generacionPorSuscripcion,
			Clock reloj) {
		this.suscripcionRepository = suscripcionRepository;
		this.generacionPorSuscripcion = generacionPorSuscripcion;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// GENERAR LOS PEDIDOS DEL DÍA
	// ---------------------------------------------------------------------

	/**
	 * Recorre los abonos pendientes del más atrasado al más nuevo y devuelve qué se generó y qué se omitió.
	 * Una omitida no avanza su fecha, así que se vuelve a intentar en la siguiente corrida.
	 */
	public ResultadoGeneracionDto generar() {
		LocalDate hoy = LocalDate.now(reloj);
		List<PedidoGenerado> generados = new ArrayList<>();
		List<SuscripcionOmitida> omitidas = new ArrayList<>();

		for (Integer idSuscripcion : suscripcionRepository.buscarIdsConPedidoPendiente(hoy)) {
			try {
				generacionPorSuscripcion.generarPedido(idSuscripcion).ifPresent(generados::add);
			} catch (StockInsuficienteException | SolicitudInvalidaException | ConflictoException ex) {
				// Motivos de negocio: el mensaje es seguro de mostrar
				LOG.warn("Abono {} omitido: {}", idSuscripcion, ex.getMessage());
				omitidas.add(new SuscripcionOmitida(idSuscripcion, ex.getMessage()));
			} catch (RuntimeException ex) {
				LOG.error("Error inesperado al generar el pedido del abono {}", idSuscripcion, ex);
				omitidas.add(new SuscripcionOmitida(idSuscripcion, MOTIVO_ERROR_INESPERADO));
			}
		}

		LOG.info("Generación de pedidos de abonos al {}: {} generados, {} omitidos", hoy, generados.size(), omitidas.size());
		return new ResultadoGeneracionDto(hoy, generados, omitidas);
	}
}
