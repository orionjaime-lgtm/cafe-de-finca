package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CobroB2cDto(
		Integer pagoId,
		Integer pedidoId,
		Integer clienteId,
		String cliente,
		LocalDateTime fechaHoraPago,
		BigDecimal montoPagado,
		Integer medioPagoId,
		String medioPago) {
}
