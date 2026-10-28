package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Orden de tueste tal como la recibe el frontend, junto con el lote tostado que nació de ella.
 * Los pesos van siempre en gramos. La merma la calcula la base.
 */
public record OrdenTuesteDto(
		Integer idOrdenTueste,
		Integer idLoteVerde,
		String codigoLoteVerde,
		String origen,
		Integer idPerfilTueste,
		String perfilTueste,
		LocalDateTime fechaHoraTueste,
		BigDecimal pesoVerdeUsadoGramos,
		BigDecimal pesoTostadoObtenidoGramos,
		BigDecimal mermaRealPct,
		String operadorTostador,
		String estadoOrden,

		// Datos del lote tostado que salió de la orden (null si la orden no tiene lote)
		String codigoLoteTostado,
		LocalDateTime fechaDisponibleVenta,
		LocalDate fechaCaducidad) {
}
