package com.cafedefinca.backend.dto;

import com.cafedefinca.backend.enums.EstadoFrescura;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lote de café tostado con su frescura calculada.
 * Lo usan el panel de caducidad y el desplegable "Café disponible" del formulario de pedidos.
 */
public record LoteTostadoDto(
		// Identificación del lote
		Integer idLoteTostado,
		String codigoLote,

		// De dónde viene el café
		String pais,
		String region,
		String variedad,
		String perfilTueste,

		// Fechas
		LocalDateTime fechaTueste,
		LocalDateTime fechaDisponibleVenta,
		LocalDate fechaCaducidad,

		// Frescura calculada al momento de la consulta
		long vidaUtilDias,
		long diasRestantes,
		EstadoFrescura estadoFrescura,

		// Stock en gramos
		BigDecimal stockDisponibleGramos,
		BigDecimal stockReservadoGramos,

		// Estado guardado en la base y si hoy se puede vender
		String estadoDisponibilidad,
		boolean disponibleParaVenta) {
}
