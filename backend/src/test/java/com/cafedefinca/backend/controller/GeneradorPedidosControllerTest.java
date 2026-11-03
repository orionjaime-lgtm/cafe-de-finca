package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.ResultadoGeneracionDto;
import com.cafedefinca.backend.dto.ResultadoGeneracionDto.PedidoGenerado;
import com.cafedefinca.backend.dto.ResultadoGeneracionDto.SuscripcionOmitida;
import com.cafedefinca.backend.service.GeneradorPedidosService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba POST /api/suscripciones/generar: responde 200 con lo generado y lo omitido (con el motivo).
 * El service va simulado: acá no hay base de datos.
 */
@WebMvcTest(GeneradorPedidosController.class)
class GeneradorPedidosControllerTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GeneradorPedidosService generadorPedidosService;

	// ---------------------------------------------------------------------
	// POST /generar
	// ---------------------------------------------------------------------
	@Test
	void generarResponde200ConLoGeneradoYLoOmitido() throws Exception {
		when(generadorPedidosService.generar()).thenReturn(new ResultadoGeneracionDto(
				LocalDate.of(2026, 11, 4),
				List.of(new PedidoGenerado(1, 100, LocalDate.of(2026, 11, 4), new BigDecimal("240000.00"), 9, 1)),
				List.of(new SuscripcionOmitida(2, "No hay un lote vendible con 10000 g libres."))));

		mockMvc.perform(post("/api/suscripciones/generar"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.fecha").value("2026-11-04"))
				.andExpect(jsonPath("$.generados[0].pedidoId").value(100))
				.andExpect(jsonPath("$.generados[0].ordenVisita").value(1))
				.andExpect(jsonPath("$.omitidas[0].suscripcionId").value(2))
				.andExpect(jsonPath("$.omitidas[0].motivo").value("No hay un lote vendible con 10000 g libres."));
	}

	@Test
	void generarSoloAceptaPost() throws Exception {
		mockMvc.perform(get("/api/suscripciones/generar"))
				.andExpect(status().is4xxClientError());
	}
}
