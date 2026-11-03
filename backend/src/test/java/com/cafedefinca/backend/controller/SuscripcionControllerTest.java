package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.CambioEstadoSuscripcionDto;
import com.cafedefinca.backend.dto.NuevaSuscripcionDto;
import com.cafedefinca.backend.dto.SuscripcionDto;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.service.SuscripcionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la capa web de /api/suscripciones: validación del cuerpo, códigos HTTP y formato del error.
 * El service va simulado: acá no hay base de datos.
 */
@WebMvcTest(SuscripcionController.class)
class SuscripcionControllerTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private SuscripcionService suscripcionService;

	private static final String CUERPO_VALIDO = """
			{ "clienteId": 1, "frecuencia": "SEMANAL", "diaEntrega": 1,
			  "items": [ { "presentacionId": 3, "tipoMoliendaId": 4, "cantidadEnvases": 2, "precioUnitario": 120000.00 } ] }
			""";

	private SuscripcionDto abono(String estado) {
		return new SuscripcionDto(7, 1, "Hotel Andino", "SEMANAL", 1, LocalDate.of(2026, 11, 4), null,
				LocalDate.of(2026, 11, 9), estado, List.of());
	}

	// ---------------------------------------------------------------------
	// GET
	// ---------------------------------------------------------------------
	@Test
	void listarResponde200() throws Exception {
		when(suscripcionService.listar()).thenReturn(List.of(abono("ACTIVA")));

		mockMvc.perform(get("/api/suscripciones"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].cliente").value("Hotel Andino"));
	}

	// ---------------------------------------------------------------------
	// POST
	// ---------------------------------------------------------------------
	@Test
	void unCuerpoValidoResponde201() throws Exception {
		when(suscripcionService.crear(any(NuevaSuscripcionDto.class))).thenReturn(abono("ACTIVA"));

		mockMvc.perform(post("/api/suscripciones").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.estado").value("ACTIVA"))
				.andExpect(jsonPath("$.proximaFechaGeneracion").value("2026-11-09"));
	}

	@Test
	void sinRenglonesResponde400() throws Exception {
		String cuerpo = """
				{ "clienteId": 1, "frecuencia": "SEMANAL", "diaEntrega": 1, "items": [] }
				""";

		mockMvc.perform(post("/api/suscripciones").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("items")));
		verify(suscripcionService, never()).crear(any());
	}

	@Test
	void unRenglonConCantidadCeroResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"cantidadEnvases\": 2", "\"cantidadEnvases\": 0");

		mockMvc.perform(post("/api/suscripciones").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest());
		verify(suscripcionService, never()).crear(any());
	}

	@Test
	void sinFrecuenciaResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"frecuencia\": \"SEMANAL\",", "");

		mockMvc.perform(post("/api/suscripciones").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("frecuencia")));
	}

	@Test
	void unEnteroConDecimalesResponde400EnVezDeTruncarse() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"diaEntrega\": 1", "\"diaEntrega\": 1.9");

		mockMvc.perform(post("/api/suscripciones").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest());
		verify(suscripcionService, never()).crear(any());
	}

	// ---------------------------------------------------------------------
	// PATCH
	// ---------------------------------------------------------------------
	@Test
	void cambiarElEstadoResponde200() throws Exception {
		when(suscripcionService.cambiarEstado(eq(7), any(CambioEstadoSuscripcionDto.class))).thenReturn(abono("PAUSADA"));

		mockMvc.perform(patch("/api/suscripciones/7").contentType(MediaType.APPLICATION_JSON).content("{ \"estado\": \"PAUSADA\" }"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estado").value("PAUSADA"));
	}

	@Test
	void unCambioNoPermitidoResponde409ConElFormatoDelEquipo() throws Exception {
		when(suscripcionService.cambiarEstado(eq(7), any(CambioEstadoSuscripcionDto.class)))
				.thenThrow(new ConflictoException("La suscripción ya está cancelada."));

		mockMvc.perform(patch("/api/suscripciones/7").contentType(MediaType.APPLICATION_JSON).content("{ \"estado\": \"ACTIVA\" }"))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.estado").value(409))
				.andExpect(jsonPath("$.mensaje").value("La suscripción ya está cancelada."));
	}

	@Test
	void sinEstadoResponde400() throws Exception {
		mockMvc.perform(patch("/api/suscripciones/7").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("estado")));
		verify(suscripcionService, never()).cambiarEstado(any(), any());
	}

	@Test
	void unIdQueNoEsNumeroResponde400() throws Exception {
		mockMvc.perform(patch("/api/suscripciones/abc").contentType(MediaType.APPLICATION_JSON).content("{ \"estado\": \"PAUSADA\" }"))
				.andExpect(status().isBadRequest());
	}
}
