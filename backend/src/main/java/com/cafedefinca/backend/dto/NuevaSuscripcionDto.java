package com.cafedefinca.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cafedefinca.backend.util.EnteroEstrictoDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos que manda el frontend para crear un abono recurrente (solo clientes B2B).
 * frecuencia: SEMANAL o MENSUAL. diaEntrega: 1 a 7 (semanal, 1 = lunes) o 1 a 28 (mensual).
 * Si no se manda fechaInicio se usa la de hoy; fechaFin es opcional.
 * Las fechas llevan lenient = FALSE: un número suelto como 20261028 no se acepta como fecha.
 */
public record NuevaSuscripcionDto(
		@NotNull @Positive Integer clienteId,
		@NotBlank String frecuencia,
		@NotNull @JsonDeserialize(using = EnteroEstrictoDeserializer.class) Integer diaEntrega,
		@JsonFormat(lenient = OptBoolean.FALSE) LocalDate fechaInicio,
		@JsonFormat(lenient = OptBoolean.FALSE) LocalDate fechaFin,
		@NotEmpty List<@Valid NuevaSuscripcionItemDto> items) {
}
