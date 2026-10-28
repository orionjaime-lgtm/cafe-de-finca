package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.NuevaOrdenTuesteDto;
import com.cafedefinca.backend.dto.OrdenTuesteDto;
import com.cafedefinca.backend.service.OrdenTuesteService;
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
 * Endpoints de órdenes de tueste.
 */
@RestController
@RequestMapping("/api/ordenes-tueste")
public class OrdenTuesteController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final OrdenTuesteService ordenTuesteService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public OrdenTuesteController(OrdenTuesteService ordenTuesteService) {
		this.ordenTuesteService = ordenTuesteService;
	}

	// ---------------------------------------------------------------------
	// GET /api/ordenes-tueste -> órdenes de la más reciente a la más antigua, con su lote tostado
	// ---------------------------------------------------------------------
	@GetMapping
	public List<OrdenTuesteDto> listar() {
		return ordenTuesteService.listar();
	}

	// ---------------------------------------------------------------------
	// POST /api/ordenes-tueste -> registra un tueste: descuenta el verde, crea la orden y el lote tostado
	// Responde 201 con la orden creada; 409 si el café verde no alcanza
	// ---------------------------------------------------------------------
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrdenTuesteDto crear(@Valid @RequestBody NuevaOrdenTuesteDto datos) {
		return ordenTuesteService.crear(datos);
	}
}
