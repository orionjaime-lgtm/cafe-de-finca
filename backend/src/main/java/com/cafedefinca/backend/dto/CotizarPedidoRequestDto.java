package com.cafedefinca.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record CotizarPedidoRequestDto(
		@NotNull @Positive Integer clienteId,
		@NotEmpty List<@Valid NuevoPedidoItemDto> items) {
}
