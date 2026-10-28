package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.NuevaOrdenTuesteDto;
import com.cafedefinca.backend.dto.OrdenTuesteDto;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.service.OrdenTuesteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la capa web de POST /api/ordenes-tueste: validación, códigos HTTP y formato del error.
 * El service va simulado: acá no hay base de datos.
 */
@WebMvcTest(OrdenTuesteController.class)
class OrdenTuesteControllerTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrdenTuesteService ordenTuesteService;

	private static final String CUERPO_VALIDO = """
			{ "idLoteVerde": 1, "idPerfilTueste": 2, "pesoVerdeUsado": 5,
			  "pesoTostadoObtenido": 4.25, "unidad": "kg", "operadorTostador": "Marta" }
			""";

	// ---------------------------------------------------------------------
	// CASO FELIZ: 201
	// ---------------------------------------------------------------------
	@Test
	void unCuerpoValidoResponde201ConLaOrdenYSuLoteTostado() throws Exception {
		OrdenTuesteDto creada = new OrdenTuesteDto(10, 1, "LV-HUI-001", "Colombia Huila", 2, "Tueste Medio",
				LocalDateTime.of(2026, 10, 28, 15, 0), new BigDecimal("5000.000"), new BigDecimal("4250.000"),
				new BigDecimal("15.00"), "Marta", "COMPLETADO", "LT-1092",
				LocalDateTime.of(2026, 10, 30, 15, 12), LocalDate.of(2026, 11, 27));
		when(ordenTuesteService.crear(any(NuevaOrdenTuesteDto.class))).thenReturn(creada);

		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.codigoLoteTostado").value("LT-1092"))
				.andExpect(jsonPath("$.estadoOrden").value("COMPLETADO"))
				.andExpect(jsonPath("$.mermaRealPct").value(15.00));
	}

	// ---------------------------------------------------------------------
	// STOCK INSUFICIENTE: 409 CON EL FORMATO DEL EQUIPO
	// ---------------------------------------------------------------------
	@Test
	void sinStockSuficienteResponde409ConElMensajeStockInsuficiente() throws Exception {
		when(ordenTuesteService.crear(any(NuevaOrdenTuesteDto.class)))
				.thenThrow(new StockInsuficienteException(new BigDecimal("2300.000"), new BigDecimal("5000.000")));

		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content(CUERPO_VALIDO))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.estado").value(409))
				.andExpect(jsonPath("$.mensaje").value(containsString("Stock insuficiente")));
	}

	// ---------------------------------------------------------------------
	// CUERPOS INVÁLIDOS: 400 Y SIN LLAMAR AL SERVICE
	// ---------------------------------------------------------------------
	@Test
	void sinLoteVerdeResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"idLoteVerde\": 1, ", "");

		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("idLoteVerde")));
		verify(ordenTuesteService, never()).crear(any());
	}

	@Test
	void conPesoVerdeNegativoResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"pesoVerdeUsado\": 5", "\"pesoVerdeUsado\": -5");

		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("pesoVerdeUsado")));
		verify(ordenTuesteService, never()).crear(any());
	}

	@Test
	void sinPesoTostadoResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"pesoTostadoObtenido\": 4.25, ", "");

		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("pesoTostadoObtenido")));
		verify(ordenTuesteService, never()).crear(any());
	}

	@Test
	void sinUnidadResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("\"unidad\": \"kg\"", "\"unidad\": \" \"");

		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("unidad")));
		verify(ordenTuesteService, never()).crear(any());
	}

	@Test
	void unOperadorDeMasDe100CaracteresResponde400() throws Exception {
		String cuerpo = CUERPO_VALIDO.replace("Marta", "M".repeat(101));

		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensaje").value(containsString("operadorTostador")));
		verify(ordenTuesteService, never()).crear(any());
	}

	@Test
	void unJsonRotoResponde400ConElFormatoDelEquipo() throws Exception {
		mockMvc.perform(post("/api/ordenes-tueste").contentType(MediaType.APPLICATION_JSON).content("{ no es json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.estado").value(400));
	}
}
