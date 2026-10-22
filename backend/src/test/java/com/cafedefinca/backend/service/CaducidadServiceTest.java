package com.cafedefinca.backend.service;

import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.enums.EstadoFrescura;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Prueba las reglas de frescura sin base de datos.
 */
class CaducidadServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final LocalDate hoy = LocalDate.of(2026, 10, 21);
	private final CaducidadService servicio = new CaducidadService(
			Mockito.mock(LoteCafeTostadoRepository.class),
			Clock.systemDefaultZone());

	// ---------------------------------------------------------------------
	// DÍAS RESTANTES
	// ---------------------------------------------------------------------
	@Test
	void diasRestantesEsNegativoSiLaFechaYaPaso() {
		assertThat(servicio.calcularDiasRestantes(hoy.minusDays(1), hoy)).isEqualTo(-1);
		assertThat(servicio.calcularDiasRestantes(hoy.plusDays(4), hoy)).isEqualTo(4);
	}

	// ---------------------------------------------------------------------
	// ESTADO DE FRESCURA (los bordes de cada rango)
	// ---------------------------------------------------------------------
	@Test
	void unDiaDespuesDeCaducarEstaVencido() {
		assertThat(servicio.calcularEstadoFrescura(-1)).isEqualTo(EstadoFrescura.VENCIDO);
	}

	@Test
	void elDiaDeLaCaducidadTodaviaEstaPorVencer() {
		assertThat(servicio.calcularEstadoFrescura(0)).isEqualTo(EstadoFrescura.POR_VENCER);
	}

	@Test
	void conSieteDiasEstaPorVencerYConOchoEstaOptimo() {
		assertThat(servicio.calcularEstadoFrescura(7)).isEqualTo(EstadoFrescura.POR_VENCER);
		assertThat(servicio.calcularEstadoFrescura(8)).isEqualTo(EstadoFrescura.OPTIMO);
	}

	// ---------------------------------------------------------------------
	// LOTE MARCADO VENCIDO A MANO EN LA BASE
	// ---------------------------------------------------------------------
	@Test
	void loteMarcadoVencidoEnLaBaseEstaVencidoAunqueSuFechaNoHayaLlegado() {
		LoteCafeTostado lote = Mockito.mock(LoteCafeTostado.class);
		when(lote.getEstadoDisponibilidad()).thenReturn("VENCIDO");
		when(lote.getFechaCaducidad()).thenReturn(hoy.plusDays(20));

		assertThat(servicio.calcularEstadoFrescura(lote, hoy)).isEqualTo(EstadoFrescura.VENCIDO);
	}
}
