package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record CambioEstadoPreparacionDto(
		@NotBlank String estadoPreparacion) {
}
