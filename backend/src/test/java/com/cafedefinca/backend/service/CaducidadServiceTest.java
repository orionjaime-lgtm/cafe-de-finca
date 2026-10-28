package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ResumenCaducidadDto;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.enums.EstadoFrescura;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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

	// ---------------------------------------------------------------------
	// RESUMEN: LOS 3 NÚMEROS SON EXCLUYENTES
	// ---------------------------------------------------------------------
	@Test
	void elResumenCuentaCadaLoteEnUnSoloNumero() {
		LoteCafeTostadoRepository repositorio = Mockito.mock(LoteCafeTostadoRepository.class);
		Clock relojFijo = Clock.fixed(Instant.parse("2026-10-21T12:00:00Z"), ZoneId.of("UTC"));
		CaducidadService servicioConRelojFijo = new CaducidadService(repositorio, relojFijo);

		// Los lotes se arman antes de stubbear el repositorio (Mockito no admite stubbings anidados)
		List<LoteCafeTostado> lotes = List.of(
				lote("DISPONIBLE", hoy.plusDays(20)),
				lote("DISPONIBLE", hoy.plusDays(8)),
				lote("DISPONIBLE", hoy.plusDays(7)),
				lote("DISPONIBLE", hoy),
				lote("DISPONIBLE", hoy.minusDays(1)),
				lote("VENCIDO", hoy.plusDays(20)));
		when(repositorio.buscarEnInventario()).thenReturn(lotes);

		ResumenCaducidadDto resumen = servicioConRelojFijo.obtenerResumen();

		assertThat(resumen.activos()).isEqualTo(2);
		assertThat(resumen.porVencer()).isEqualTo(2);
		assertThat(resumen.vencidos()).isEqualTo(2);
	}

	// ---------------------------------------------------------------------
	// AYUDANTE PARA ARMAR LOTES DE PRUEBA
	// ---------------------------------------------------------------------
	private LoteCafeTostado lote(String estado, LocalDate fechaCaducidad) {
		LoteCafeTostado lote = Mockito.mock(LoteCafeTostado.class);
		when(lote.getEstadoDisponibilidad()).thenReturn(estado);
		when(lote.getFechaCaducidad()).thenReturn(fechaCaducidad);
		return lote;
	}
}
