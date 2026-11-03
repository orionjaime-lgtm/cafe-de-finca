package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NuevaMembresiaDto(
		@NotNull @Positive Integer clienteId,
		@NotNull @Positive Integer planId) {
}
