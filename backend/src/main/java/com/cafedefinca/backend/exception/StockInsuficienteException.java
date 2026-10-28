package com.cafedefinca.backend.exception;

/**
 * Se lanza cuando no hay stock suficiente para descontar en una operación (venta, salida, etc.).
 * El ManejadorGlobalErrores la convierte en una respuesta 409 (CONFLICT) con un mensaje claro.
 */
public class StockInsuficienteException extends RuntimeException {

	public StockInsuficienteException(String mensaje) {
		super(mensaje);
	}

	public StockInsuficienteException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}