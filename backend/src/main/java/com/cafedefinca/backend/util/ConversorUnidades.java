package com.cafedefinca.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Única clase donde se hacen conversiones de unidades (regla técnica 1 del README).
 * Todo peso se guarda en gramos; esta clase solo lo traduce para mostrarlo o para convertir a gramos.
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
		if (gramos == null) {
			return null;
		}
		return gramos.divide(GRAMOS_POR_KILO, DECIMALES_KILOS, RoundingMode.HALF_UP);
	}

	/**
	 * Convierte kilos a gramos. Ejemplo: 2.3 kg -> 2300 g.
	 * Todo lo que se guarda en base de datos debe quedar en gramos.
	 */
	public static BigDecimal aGramos(BigDecimal kilos) {
		if (kilos == null) {
			return null;
		}
		return kilos.multiply(GRAMOS_POR_KILO);
	}

	/**
	 * Convierte kilos a gramos. Sobrecarga por comodidad.
	 */
	public static BigDecimal kilosAGramos(BigDecimal kilos) {
		return aGramos(kilos);
	}
}