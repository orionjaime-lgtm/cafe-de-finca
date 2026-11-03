package com.cafedefinca.backend.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Abono recurrente tal como lo recibe el frontend.
 * proximaFechaGeneracion es la fecha de entrega del próximo pedido.
 */
public record SuscripcionDto(
		Integer idSuscripcion,
		Integer clienteId,
		String cliente,
		String frecuencia,
		Integer diaEntrega,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		LocalDate proximaFechaGeneracion,
		String estado,
		List<SuscripcionItemDto> items) {
}
