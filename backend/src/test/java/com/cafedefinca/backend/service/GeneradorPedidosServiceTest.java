package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ResultadoGeneracionDto;
import com.cafedefinca.backend.dto.ResultadoGeneracionDto.PedidoGenerado;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.repository.SuscripcionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba cómo el generador recorre los abonos: que cada uno se procese por separado,
 * que un abono sin stock o con un error no frene a los demás y que quede registrado el motivo.
 * La generación de cada abono va simulada (se prueba aparte).
 */
class GeneradorPedidosServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final Clock relojFijo = Clock.fixed(Instant.parse("2026-11-04T15:00:00Z"), ZoneId.of("UTC"));
	private final LocalDate hoy = LocalDate.of(2026, 11, 4);
	private SuscripcionRepository suscripcionRepository;
	private GeneracionPedidoSuscripcionService porSuscripcion;
	private GeneradorPedidosService servicio;

	@BeforeEach
	void preparar() {
		suscripcionRepository = Mockito.mock(SuscripcionRepository.class);
		porSuscripcion = Mockito.mock(GeneracionPedidoSuscripcionService.class);
		servicio = new GeneradorPedidosService(suscripcionRepository, porSuscripcion, relojFijo);
	}

	// ---------------------------------------------------------------------
	// CASOS NORMALES
	// ---------------------------------------------------------------------
	@Test
	void sinAbonosPendientesNoGeneraNadaYNoFalla() {
		when(suscripcionRepository.buscarIdsConPedidoPendiente(hoy)).thenReturn(List.of());

		ResultadoGeneracionDto resultado = servicio.generar();

		assertThat(resultado.fecha()).isEqualTo(hoy);
		assertThat(resultado.generados()).isEmpty();
		assertThat(resultado.omitidas()).isEmpty();
	}

	@Test
	void generaElPedidoDeCadaAbonoPendiente() {
		when(suscripcionRepository.buscarIdsConPedidoPendiente(hoy)).thenReturn(List.of(1, 2));
		when(porSuscripcion.generarPedido(1)).thenReturn(Optional.of(generado(1, 100)));
		when(porSuscripcion.generarPedido(2)).thenReturn(Optional.of(generado(2, 101)));

		ResultadoGeneracionDto resultado = servicio.generar();

		assertThat(resultado.generados()).extracting(PedidoGenerado::pedidoId).containsExactly(100, 101);
		assertThat(resultado.omitidas()).isEmpty();
	}

	@Test
	void unAbonoQueYaNoLeTocaNoApareceNiComoGeneradoNiComoOmitido() {
		// Es lo que pasa si otra corrida lo generó entre que se armó la lista y se procesó
		when(suscripcionRepository.buscarIdsConPedidoPendiente(hoy)).thenReturn(List.of(1));
		when(porSuscripcion.generarPedido(1)).thenReturn(Optional.empty());

		ResultadoGeneracionDto resultado = servicio.generar();

		assertThat(resultado.generados()).isEmpty();
		assertThat(resultado.omitidas()).isEmpty();
	}

	// ---------------------------------------------------------------------
	// SIN STOCK: QUEDA REGISTRADO Y NO FRENA A LOS DEMÁS
	// ---------------------------------------------------------------------
	@Test
	void unAbonoSinStockSeOmiteConSuMotivoYLosDemasSiguen() {
		when(suscripcionRepository.buscarIdsConPedidoPendiente(hoy)).thenReturn(List.of(1, 2, 3));
		when(porSuscripcion.generarPedido(1)).thenReturn(Optional.of(generado(1, 100)));
		when(porSuscripcion.generarPedido(2))
				.thenThrow(new StockInsuficienteException("No hay un lote vendible con 10000 g libres."));
		when(porSuscripcion.generarPedido(3)).thenReturn(Optional.of(generado(3, 102)));

		ResultadoGeneracionDto resultado = servicio.generar();

		assertThat(resultado.generados()).extracting(PedidoGenerado::suscripcionId).containsExactly(1, 3);
		assertThat(resultado.omitidas()).hasSize(1);
		assertThat(resultado.omitidas().get(0).suscripcionId()).isEqualTo(2);
		assertThat(resultado.omitidas().get(0).motivo()).isEqualTo("No hay un lote vendible con 10000 g libres.");
		verify(porSuscripcion).generarPedido(3);
	}

	@Test
	void unErrorDeNegocioDeUnAbonoNoFrenaALosDemas() {
		when(suscripcionRepository.buscarIdsConPedidoPendiente(hoy)).thenReturn(List.of(1, 2, 3));
		when(porSuscripcion.generarPedido(1))
				.thenThrow(new SolicitudInvalidaException("El lote tostado con id 5 está vencido o agotado."));
		when(porSuscripcion.generarPedido(2)).thenThrow(new ConflictoException("Ya existe un pedido para esa fecha."));
		when(porSuscripcion.generarPedido(3)).thenReturn(Optional.of(generado(3, 102)));

		ResultadoGeneracionDto resultado = servicio.generar();

		assertThat(resultado.generados()).hasSize(1);
		assertThat(resultado.omitidas()).extracting(omitida -> omitida.suscripcionId()).containsExactly(1, 2);
		assertThat(resultado.omitidas().get(0).motivo()).contains("vencido o agotado");
	}

	// ---------------------------------------------------------------------
	// ERROR INESPERADO: NO FRENA A LOS DEMÁS Y NO FILTRA DETALLES INTERNOS
	// ---------------------------------------------------------------------
	@Test
	void unErrorInesperadoSeOmiteSinMostrarSusDetallesYLosDemasSiguen() {
		when(suscripcionRepository.buscarIdsConPedidoPendiente(hoy)).thenReturn(List.of(1, 2));
		when(porSuscripcion.generarPedido(1)).thenThrow(new RuntimeException("clave_secreta_interna SELECT * FROM"));
		when(porSuscripcion.generarPedido(2)).thenReturn(Optional.of(generado(2, 101)));

		ResultadoGeneracionDto resultado = servicio.generar();

		assertThat(resultado.generados()).hasSize(1);
		assertThat(resultado.omitidas()).hasSize(1);
		assertThat(resultado.omitidas().get(0).motivo())
				.isEqualTo("Error inesperado: se reintenta en la próxima corrida.")
				.doesNotContain("clave_secreta_interna");
	}

	// ---------------------------------------------------------------------
	// AYUDANTE
	// ---------------------------------------------------------------------
	private PedidoGenerado generado(int suscripcionId, int pedidoId) {
		return new PedidoGenerado(suscripcionId, pedidoId, hoy, new BigDecimal("240000.00"), 9, 1);
	}
}
