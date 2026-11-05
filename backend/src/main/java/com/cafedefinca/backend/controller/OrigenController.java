package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.ActualizarStockMinimoDto;
import com.cafedefinca.backend.dto.OrigenStockDto;
import com.cafedefinca.backend.service.OrigenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de orígenes de café.
 */
@RestController
@RequestMapping("/api/origenes")
public class OrigenController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final OrigenService origenService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public OrigenController(OrigenService origenService) {
		this.origenService = origenService;
	}

	// ---------------------------------------------------------------------
	// GET /api/origenes/stock -> tabla "Límites de Stock Mínimo"
	// ---------------------------------------------------------------------
	@GetMapping("/stock")
	public List<OrigenStockDto> listarStock() {
		return origenService.listarStock();
	}

	// ---------------------------------------------------------------------
	// PATCH /api/origenes/{id}/stock-minimo -> edición de alertas
	// ---------------------------------------------------------------------
	@PatchMapping("/{idOrigen}/stock-minimo")
	public OrigenStockDto actualizarStockMinimo(@PathVariable Integer idOrigen,
			@Valid @RequestBody ActualizarStockMinimoDto datos) {
		return origenService.actualizarStockMinimo(idOrigen, datos);
	}
}
