package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ActualizarStockMinimoDto;
import com.cafedefinca.backend.dto.TotalPorOrigen;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.enums.EstadoStock;
import com.cafedefinca.backend.repository.LoteCafeVerdeRepository;
import com.cafedefinca.backend.repository.OrigenCafeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

	@Test
	void actualizaElMinimoYDevuelveElStockRecalculado() {
		OrigenCafeRepository origenRepo = mock(OrigenCafeRepository.class);
		LoteCafeVerdeRepository loteRepo = mock(LoteCafeVerdeRepository.class);
		OrigenCafe origen = mock(OrigenCafe.class);
		when(origen.getIdOrigen()).thenReturn(7);
		when(origen.getPais()).thenReturn("Brasil");
		when(origen.getRegion()).thenReturn("Cerrado");
		when(origen.getVariedad()).thenReturn("Bourbon");
		when(origen.getStockMinimoAlertaGramos()).thenReturn(new BigDecimal("12000"));
		when(origenRepo.findById(7)).thenReturn(Optional.of(origen));
		when(loteRepo.sumarStockPorOrigen()).thenReturn(List.of(new TotalPorOrigen(7, new BigDecimal("9000"))));

		OrigenService servicio = new OrigenService(origenRepo, loteRepo);
		var resultado = servicio.actualizarStockMinimo(7, new ActualizarStockMinimoDto(new BigDecimal("12000")));

		verify(origen).actualizarStockMinimoAlerta(new BigDecimal("12000"));
		verify(origenRepo).save(origen);
		assertThat(resultado.stockActualGramos()).isEqualTo(new BigDecimal("9000"));
		assertThat(resultado.estado()).isEqualTo(EstadoStock.BAJO);
	}
}
