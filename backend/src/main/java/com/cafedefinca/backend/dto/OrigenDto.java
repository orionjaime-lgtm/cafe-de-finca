package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

/**
 * Origen del café (país, región, variedad) tal como lo recibe el frontend.
 * El stock de cada origen va aparte, en OrigenStockDto (GET /api/origenes/stock).
 */
public record OrigenDto(
		Integer idOrigen,
		String pais,
		String region,
		String variedad,
		Integer alturaMsnm,
		String notasCata,
		BigDecimal stockMinimoAlertaGramos) {
}
