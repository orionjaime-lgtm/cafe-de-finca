package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record NuevoPagoDto(
		@NotNull @Positive Integer pedidoId,
		@NotNull @Positive Integer medioPagoId,
		@NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal monto) {
}
