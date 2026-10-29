package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

public record DetallePedidoDto(
		Integer loteTostadoId,
		Integer presentacionId,
		Integer tipoMoliendaId,
		Integer cantidadEnvases,
		BigDecimal pesoTotalGramos,
		BigDecimal precioUnitario,
		BigDecimal subtotal) {
}
