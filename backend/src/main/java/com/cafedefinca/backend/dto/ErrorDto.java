package com.cafedefinca.backend.dto;

/**
 * Respuesta JSON cuando una petición tiene un error (regla técnica 6 del README).
 */
public record ErrorDto(
		int estado,
		String mensaje) {
}
