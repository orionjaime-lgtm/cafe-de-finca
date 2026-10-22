package com.cafedefinca.backend.service;

import com.cafedefinca.backend.enums.EstadoStock;
import com.cafedefinca.backend.repository.LoteCafeVerdeRepository;
import com.cafedefinca.backend.repository.OrigenCafeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la regla que compara el stock de café verde con el mínimo.
 */
class OrigenServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final BigDecimal minimo = new BigDecimal("5000");
	private final OrigenService servicio = new OrigenService(
			Mockito.mock(OrigenCafeRepository.class),
			Mockito.mock(LoteCafeVerdeRepository.class));

	// ---------------------------------------------------------------------
	// ESTADO DEL STOCK
	// ---------------------------------------------------------------------
	@Test
	void stockIgualAlMinimoEstaOk() {
		assertThat(servicio.calcularEstado(new BigDecimal("5000.000"), minimo)).isEqualTo(EstadoStock.OK);
	}

	@Test
	void stockDebajoDelMinimoEstaBajo() {
		assertThat(servicio.calcularEstado(new BigDecimal("2300"), minimo)).isEqualTo(EstadoStock.BAJO);
	}

	@Test
	void stockEnCeroEstaCritico() {
		assertThat(servicio.calcularEstado(BigDecimal.ZERO, minimo)).isEqualTo(EstadoStock.CRITICO);
	}

	@Test
	void sinMinimoConfiguradoNuncaHayAlerta() {
		assertThat(servicio.calcularEstado(BigDecimal.ZERO, BigDecimal.ZERO)).isEqualTo(EstadoStock.OK);
	}
}
