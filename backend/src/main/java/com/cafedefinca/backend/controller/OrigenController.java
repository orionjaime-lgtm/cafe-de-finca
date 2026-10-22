package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.OrigenStockDto;
import com.cafedefinca.backend.service.OrigenService;
import org.springframework.web.bind.annotation.GetMapping;
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
}
