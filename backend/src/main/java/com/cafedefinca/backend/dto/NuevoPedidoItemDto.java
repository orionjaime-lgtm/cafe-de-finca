package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record NuevoPedidoItemDto(
		@NotNull @Positive Integer loteTostadoId,
		@NotNull @Positive Integer presentacionId,
		@NotNull @Positive Integer tipoMoliendaId,
		@NotNull @Positive Integer cantidadEnvases,
		@NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal precioUnitario) {
}
