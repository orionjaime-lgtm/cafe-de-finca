package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobroB2bDto(
		Integer pedidoId,
		Integer clienteId,
		String cliente,
		BigDecimal deuda,
		FacturaDto factura,
		LocalDate vencimiento,
		String estado) {
}
