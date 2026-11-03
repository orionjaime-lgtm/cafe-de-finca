package com.cafedefinca.backend.enums;

import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba el cálculo de la fecha de entrega de los abonos: la primera y la siguiente, semanal y mensual.
 * Referencias de calendario: el 02/11/2026 es lunes, el 03/11 martes y el 04/11 miércoles.
 */
class FrecuenciaSuscripcionTest {

	// ---------------------------------------------------------------------
	// SIGUIENTE ENTREGA SEMANAL: SIEMPRE 7 DÍAS DESPUÉS
	// ---------------------------------------------------------------------
	@Test
	void laSiguienteEntregaSemanalEs7DiasDespues() {
		assertThat(FrecuenciaSuscripcion.SEMANAL.siguienteFecha(LocalDate.of(2026, 11, 2), 1))
				.isEqualTo(LocalDate.of(2026, 11, 9));
	}

	@Test
	void laSiguienteEntregaSemanalCruzaElFinDeMesYDeAnio() {
		assertThat(FrecuenciaSuscripcion.SEMANAL.siguienteFecha(LocalDate.of(2026, 11, 30), 1))
				.isEqualTo(LocalDate.of(2026, 12, 7));
		assertThat(FrecuenciaSuscripcion.SEMANAL.siguienteFecha(LocalDate.of(2026, 12, 28), 1))
				.isEqualTo(LocalDate.of(2027, 1, 4));
	}

	@Test
	void laSiguienteEntregaSemanalRespetaElAnioBisiesto() {
		assertThat(FrecuenciaSuscripcion.SEMANAL.siguienteFecha(LocalDate.of(2028, 2, 28), 1))
				.isEqualTo(LocalDate.of(2028, 3, 6));
	}

	// ---------------------------------------------------------------------
	// SIGUIENTE ENTREGA MENSUAL: EL MISMO DÍA DEL MES SIGUIENTE
	// ---------------------------------------------------------------------
	@Test
	void laSiguienteEntregaMensualEsElMismoDiaDelMesSiguiente() {
		assertThat(FrecuenciaSuscripcion.MENSUAL.siguienteFecha(LocalDate.of(2026, 11, 15), 15))
				.isEqualTo(LocalDate.of(2026, 12, 15));
	}

	@Test
	void laSiguienteEntregaMensualDeDiciembrePasaAEneroDelAnioSiguiente() {
		assertThat(FrecuenciaSuscripcion.MENSUAL.siguienteFecha(LocalDate.of(2026, 12, 10), 10))
				.isEqualTo(LocalDate.of(2027, 1, 10));
	}

	@Test
	void elDia28SiempreExisteAunqueElMesSiguienteSeaFebrero() {
		assertThat(FrecuenciaSuscripcion.MENSUAL.siguienteFecha(LocalDate.of(2026, 1, 28), 28))
				.isEqualTo(LocalDate.of(2026, 2, 28));
		assertThat(FrecuenciaSuscripcion.MENSUAL.siguienteFecha(LocalDate.of(2028, 1, 28), 28))
				.isEqualTo(LocalDate.of(2028, 2, 28));
		assertThat(FrecuenciaSuscripcion.MENSUAL.siguienteFecha(LocalDate.of(2026, 2, 28), 28))
				.isEqualTo(LocalDate.of(2026, 3, 28));
	}

	@Test
	void unaEntregaMensualQueSeAtrasoVuelveASuDiaDelMes() {
		// Si la fecha cargada no cae en el día de entrega, la siguiente corrige al día del abono
		assertThat(FrecuenciaSuscripcion.MENSUAL.siguienteFecha(LocalDate.of(2026, 11, 20), 5))
				.isEqualTo(LocalDate.of(2026, 12, 5));
	}

	// ---------------------------------------------------------------------
	// PRIMERA ENTREGA SEMANAL: EL PRÓXIMO DÍA DE LA SEMANA ELEGIDO (HOY INCLUIDO)
	// ---------------------------------------------------------------------
	@Test
	void laPrimeraEntregaSemanalDeHoyMismoSiCoincideElDia() {
		// 02/11/2026 es lunes (día 1)
		assertThat(FrecuenciaSuscripcion.SEMANAL.primeraFecha(1, LocalDate.of(2026, 11, 2)))
				.isEqualTo(LocalDate.of(2026, 11, 2));
	}

