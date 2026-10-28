package com.cafedefinca.backend.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de las conversiones de unidades.
 */
class ConversorUnidadesTest {

	// ---------------------------------------------------------------------
	// KILOS A GRAMOS
	// ---------------------------------------------------------------------

	@Test
	void dosKilosYPuntoCincoDanDosMilQuinientosGramos() {
		BigDecimal kilos = new BigDecimal("2.5");
		BigDecimal gramos = ConversorUnidades.aGramos(kilos);
		assertThat(gramos).isEqualByComparingTo("2500");
	}

	@Test
	void unKiloDaMilGramos() {
		BigDecimal kilos = new BigDecimal("1");
		BigDecimal gramos = ConversorUnidades.aGramos(kilos);
		assertThat(gramos).isEqualByComparingTo("1000");
	}

	@Test
	void ceroKilosDaCeroGramos() {
		BigDecimal kilos = BigDecimal.ZERO;
		BigDecimal gramos = ConversorUnidades.aGramos(kilos);
		assertThat(gramos).isEqualByComparingTo("0");
	}

	@Test
	void kilosAGramosEsAliasDeAGramos() {
		BigDecimal kilos = new BigDecimal("3.2");
		assertThat(ConversorUnidades.kilosAGramos(kilos)).isEqualByComparingTo(ConversorUnidades.aGramos(kilos));
	}

	// ---------------------------------------------------------------------
	// GRAMOS A KILOS
	// ---------------------------------------------------------------------

	@Test
	void dosMilTrescientosGramosDanDosKilosTrescientos() {
		BigDecimal gramos = new BigDecimal("2300");
		BigDecimal kilos = ConversorUnidades.gramosAKilos(gramos);
		assertThat(kilos).isEqualByComparingTo("2.300");
	}

	@Test
	void unGramoDaCeroPuntoCeroCeroUnoKilos() {
		BigDecimal gramos = new BigDecimal("1");
		BigDecimal kilos = ConversorUnidades.gramosAKilos(gramos);
		assertThat(kilos).isEqualByComparingTo("0.001");
	}

	@Test
	void quinientosGramosDanCeroPuntoQuinientosKilos() {
		BigDecimal gramos = new BigDecimal("500");
		BigDecimal kilos = ConversorUnidades.gramosAKilos(gramos);
		assertThat(kilos).isEqualByComparingTo("0.500");
	}

	@Test
	void milQuinientosGramosDanUnoPuntoQuinientosKilos() {
		BigDecimal gramos = new BigDecimal("1500");
		BigDecimal kilos = ConversorUnidades.gramosAKilos(gramos);
		assertThat(kilos).isEqualByComparingTo("1.500");
	}
}