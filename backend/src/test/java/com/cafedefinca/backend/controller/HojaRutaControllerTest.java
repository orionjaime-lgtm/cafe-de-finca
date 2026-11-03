package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.HojaRutaDto;
import com.cafedefinca.backend.dto.HojaRutaDto.Parada;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.service.HojaRutaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba la capa web de /api/hojas-ruta: el filtro por fecha, el formato de la respuesta y los errores.
 * El service va simulado: acá no hay base de datos.
 */
@WebMvcTest(HojaRutaController.class)
class HojaRutaControllerTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private HojaRutaService hojaRutaService;

	private HojaRutaDto hoja() {
		return new HojaRutaDto(1, LocalDate.of(2026, 11, 9), "PENDIENTE", List.of(
				new Parada(1, 10, 4, "Hotel Andino", "Av. Libertador 1200", "Vicente López",
						new BigDecimal("324000.00"), "PENDIENTE")));
	}

	// ---------------------------------------------------------------------
	// GET /api/hojas-ruta
	// ---------------------------------------------------------------------
	@Test
	void listarSinFechaDevuelveTodasConSusParadas() throws Exception {
		when(hojaRutaService.listar(null)).thenReturn(List.of(hoja()));

		mockMvc.perform(get("/api/hojas-ruta"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].fechaReparto").value("2026-11-09"))
				.andExpect(jsonPath("$[0].paradas[0].ordenVisita").value(1))
				.andExpect(jsonPath("$[0].paradas[0].cliente").value("Hotel Andino"))
				.andExpect(jsonPath("$[0].paradas[0].domicilio").value("Av. Libertador 1200"))
				.andExpect(jsonPath("$[0].paradas[0].localidad").value("Vicente López"));
	}

	@Test
	void listarConFechaLaPasaAlService() throws Exception {
		when(hojaRutaService.listar(LocalDate.of(2026, 11, 9))).thenReturn(List.of(hoja()));

		mockMvc.perform(get("/api/hojas-ruta").param("fecha", "2026-11-09"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].idHojaRuta").value(1));
		verify(hojaRutaService).listar(LocalDate.of(2026, 11, 9));
	}

	@Test
	void unaFechaSinRepartoDevuelveUnaListaVacia() throws Exception {
		when(hojaRutaService.listar(LocalDate.of(2026, 12, 25))).thenReturn(List.of());

		mockMvc.perform(get("/api/hojas-ruta").param("fecha", "2026-12-25"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());
	}

	@Test
	void unaFechaConFormatoInvalidoResponde400() throws Exception {
		mockMvc.perform(get("/api/hojas-ruta").param("fecha", "09/11/2026"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.estado").value(400));
		verify(hojaRutaService, never()).listar(any());
	}

	// ---------------------------------------------------------------------
	// GET /api/hojas-ruta/{id}
	// ---------------------------------------------------------------------
	@Test
	void obtenerUnaHojaDevuelveSusParadas() throws Exception {
		when(hojaRutaService.obtener(1)).thenReturn(hoja());

		mockMvc.perform(get("/api/hojas-ruta/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.estadoRuta").value("PENDIENTE"))
				.andExpect(jsonPath("$.paradas.length()").value(1));
	}

	@Test
	void unaHojaQueNoExisteResponde400ConElFormatoDelEquipo() throws Exception {
		when(hojaRutaService.obtener(99)).thenThrow(new SolicitudInvalidaException("No existe la hoja de ruta 99."));

		mockMvc.perform(get("/api/hojas-ruta/99"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.estado").value(400))
				.andExpect(jsonPath("$.mensaje").value("No existe la hoja de ruta 99."));
	}

	@Test
	void unIdQueNoEsNumeroResponde400() throws Exception {
		mockMvc.perform(get("/api/hojas-ruta/abc")).andExpect(status().isBadRequest());
	}

	@Test
	void lasHojasDeRutaSonSoloDeLectura() throws Exception {
		mockMvc.perform(post("/api/hojas-ruta")).andExpect(status().isMethodNotAllowed());
	}
}
