package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.CambioEstadoSuscripcionDto;
import com.cafedefinca.backend.dto.NuevaSuscripcionDto;
import com.cafedefinca.backend.dto.SuscripcionDto;
import com.cafedefinca.backend.service.SuscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de abonos recurrentes (suscripciones).
 */
@RestController
@RequestMapping("/api/suscripciones")
public class SuscripcionController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final SuscripcionService suscripcionService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public SuscripcionController(SuscripcionService suscripcionService) {
		this.suscripcionService = suscripcionService;
	}

	// ---------------------------------------------------------------------
	// GET /api/suscripciones -> todos los abonos con sus renglones
	// ---------------------------------------------------------------------
	@GetMapping
	public List<SuscripcionDto> listar() {
		return suscripcionService.listar();
	}

	// ---------------------------------------------------------------------
	// POST /api/suscripciones -> crea un abono (solo clientes B2B). Responde 201.
	// ---------------------------------------------------------------------
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SuscripcionDto crear(@Valid @RequestBody NuevaSuscripcionDto datos) {
		return suscripcionService.crear(datos);
	}

	// ---------------------------------------------------------------------
	// PATCH /api/suscripciones/{id} -> { "estado": "PAUSADA" | "ACTIVA" | "CANCELADA" }
	// Pausar, reactivar o cancelar. Un cambio no permitido responde 409.
	// ---------------------------------------------------------------------
	@PatchMapping("/{id}")
	public SuscripcionDto cambiarEstado(@PathVariable Integer id, @Valid @RequestBody CambioEstadoSuscripcionDto datos) {
		return suscripcionService.cambiarEstado(id, datos);
	}
}
