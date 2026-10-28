package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.service.CatalogoProduccionService;
import com.cafedefinca.backend.service.OrigenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/origenes (catálogo) y GET /api/origenes/stock (alertas) viven en controllers distintos.
 * Esta prueba confirma que las dos rutas conviven sin pisarse.
 */
@WebMvcTest({OrigenController.class, CatalogoProduccionController.class})
class RutasOrigenesTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private OrigenService origenService;

	@MockitoBean
	private CatalogoProduccionService catalogoProduccionService;

	// ---------------------------------------------------------------------
	// CADA RUTA LLEGA A SU SERVICE
	// ---------------------------------------------------------------------
	@Test
	void origenesLlegaAlCatalogoYOrigenesStockLlegaAlStock() throws Exception {
		when(catalogoProduccionService.listarOrigenes()).thenReturn(List.of());
		when(origenService.listarStock()).thenReturn(List.of());

		mockMvc.perform(get("/api/origenes")).andExpect(status().isOk());
		verify(catalogoProduccionService).listarOrigenes();

		mockMvc.perform(get("/api/origenes/stock")).andExpect(status().isOk());
		verify(origenService).listarStock();
	}

	// ---------------------------------------------------------------------
	// LOS OTROS CATÁLOGOS RESPONDEN
	// ---------------------------------------------------------------------
	@Test
	void proveedoresYPerfilesDeTuesteResponden() throws Exception {
		when(catalogoProduccionService.listarProveedores()).thenReturn(List.of());
		when(catalogoProduccionService.listarPerfilesTueste()).thenReturn(List.of());

		mockMvc.perform(get("/api/proveedores")).andExpect(status().isOk());
		mockMvc.perform(get("/api/perfiles-tueste")).andExpect(status().isOk());
	}
}
