package com.cafedefinca.backend.enums;

import com.cafedefinca.backend.exception.SolicitudInvalidaException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.Locale;

/**
 * Cada cuánto genera un pedido un abono recurrente.
 * Es el único lugar donde se calcula la fecha de la próxima entrega.
 *
 * - SEMANAL: dia_entrega va de 1 (lunes) a 7 (domingo).
 * - MENSUAL: dia_entrega va de 1 a 28 (así existe en todos los meses, febrero incluido).
 */
public enum FrecuenciaSuscripcion {

	// ---------------------------------------------------------------------
	// FRECUENCIAS Y SU DÍA MÁXIMO
	// ---------------------------------------------------------------------
	SEMANAL(7),
	MENSUAL(28);

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------
	private static final int DIAS_ENTRE_ENTREGAS_SEMANALES = 7;

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final int diaMaximo;

	FrecuenciaSuscripcion(int diaMaximo) {
		this.diaMaximo = diaMaximo;
	}

	// ---------------------------------------------------------------------
	// GETTER
	// ---------------------------------------------------------------------
	public int getDiaMaximo() {
		return diaMaximo;
	}

	// ---------------------------------------------------------------------
	// INTERPRETAR EL TEXTO QUE MANDA EL FRONTEND ("semanal", "MENSUAL"...)
	// ---------------------------------------------------------------------
	public static FrecuenciaSuscripcion desdeTexto(String texto) {
		if (texto == null || texto.isBlank()) {
			throw new SolicitudInvalidaException("Falta indicar la frecuencia (SEMANAL o MENSUAL).");
		}
		try {
			return valueOf(texto.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			throw new SolicitudInvalidaException("Frecuencia desconocida: '" + texto + "'. Usá SEMANAL o MENSUAL.");
		}
	}

	// ---------------------------------------------------------------------
	// ¿EL DÍA DE ENTREGA ES VÁLIDO PARA ESTA FRECUENCIA?
	// ---------------------------------------------------------------------
	public boolean esDiaValido(int diaEntrega) {
		return diaEntrega >= 1 && diaEntrega <= diaMaximo;
	}

	// ---------------------------------------------------------------------
	// PRIMERA ENTREGA: la primera fecha que cae en el día de entrega, hoy incluido
	// Semanal: el próximo día de la semana elegido. Mensual: el día del mes elegido, este mes o el siguiente.
	// ---------------------------------------------------------------------
	public LocalDate primeraFecha(int diaEntrega, LocalDate desde) {
		if (this == SEMANAL) {
			return desde.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(diaEntrega)));
		}
		LocalDate esteMes = desde.withDayOfMonth(diaEntrega);
		return esteMes.isBefore(desde) ? YearMonth.from(desde).plusMonths(1).atDay(diaEntrega) : esteMes;
	}

	// ---------------------------------------------------------------------
	// SIGUIENTE ENTREGA, a partir de la que se acaba de generar
	// Semanal: 7 días después. Mensual: el mismo día del mes siguiente.
	// ---------------------------------------------------------------------
	public LocalDate siguienteFecha(LocalDate fechaActual, int diaEntrega) {
		if (this == SEMANAL) {
			return fechaActual.plusDays(DIAS_ENTRE_ENTREGAS_SEMANALES);
		}
		return YearMonth.from(fechaActual).plusMonths(1).atDay(diaEntrega);
	}
}
