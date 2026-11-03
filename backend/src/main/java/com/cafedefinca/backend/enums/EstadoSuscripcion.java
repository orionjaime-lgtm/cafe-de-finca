package com.cafedefinca.backend.enums;

import com.cafedefinca.backend.exception.SolicitudInvalidaException;

import java.util.Locale;

/**
 * Estado de un abono recurrente. Solo las ACTIVAS generan pedidos; una CANCELADA no vuelve a activarse.
 */
public enum EstadoSuscripcion {

	// ---------------------------------------------------------------------
	// ESTADOS
	// ---------------------------------------------------------------------
	ACTIVA,
	PAUSADA,
	CANCELADA;

	// ---------------------------------------------------------------------
	// INTERPRETAR EL TEXTO QUE MANDA EL FRONTEND ("pausada", "ACTIVA"...)
	// ---------------------------------------------------------------------
	public static EstadoSuscripcion desdeTexto(String texto) {
		if (texto == null || texto.isBlank()) {
			throw new SolicitudInvalidaException("Falta indicar el estado (ACTIVA, PAUSADA o CANCELADA).");
		}
		try {
			return valueOf(texto.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new SolicitudInvalidaException("Estado desconocido: '" + texto + "'. Usá ACTIVA, PAUSADA o CANCELADA.");
		}
	}
}
