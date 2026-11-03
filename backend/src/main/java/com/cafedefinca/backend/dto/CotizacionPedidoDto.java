package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CotizacionPedidoDto(
		BigDecimal montoTotal,
		BigDecimal porcentajeDescuentoAplicado,
		BigDecimal montoDescuentoAplicado,
		List<AhorroPlanDto> ahorroPorPlan) {
}
