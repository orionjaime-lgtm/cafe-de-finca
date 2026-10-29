package com.cafedefinca.backend.dto;

public record CanalVentaDto(
		Integer idCanalVenta,
		String nombreCanal,
		Boolean requiereDatosFiscales,
		Boolean permiteCtaCte) {
}
