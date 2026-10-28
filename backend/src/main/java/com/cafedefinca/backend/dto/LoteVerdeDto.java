package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Lote de café verde tal como lo recibe el frontend. Los pesos van siempre en gramos.
 */
public record LoteVerdeDto(
		Integer idLoteVerde,
		String codigoLote,
		Integer idOrigen,
		String origen,
		Integer idProveedor,
		String proveedor,
		LocalDate fechaIngreso,
		Integer cantBolsas,
		BigDecimal pesoInicialGramos,
		BigDecimal stockActualGramos) {
}
