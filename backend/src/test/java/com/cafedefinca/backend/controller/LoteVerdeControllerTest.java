package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.LoteVerdeDto;
import com.cafedefinca.backend.dto.NuevoLoteVerdeDto;
import com.cafedefinca.backend.service.LoteVerdeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la capa web de POST /api/lotes-verdes: validación del cuerpo, código HTTP y formato del error.
 * El service va simulado: acá no hay base de datos.
 */
@WebMvcTest(LoteVerdeController.class)
class LoteVerdeControllerTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private LoteVerdeService loteVerdeService;

	private static final String CUERPO_VALIDO = """
			{ "idOrigen": 1, "idProveedor": 2, "codigoLote": "LV-HUI-002",
			  "cantidad": 150, "unidad": "kg", "cantBolsas": 5 }
			""";

	// ---------------------------------------------------------------------
	// CASO FELIZ: 201
	// ---------------------------------------------------------------------
	@Test
	void unCuerpoValidoResponde201ConElLoteCreado() throws Exception {
		LoteVerdeDto creado = new LoteVerdeDto(10, "LV-HUI-002", 1, "Colombia Huila", 2, "Exportadora Huila",
				LocalDate.of(2026, 10, 28), 5, new BigDecimal("150000.000"), new BigDecimal("150000.000"));
		when(loteVerdeService.crear(any(NuevoLoteVerdeDto.class))).thenReturn(creado);

		mockMvc.perform(post("/api/lotes-verdes").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.codigoLote").value("LV-HUI-002"))
				.andExpect(jsonPath("$.pesoInicialGramos").value(150000.000));
	}

	// ---------------------------------------------------------------------
	// CUERPOS INVÁLIDOS: 400 CON JSON CLARO Y SIN LLAMAR AL SERVICE
	// ---------------------------------------------------------------------
	@Test
	void sinCodigoDeLoteResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"codigoLote\": \"LV-HUI-002\"", "\"codigoLote\": \"  \"");

		mockMvc.perform(post("/api/lotes-verdes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.estado").value(400))
				.andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("codigoLote")));
		verify(loteVerdeService, never()).crear(any());
	}

	@Test
	void conCantidadNegativaResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"cantidad\": 150", "\"cantidad\": -5");

		mockMvc.perform(post("/api/lotes-verdes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("cantidad")));
		verify(loteVerdeService, never()).crear(any());
	}

	@Test
	void sinCantidadDeBolsasResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace(", \"cantBolsas\": 5", "");

		mockMvc.perform(post("/api/lotes-verdes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("cantBolsas")));
		verify(loteVerdeService, never()).crear(any());
	}

	@Test
	void unCodigoDeMasDe50CaracteresResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("LV-HUI-002", "L".repeat(51));

		mockMvc.perform(post("/api/lotes-verdes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(org.hamcrest.Matchers.containsString("codigoLote")));
		verify(loteVerdeService, never()).crear(any());
	}

	@Test
	void unJsonRotoResponde400ConElFormatoDelEquipo() throws Exception {
		mockMvc.perform(post("/api/lotes-verdes").contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.estado").value(400));
	}
}
