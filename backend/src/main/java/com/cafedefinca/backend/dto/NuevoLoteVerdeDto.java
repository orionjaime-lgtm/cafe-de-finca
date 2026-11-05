package com.cafedefinca.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.cafedefinca.backend.util.EnteroEstrictoDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos que manda el frontend para ingresar un lote de café verde.
 * La cantidad viaja con su unidad ("kg", "lb", "g") y el backend la convierte a gramos.
 * Si no se manda fechaIngreso se usa la de hoy.
 *
 * La fecha lleva lenient = FALSE: sin eso, un número suelto como 20261028 se tomaría como una fecha.
 */
public record NuevoLoteVerdeDto(
		@NotNull @JsonDeserialize(using = EnteroEstrictoDeserializer.class) Integer idOrigen,
		@NotNull @JsonDeserialize(using = EnteroEstrictoDeserializer.class) Integer idProveedor,
		@NotBlank @Size(max = 50) String codigoLote,
		@NotNull @Positive BigDecimal cantidad,
		@NotBlank String unidad,
		@NotNull @Positive @JsonDeserialize(using = EnteroEstrictoDeserializer.class) Integer cantBolsas,
		@JsonFormat(lenient = OptBoolean.FALSE) LocalDate fechaIngreso) {
}
