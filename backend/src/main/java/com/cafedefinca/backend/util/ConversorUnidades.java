package com.cafedefinca.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Única clase donde se hacen conversiones de unidades (regla técnica 1 del README).
 * Todo peso se guarda en gramos; esta clase solo lo traduce para mostrarlo.
 */
public final class ConversorUnidades {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------
	public static final BigDecimal GRAMOS_POR_KILO = new BigDecimal("1000");

	// Decimales que se conservan al pasar de gramos a kilos (1 g = 0.001 kg)
	private static final int DECIMALES_KILOS = 3;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR PRIVADO: es una clase utilitaria, no se instancia
	// ---------------------------------------------------------------------
	private ConversorUnidades() {
	}

	// ---------------------------------------------------------------------
	// CONVERSIONES
	// ---------------------------------------------------------------------

	/**
	 * Convierte gramos a kilos sin perder precisión. Ejemplo: 2300 g -> 2.300 kg.
	 */
	public static BigDecimal gramosAKilos(BigDecimal gramos) {
		return gramos.divide(GRAMOS_POR_KILO, DECIMALES_KILOS, RoundingMode.HALF_UP);
	}
}
