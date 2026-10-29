package com.cafedefinca.backend.dto;

public record MedioPagoDto(
		Integer idMedioPago,
		String nombreMedio,
		Boolean requiereComprobante) {
}
