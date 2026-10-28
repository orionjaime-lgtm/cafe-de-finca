package com.cafedefinca.backend.exception;

import java.math.BigDecimal;

/**
 * Se lanza cuando se pide más café del que hay (verde o tostado).
 * El ManejadorGlobalErrores la convierte en una respuesta 409 con un mensaje claro.
 */
public class StockInsuficienteException extends RuntimeException {

	public StockInsuficienteException(String mensaje) {
		super(mensaje);
	}

	public StockInsuficienteException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

	// Mensaje armado con los gramos: "Stock insuficiente: hay 2300 g disponibles y se pidieron 5000 g."
	public StockInsuficienteException(BigDecimal gramosDisponibles, BigDecimal gramosPedidos) {
		super("Stock insuficiente: hay " + gramosDisponibles.stripTrailingZeros().toPlainString()
				+ " g disponibles y se pidieron " + gramosPedidos.stripTrailingZeros().toPlainString() + " g.");
	}
}
