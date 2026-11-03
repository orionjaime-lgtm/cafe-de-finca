package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

public record PlanSuscripcionDto(
		Integer idPlan,
		String nombre,
		BigDecimal porcentajeDescuento,
		String descripcion) {
}
