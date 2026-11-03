package com.cafedefinca.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Hoja de ruta (el reparto de un día) tal como la recibe el frontend, con sus paradas en orden de visita.
 */
public record HojaRutaDto(
		Integer idHojaRuta,
		LocalDate fechaReparto,
		String estadoRuta,
		List<Parada> paradas) {

	// ---------------------------------------------------------------------
	// UNA PARADA: un pedido a entregar, con el cliente y su domicilio
	// ---------------------------------------------------------------------
	public record Parada(
			Integer ordenVisita,
			Integer pedidoId,
			Integer clienteId,
			String cliente,
			String domicilio,
			String localidad,
			BigDecimal montoTotal,
			String estadoPreparacion) {
	}
}
