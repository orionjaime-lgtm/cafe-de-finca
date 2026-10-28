package com.cafedefinca.backend.util;

import com.cafedefinca.backend.enums.UnidadPeso;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Única clase donde se hacen conversiones de unidades (regla técnica 1 del README).
 * Todo peso se guarda y se calcula en gramos: esta clase traduce lo que escribe la persona
 * (kilos, libras, envases) a gramos, y los gramos a kilos para mostrarlos.
 */
public final class ConversorUnidades {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------
	public static final BigDecimal GRAMOS_POR_KILO = new BigDecimal("1000");

	// Decimales que se conservan (la base guarda los gramos con 3 decimales: NUMERIC(12,3))
	private static final int DECIMALES_GRAMOS = 3;
	private static final int DECIMALES_KILOS = 3;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR PRIVADO: es una clase utilitaria, no se instancia
	// ---------------------------------------------------------------------
	private ConversorUnidades() {
	}

	// ---------------------------------------------------------------------
	// A GRAMOS: cantidad + unidad (ej: 3 lb -> 1360.777 g)
	// ---------------------------------------------------------------------
	public static BigDecimal aGramos(BigDecimal cantidad, UnidadPeso unidad) {
		if (cantidad == null) {
			throw new SolicitudInvalidaException("Falta indicar la cantidad.");
		}
		if (unidad == null) {
			throw new SolicitudInvalidaException("Falta indicar la unidad (g, kg o lb).");
		}
		if (cantidad.signum() <= 0) {
			throw new SolicitudInvalidaException("La cantidad debe ser mayor que cero.");
		}
		return cantidad.multiply(unidad.getGramos()).setScale(DECIMALES_GRAMOS, RoundingMode.HALF_UP);
	}

	// Igual que el anterior, pero con la unidad como texto tal cual la manda el frontend ("kg", "lb")
	public static BigDecimal aGramos(BigDecimal cantidad, String unidad) {
		return aGramos(cantidad, UnidadPeso.desdeTexto(unidad));
	}

	// ---------------------------------------------------------------------
	// ENVASES A GRAMOS: 4 bolsas de 250 g -> 1000 g
	// El peso del envase sale de la tabla "presentacion" (peso_gramos), no se escribe a mano.
	// ---------------------------------------------------------------------
	public static BigDecimal envasesAGramos(int cantidadEnvases, BigDecimal pesoEnvaseGramos) {
		if (cantidadEnvases <= 0) {
			throw new SolicitudInvalidaException("La cantidad de envases debe ser mayor que cero.");
		}
		if (pesoEnvaseGramos == null || pesoEnvaseGramos.signum() <= 0) {
			throw new SolicitudInvalidaException("El peso del envase debe ser mayor que cero.");
		}
		return pesoEnvaseGramos.multiply(BigDecimal.valueOf(cantidadEnvases)).setScale(DECIMALES_GRAMOS, RoundingMode.HALF_UP);
	}

	// ---------------------------------------------------------------------
	// A KILOS (solo para mostrar). Ejemplo: 2300 g -> 2.300 kg.
	// ---------------------------------------------------------------------
	public static BigDecimal gramosAKilos(BigDecimal gramos) {
		return gramos.divide(GRAMOS_POR_KILO, DECIMALES_KILOS, RoundingMode.HALF_UP);
	}
}
