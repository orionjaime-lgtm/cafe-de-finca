package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MembresiaDto(
		Integer idMembresia,
		Integer clienteId,
		String cliente,
		Integer planId,
		String nombrePlan,
		BigDecimal porcentajeDescuento,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		String estado) {
}
