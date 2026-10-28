package com.cafedefinca.backend.dto;

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
 */
public record NuevoLoteVerdeDto(
		@NotNull Integer idOrigen,
		@NotNull Integer idProveedor,
		@NotBlank @Size(max = 50) String codigoLote,
		@NotNull @Positive BigDecimal cantidad,
		@NotBlank String unidad,
		@NotNull @Positive Integer cantBolsas,
		LocalDate fechaIngreso) {
}
