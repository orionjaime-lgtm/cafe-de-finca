package com.cafedefinca.backend.exception;

/**
 * Se lanza cuando el frontend manda un dato inválido (por ejemplo, un tipo de cliente que no existe).
 * El ManejadorGlobalErrores la convierte en una respuesta 400 con un mensaje claro.
 */
public class SolicitudInvalidaException extends RuntimeException {

	public SolicitudInvalidaException(String mensaje) {
		super(mensaje);
	}
}
