package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

public record AhorroPlanDto(
		Integer planId,
		String nombrePlan,
		BigDecimal porcentajeDescuento,
		BigDecimal montoAhorro) {
}
