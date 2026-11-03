package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resultado de una corrida del generador de pedidos de abonos.
 * "generados" son los pedidos creados; "omitidas" son las suscripciones que no pudieron generar
 * su pedido, con el motivo (por ejemplo, sin stock). Una omitida se reintenta en la próxima corrida.
 */
public record ResultadoGeneracionDto(
		LocalDate fecha,
		List<PedidoGenerado> generados,
		List<SuscripcionOmitida> omitidas) {

	// ---------------------------------------------------------------------
	// UN PEDIDO CREADO: de qué abono salió, a qué ruta quedó asignado y en qué lugar de la visita
	// ---------------------------------------------------------------------
	public record PedidoGenerado(
			Integer suscripcionId,
			Integer pedidoId,
			LocalDate fechaEntrega,
			BigDecimal montoTotal,
			Integer hojaRutaId,
			Integer ordenVisita) {
	}

	// ---------------------------------------------------------------------
	// UN ABONO QUE NO PUDO GENERAR SU PEDIDO
	// ---------------------------------------------------------------------
	public record SuscripcionOmitida(
			Integer suscripcionId,
			String motivo) {
	}
}
