package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos que manda el frontend para registrar un tueste.
 * Los dos pesos viajan con la misma unidad ("kg", "lb", "g") y el backend los convierte a gramos.
 * La fecha del tueste no se manda: la pone el backend con su reloj.
 */
public record NuevaOrdenTuesteDto(
		@NotNull Integer idLoteVerde,
		@NotNull Integer idPerfilTueste,
		@NotNull @Positive BigDecimal pesoVerdeUsado,
		@NotNull @Positive BigDecimal pesoTostadoObtenido,
		@NotBlank String unidad,
		@Size(max = 100) String operadorTostador) {
}
