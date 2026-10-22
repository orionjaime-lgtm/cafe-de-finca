package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

/**
 * Resultado de una consulta agrupada por origen (por ejemplo, gramos en stock o gramos tostados).
 * No sale hacia el frontend: solo viaja del repository al service.
 */
public record TotalPorOrigen(
		Integer idOrigen,
		BigDecimal totalGramos) {
}
