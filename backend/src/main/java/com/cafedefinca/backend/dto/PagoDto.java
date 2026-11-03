package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoDto(
		Integer pagoId,
		Integer pedidoId,
		Integer medioPagoId,
		String medioPago,
		LocalDateTime fechaHoraPago,
		BigDecimal montoPagado,
		String estadoCobro) {
}
