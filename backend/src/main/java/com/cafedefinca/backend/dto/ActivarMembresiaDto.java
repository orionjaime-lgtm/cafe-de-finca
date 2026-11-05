package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ActivarMembresiaDto(
		@NotNull @Positive Integer planId) {
}