	@Test
	void laPrimeraEntregaSemanalEsElProximoDiaElegidoDeEstaSemana() {
		assertThat(FrecuenciaSuscripcion.SEMANAL.primeraFecha(3, LocalDate.of(2026, 11, 2)))
				.isEqualTo(LocalDate.of(2026, 11, 4));
		assertThat(FrecuenciaSuscripcion.SEMANAL.primeraFecha(7, LocalDate.of(2026, 11, 2)))
				.isEqualTo(LocalDate.of(2026, 11, 8));
	}

	@Test
	void laPrimeraEntregaSemanalPasaALaSemanaSiguienteSiElDiaYaPaso() {
		// 03/11/2026 es martes y el abono es los lunes
		assertThat(FrecuenciaSuscripcion.SEMANAL.primeraFecha(1, LocalDate.of(2026, 11, 3)))
				.isEqualTo(LocalDate.of(2026, 11, 9));
	}

	// ---------------------------------------------------------------------
	// PRIMERA ENTREGA MENSUAL: EL DÍA DEL MES, ESTE MES O EL SIGUIENTE
	// ---------------------------------------------------------------------
	@Test
	void laPrimeraEntregaMensualDeHoyMismoSiCoincideElDia() {
		assertThat(FrecuenciaSuscripcion.MENSUAL.primeraFecha(2, LocalDate.of(2026, 11, 2)))
				.isEqualTo(LocalDate.of(2026, 11, 2));
	}

	@Test
	void laPrimeraEntregaMensualEsEsteMesSiElDiaTodaviaNoLlego() {
		assertThat(FrecuenciaSuscripcion.MENSUAL.primeraFecha(15, LocalDate.of(2026, 11, 2)))
				.isEqualTo(LocalDate.of(2026, 11, 15));
	}

	@Test
	void laPrimeraEntregaMensualEsElMesSiguienteSiElDiaYaPaso() {
		assertThat(FrecuenciaSuscripcion.MENSUAL.primeraFecha(1, LocalDate.of(2026, 11, 2)))
				.isEqualTo(LocalDate.of(2026, 12, 1));
		assertThat(FrecuenciaSuscripcion.MENSUAL.primeraFecha(5, LocalDate.of(2026, 12, 20)))
				.isEqualTo(LocalDate.of(2027, 1, 5));
		assertThat(FrecuenciaSuscripcion.MENSUAL.primeraFecha(28, LocalDate.of(2026, 1, 31)))
				.isEqualTo(LocalDate.of(2026, 2, 28));
	}

	// ---------------------------------------------------------------------
	// DÍAS VÁLIDOS: SEMANAL 1 A 7, MENSUAL 1 A 28
	// ---------------------------------------------------------------------
	@Test
	void losDiasValidosDependenDeLaFrecuencia() {
		assertThat(FrecuenciaSuscripcion.SEMANAL.esDiaValido(1)).isTrue();
		assertThat(FrecuenciaSuscripcion.SEMANAL.esDiaValido(7)).isTrue();
		assertThat(FrecuenciaSuscripcion.SEMANAL.esDiaValido(0)).isFalse();
		assertThat(FrecuenciaSuscripcion.SEMANAL.esDiaValido(8)).isFalse();
		assertThat(FrecuenciaSuscripcion.MENSUAL.esDiaValido(1)).isTrue();
		assertThat(FrecuenciaSuscripcion.MENSUAL.esDiaValido(28)).isTrue();
		assertThat(FrecuenciaSuscripcion.MENSUAL.esDiaValido(29)).isFalse();
		assertThat(FrecuenciaSuscripcion.MENSUAL.esDiaValido(-1)).isFalse();
	}

	// ---------------------------------------------------------------------
	// TEXTO QUE MANDA EL FRONTEND
	// ---------------------------------------------------------------------
	@Test
	void elTextoSeInterpretaSinImportarMayusculasNiEspacios() {
		assertThat(FrecuenciaSuscripcion.desdeTexto("semanal")).isEqualTo(FrecuenciaSuscripcion.SEMANAL);
		assertThat(FrecuenciaSuscripcion.desdeTexto("  Mensual ")).isEqualTo(FrecuenciaSuscripcion.MENSUAL);
	}

	@Test
	void unaFrecuenciaDesconocidaOVaciaSeRechaza() {
		assertThatThrownBy(() -> FrecuenciaSuscripcion.desdeTexto("diaria"))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("Frecuencia desconocida");
		assertThatThrownBy(() -> FrecuenciaSuscripcion.desdeTexto("  "))
				.isInstanceOf(SolicitudInvalidaException.class);
		assertThatThrownBy(() -> FrecuenciaSuscripcion.desdeTexto(null))
				.isInstanceOf(SolicitudInvalidaException.class);
	}
}
