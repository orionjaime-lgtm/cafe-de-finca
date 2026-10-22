package com.cafedefinca.backend.dto;

import com.cafedefinca.backend.enums.EstadoStock;

import java.math.BigDecimal;

/**
 * Una fila de la tabla "Límites de Stock Mínimo": stock de café verde de un origen.
 */
public record OrigenStockDto(
		Integer idOrigen,
		String pais,
		String region,
		String variedad,
		BigDecimal stockActualGramos,
		BigDecimal stockMinimoGramos,
		EstadoStock estado) {
}
