package com.cafedefinca.backend.enums;

import com.cafedefinca.backend.exception.SolicitudInvalidaException;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Unidades de peso que acepta el sistema al recibir cantidades de café.
 * Cada unidad sabe cuántos gramos vale; la cuenta la hace ConversorUnidades.
 */
public enum UnidadPeso {

	// ---------------------------------------------------------------------
	// UNIDADES Y SU VALOR EN GRAMOS
	// ---------------------------------------------------------------------
	G("1"),
	KG("1000"),
	LB("453.59237");

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final BigDecimal gramos;

	UnidadPeso(String gramos) {
		this.gramos = new BigDecimal(gramos);
	}

	// ---------------------------------------------------------------------
	// GETTER
	// ---------------------------------------------------------------------
	public BigDecimal getGramos() {
		return gramos;
	}

	// ---------------------------------------------------------------------
	// INTERPRETAR EL TEXTO QUE MANDA EL FRONTEND ("kg", "Libras", "g"...)
	// ---------------------------------------------------------------------
	public static UnidadPeso desdeTexto(String texto) {
		if (texto == null || texto.isBlank()) {
			throw new SolicitudInvalidaException("Falta indicar la unidad (g, kg o lb).");
		}
		String limpio = texto.trim().toLowerCase(Locale.ROOT);
		return switch (limpio) {
			case "g", "gr", "gramo", "gramos" -> G;
			case "kg", "kilo", "kilos", "kilogramo", "kilogramos" -> KG;
			case "lb", "lbs", "libra", "libras" -> LB;
			default -> throw new SolicitudInvalidaException("Unidad desconocida: '" + texto + "'. Usá g, kg o lb.");
		};
	}
}
