package com.cafedefinca.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record NuevoPedidoDto(
		@NotNull @Positive Integer clienteId,
		@NotNull @Positive Integer canalVentaId,
		@NotEmpty List<@Valid NuevoPedidoItemDto> items) {
}
