package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

/**
 * Presentación (envase) tal como la recibe el frontend.
 */
public record PresentacionDto(
		Integer idPresentacion,
		String nombreEnvase,
		BigDecimal pesoGramos) {
}
