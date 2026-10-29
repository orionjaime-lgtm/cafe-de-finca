package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDto(
		Integer idPedido,
		Integer clienteId,
		Integer canalVentaId,
		LocalDateTime fechaHoraPedido,
		String estadoPreparacion,
		String estadoCobro,
		BigDecimal montoTotal,
		List<DetallePedidoDto> items,
		FacturaDto factura) {
}
