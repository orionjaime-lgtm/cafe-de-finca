package com.cafedefinca.backend.dto;

import com.cafedefinca.backend.util.EnteroEstrictoDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos que manda el frontend para registrar un tueste.
 * El peso verde viaja con su unidad ("kg", "lb", "g") y el backend lo convierte a gramos.
 * El peso tostado se calcula desde la merma esperada del perfil; el campo legacy es opcional y se ignora.
 * La fecha del tueste no se manda: la pone el backend con su reloj.
 */
public record NuevaOrdenTuesteDto(
		@NotNull @JsonDeserialize(using = EnteroEstrictoDeserializer.class) Integer idLoteVerde,
		@NotNull Integer idPerfilTueste,
		@NotNull @Positive BigDecimal pesoVerdeUsado,
		@Positive BigDecimal pesoTostadoObtenido,
		@NotBlank String unidad,
		@Size(max = 100) String operadorTostador) {
}
