package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

public record PagoAcumuladoDto(Integer pedidoId, BigDecimal montoPagado) {
}
