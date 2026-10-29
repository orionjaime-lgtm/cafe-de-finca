package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FacturaDto(
		Integer idFactura,
		String tipoComprobante,
		String numeroComprobante,
		LocalDate fechaEmision,
		LocalDate fechaVencimiento,
		BigDecimal montoTotal) {
}
