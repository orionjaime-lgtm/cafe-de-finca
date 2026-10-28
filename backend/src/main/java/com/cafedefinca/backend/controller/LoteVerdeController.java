package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.LoteVerdeDto;
import com.cafedefinca.backend.dto.NuevoLoteVerdeDto;
import com.cafedefinca.backend.service.LoteVerdeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de lotes de café verde.
 */
@RestController
@RequestMapping("/api/lotes-verdes")
public class LoteVerdeController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final LoteVerdeService loteVerdeService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public LoteVerdeController(LoteVerdeService loteVerdeService) {
		this.loteVerdeService = loteVerdeService;
	}

	// ---------------------------------------------------------------------
	// GET /api/lotes-verdes -> lotes del más antiguo al más nuevo, con su stock en gramos
	// ---------------------------------------------------------------------
	@GetMapping
	public List<LoteVerdeDto> listar() {
		return loteVerdeService.listar();
	}

	// ---------------------------------------------------------------------
	// POST /api/lotes-verdes -> ingresa un lote; la cantidad llega con su unidad (kg, lb, g)
	// Responde 201 con el lote creado
	// ---------------------------------------------------------------------
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public LoteVerdeDto crear(@Valid @RequestBody NuevoLoteVerdeDto datos) {
		return loteVerdeService.crear(datos);
	}
}
