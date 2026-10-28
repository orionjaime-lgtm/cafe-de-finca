package com.cafedefinca.backend.dto;

/**
 * Proveedor de café verde tal como lo recibe el frontend (desplegable de ingreso de lotes).
 */
public record ProveedorDto(
		Integer idProveedor,
		String cuit,
		String razonSocial,
		String paisOrigen,
		String telefonoEmail) {
}
