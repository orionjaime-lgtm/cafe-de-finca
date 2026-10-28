package com.cafedefinca.backend.dto;

/**
 * Los 3 números de arriba del panel de caducidad.
 * Solo cuenta lotes que todavía tienen stock (disponible o reservado).
 */
public record ResumenCaducidadDto(
		// Lotes en estado óptimo (no incluye los que están por vencer)
		long activos,

		// Lotes que caducan dentro de los próximos 7 días
		long porVencer,

		// Lotes cuya fecha de caducidad ya pasó
		long vencidos) {
}
