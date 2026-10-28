package com.cafedefinca.backend.entity;

import com.cafedefinca.backend.exception.StockInsuficienteException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba la regla de negocio de descontar stock del café verde, sin base de datos.
 */
class LoteCafeVerdeTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final OrigenCafe origen = Mockito.mock(OrigenCafe.class);

	// ---------------------------------------------------------------------
	// DESCONTAR STOCK
	// ---------------------------------------------------------------------
	@Test
	void descontarRestaLosGramosDelStockActual() {
		LoteCafeVerde lote = loteConStock("10000");

		lote.descontarStock(new BigDecimal("2500.500"));

		assertThat(lote.getStockActualGramos()).isEqualByComparingTo("7499.500");
		assertThat(lote.getPesoInicialGramos()).isEqualByComparingTo("10000");
	}

	@Test
	void descontarTodoElStockJustoDejaElLoteEnCero() {
		LoteCafeVerde lote = loteConStock("10000");

		lote.descontarStock(new BigDecimal("10000"));

		assertThat(lote.getStockActualGramos()).isEqualByComparingTo("0");
	}

	@Test
	void descontarUnGramoDeMasLanzaStockInsuficienteYNoToca() {
		LoteCafeVerde lote = loteConStock("10000");

		assertThatThrownBy(() -> lote.descontarStock(new BigDecimal("10000.001")))
				.isInstanceOf(StockInsuficienteException.class)
				.hasMessage("Stock insuficiente: hay 10000 g disponibles y se pidieron 10000.001 g.");
		assertThat(lote.getStockActualGramos()).isEqualByComparingTo("10000");
	}

	@Test
	void descontarSobreUnLoteAgotadoLanzaStockInsuficiente() {
		LoteCafeVerde lote = loteConStock("10000");
		lote.descontarStock(new BigDecimal("10000"));

		assertThatThrownBy(() -> lote.descontarStock(new BigDecimal("1")))
				.isInstanceOf(StockInsuficienteException.class);
	}

	// ---------------------------------------------------------------------
	// AYUDANTE
	// ---------------------------------------------------------------------
	private LoteCafeVerde loteConStock(String gramos) {
		return new LoteCafeVerde(origen, 1, "LV-T-001", LocalDate.of(2026, 10, 1), 2, new BigDecimal(gramos));
	}
}
