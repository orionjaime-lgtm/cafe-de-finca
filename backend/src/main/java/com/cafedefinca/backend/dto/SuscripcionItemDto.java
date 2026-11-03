package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

/**
 * Un renglón de un abono tal como lo recibe el frontend.
 */
public record SuscripcionItemDto(
		Integer presentacionId,
		String presentacion,
		Integer tipoMoliendaId,
		String tipoMolienda,
		Integer cantidadEnvases,
		BigDecimal precioUnitario) {
}
