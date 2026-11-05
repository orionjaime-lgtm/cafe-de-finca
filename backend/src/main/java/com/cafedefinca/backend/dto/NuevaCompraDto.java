package com.cafedefinca.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NuevaCompraDto(
		@NotNull @Valid NuevoPedidoDto pedido,
		@NotNull @Positive Integer medioPagoId) {
}
