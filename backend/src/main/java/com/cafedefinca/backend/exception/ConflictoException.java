package com.cafedefinca.backend.exception;

/**
 * Se lanza cuando lo que se pide choca con algo que ya existe (por ejemplo, un código de lote repetido).
 * El ManejadorGlobalErrores la convierte en una respuesta 409 con un mensaje claro.
 */
public class ConflictoException extends RuntimeException {

	public ConflictoException(String mensaje) {
		super(mensaje);
	}
}
