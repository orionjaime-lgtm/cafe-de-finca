package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

/**
 * Cliente tal como lo recibe el frontend (desplegable del formulario de pedidos).
 */
public record ClienteDto(
		Integer idCliente,
		String tipoCliente,
		String nombreRazonSocial,
		String cuitODni,
		String condicionIva,
		Integer plazoCtaCteDias,
		BigDecimal limiteCredito) {
}
