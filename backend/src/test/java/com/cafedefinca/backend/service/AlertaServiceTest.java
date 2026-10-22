package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.AlertaDto;
import com.cafedefinca.backend.dto.OrigenStockDto;
import com.cafedefinca.backend.dto.TotalPorOrigen;
import com.cafedefinca.backend.enums.EstadoStock;
import com.cafedefinca.backend.enums.NivelAlerta;
import com.cafedefinca.backend.repository.OrdenTuesteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Prueba qué alertas se generan, con orígenes y consumos armados a mano (sin base de datos).
 */
class AlertaServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final Clock relojFijo = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneId.of("UTC"));
	private OrigenService origenService;
	private OrdenTuesteRepository ordenTuesteRepository;
	private AlertaService servicio;

	@BeforeEach
	void preparar() {
		origenService = Mockito.mock(OrigenService.class);
		ordenTuesteRepository = Mockito.mock(OrdenTuesteRepository.class);
		servicio = new AlertaService(origenService, ordenTuesteRepository, relojFijo);
	}

	// ---------------------------------------------------------------------
	// ALERTAS CRÍTICAS
	// ---------------------------------------------------------------------
	@Test
	void origenBajoElMinimoGeneraAlertaCritica() {
		when(origenService.listarStock()).thenReturn(List.of(
				origen(1, "Colombia", "Huila", "2300", "5000", EstadoStock.BAJO)));
		when(ordenTuesteRepository.sumarVerdeTostadoPorOrigenDesde(any())).thenReturn(List.of());

		List<AlertaDto> alertas = servicio.listarAlertas();

		assertThat(alertas).hasSize(1);
		assertThat(alertas.get(0).nivel()).isEqualTo(NivelAlerta.CRITICO);
		assertThat(alertas.get(0).mensaje())
				.isEqualTo("Stock crítico: 2.3 kg restantes. El mínimo configurado es 5.0 kg.");
	}

	@Test
	void origenAgotadoGeneraAlertaCritica() {
		when(origenService.listarStock()).thenReturn(List.of(
				origen(3, "Panamá", "Boquete", "0", "3000", EstadoStock.CRITICO)));
		when(ordenTuesteRepository.sumarVerdeTostadoPorOrigenDesde(any())).thenReturn(List.of());

		List<AlertaDto> alertas = servicio.listarAlertas();

		assertThat(alertas).hasSize(1);
		assertThat(alertas.get(0).mensaje()).startsWith("Stock agotado totalmente.");
	}

	// ---------------------------------------------------------------------
	// ALERTAS PREDICTIVAS
	// ---------------------------------------------------------------------
	@Test
	void consumoRapidoGeneraAdvertenciaConDiasEstimados() {
		// 12 kg en stock, mínimo 10 kg -> margen de 2000 g
		// 25 kg tostados en 30 días -> 833.333 g/día -> 2000 / 833.333 = 2.4 -> 3 días
		when(origenService.listarStock()).thenReturn(List.of(
				origen(2, "Brasil", "Santos", "12000", "10000", EstadoStock.OK)));
		when(ordenTuesteRepository.sumarVerdeTostadoPorOrigenDesde(any())).thenReturn(List.of(
				new TotalPorOrigen(2, new BigDecimal("25000"))));

		List<AlertaDto> alertas = servicio.listarAlertas();

		assertThat(alertas).hasSize(1);
		assertThat(alertas.get(0).nivel()).isEqualTo(NivelAlerta.ADVERTENCIA);
		assertThat(alertas.get(0).diasEstimados()).isEqualTo(3L);
	}

	@Test
	void consumoLentoNoGeneraAlerta() {
		// 18.4 kg en stock, mínimo 6 kg, 12 kg tostados en 30 días -> 31 días hasta el mínimo
		when(origenService.listarStock()).thenReturn(List.of(
				origen(4, "Etiopía", "Yirgacheffe", "18400", "6000", EstadoStock.OK)));
		when(ordenTuesteRepository.sumarVerdeTostadoPorOrigenDesde(any())).thenReturn(List.of(
				new TotalPorOrigen(4, new BigDecimal("12000"))));

		assertThat(servicio.listarAlertas()).isEmpty();
	}

	// ---------------------------------------------------------------------
	// ORDEN: PRIMERO LAS CRÍTICAS
	// ---------------------------------------------------------------------
	@Test
	void lasCriticasAparecenAntesQueLasAdvertencias() {
		when(origenService.listarStock()).thenReturn(List.of(
				origen(2, "Brasil", "Santos", "12000", "10000", EstadoStock.OK),
				origen(1, "Colombia", "Huila", "2300", "5000", EstadoStock.BAJO)));
		when(ordenTuesteRepository.sumarVerdeTostadoPorOrigenDesde(any())).thenReturn(List.of(
				new TotalPorOrigen(2, new BigDecimal("25000"))));

		List<AlertaDto> alertas = servicio.listarAlertas();

		assertThat(alertas).extracting(AlertaDto::nivel)
				.containsExactly(NivelAlerta.CRITICO, NivelAlerta.ADVERTENCIA);
	}

	// ---------------------------------------------------------------------
	// AYUDANTE PARA ARMAR ORÍGENES DE PRUEBA
	// ---------------------------------------------------------------------
	private OrigenStockDto origen(int id, String pais, String region, String stock, String minimo, EstadoStock estado) {
		return new OrigenStockDto(id, pais, region, null, new BigDecimal(stock), new BigDecimal(minimo), estado);
	}
}
