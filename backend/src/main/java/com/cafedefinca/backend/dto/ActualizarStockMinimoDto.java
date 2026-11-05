package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ActualizarStockMinimoDto(
	@NotNull(message = "El stock mínimo es obligatorio.")
	@DecimalMin(value = "0", message = "El stock mínimo no puede ser negativo.")
	BigDecimal stockMinimoGramos) {
}
