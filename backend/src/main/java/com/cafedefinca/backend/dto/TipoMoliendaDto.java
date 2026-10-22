package com.cafedefinca.backend.dto;

/**
 * Tipo de molienda tal como lo recibe el frontend.
 */
public record TipoMoliendaDto(
		Integer idTipoMolienda,
		String nombreMolienda,
		String tipoCafetera,
		String nivelGranulometria,
		Boolean esGranoEntero) {
}
