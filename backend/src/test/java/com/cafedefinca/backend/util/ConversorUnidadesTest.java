package com.cafedefinca.backend.util;

import com.cafedefinca.backend.enums.UnidadPeso;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba la regla de oro: todo se convierte a gramos con precisión exacta.
 */
class ConversorUnidadesTest {

	// ---------------------------------------------------------------------
	// A GRAMOS
	// ---------------------------------------------------------------------
	@Test
	void doceKilosSonDoceMilGramos() {
		BigDecimal gramos = ConversorUnidades.aGramos(new BigDecimal("12"), UnidadPeso.KG);

		assertThat(gramos).isEqualByComparingTo("12000");
	}

	@Test
	void tresLibrasSonMilTrescientosSesentaGramosConSieteSieteSiete() {
		BigDecimal gramos = ConversorUnidades.aGramos(new BigDecimal("3"), UnidadPeso.LB);

		assertThat(gramos).isEqualByComparingTo("1360.777");
	}

	@Test
	void losGramosQuedanIgual() {
		BigDecimal gramos = ConversorUnidades.aGramos(new BigDecimal("8000"), UnidadPeso.G);

		assertThat(gramos).isEqualByComparingTo("8000");
	}

	@Test
	void aceptaLaUnidadComoTexto() {
		assertThat(ConversorUnidades.aGramos(new BigDecimal("2"), "Kilos")).isEqualByComparingTo("2000");
		assertThat(ConversorUnidades.aGramos(new BigDecimal("1"), " LB ")).isEqualByComparingTo("453.592");
	}

	@Test
	void decimalesSeRedondeanATresLugares() {
		// 0.5 lb = 226.796185 g -> 226.796 g
		assertThat(ConversorUnidades.aGramos(new BigDecimal("0.5"), UnidadPeso.LB)).isEqualByComparingTo("226.796");
	}

	// ---------------------------------------------------------------------
	// DATOS INVÁLIDOS -> error 400
	// ---------------------------------------------------------------------
	@Test
	void cantidadCeroSeRechaza() {
		assertThatThrownBy(() -> ConversorUnidades.aGramos(BigDecimal.ZERO, UnidadPeso.KG))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("mayor que cero");
	}

	@Test
	void cantidadNegativaSeRechaza() {
		assertThatThrownBy(() -> ConversorUnidades.aGramos(new BigDecimal("-1"), UnidadPeso.KG))
				.isInstanceOf(SolicitudInvalidaException.class);
	}

	@Test
	void unidadDesconocidaSeRechaza() {
		assertThatThrownBy(() -> ConversorUnidades.aGramos(BigDecimal.ONE, "toneladas"))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("Unidad desconocida");
	}

	@Test
	void unidadVaciaSeRechaza() {
		assertThatThrownBy(() -> ConversorUnidades.aGramos(BigDecimal.ONE, " "))
				.isInstanceOf(SolicitudInvalidaException.class);
	}

	// ---------------------------------------------------------------------
	// ENVASES A GRAMOS
	// ---------------------------------------------------------------------
	@Test
	void cuatroBolsasDe250SonMilGramos() {
		BigDecimal gramos = ConversorUnidades.envasesAGramos(4, new BigDecimal("250"));

		assertThat(gramos).isEqualByComparingTo("1000");
	}

	@Test
	void envasesEnCeroSeRechazan() {
		assertThatThrownBy(() -> ConversorUnidades.envasesAGramos(0, new BigDecimal("250")))
				.isInstanceOf(SolicitudInvalidaException.class);
	}

	// ---------------------------------------------------------------------
	// IDA Y VUELTA: gramos -> kilos sin perder precisión
	// ---------------------------------------------------------------------
	@Test
	void idaYVueltaNoPierdePrecision() {
		BigDecimal gramos = ConversorUnidades.aGramos(new BigDecimal("2.5"), UnidadPeso.KG);

		assertThat(ConversorUnidades.gramosAKilos(gramos)).isEqualByComparingTo("2.5");
	}

	@Test
	void dosMilTrescientosGramosSonDosPuntoTresKilos() {
		assertThat(ConversorUnidades.gramosAKilos(new BigDecimal("2300"))).isEqualByComparingTo("2.3");
	}
}
