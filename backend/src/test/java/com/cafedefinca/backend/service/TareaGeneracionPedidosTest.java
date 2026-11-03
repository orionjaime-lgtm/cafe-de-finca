package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ResultadoGeneracionDto;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba la tarea programada: que dispare el generador y que un error no la rompa.
 */
class TareaGeneracionPedidosTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final GeneradorPedidosService generador = Mockito.mock(GeneradorPedidosService.class);
	private final TareaGeneracionPedidos tarea = new TareaGeneracionPedidos(generador);

	// ---------------------------------------------------------------------
	// LA TAREA
	// ---------------------------------------------------------------------
	@Test
	void laTareaDisparaUnaVezElGenerador() {
		when(generador.generar()).thenReturn(new ResultadoGeneracionDto(LocalDate.of(2026, 11, 4), List.of(), List.of()));

		tarea.ejecutar();

		verify(generador, times(1)).generar();
	}

	@Test
	void siElGeneradorFallaLaTareaNoLanzaElErrorParaQueSigaCorriendoLosDiasSiguientes() {
		when(generador.generar()).thenThrow(new IllegalStateException("la base no responde"));

		assertThatCode(tarea::ejecutar).doesNotThrowAnyException();
	}
}
