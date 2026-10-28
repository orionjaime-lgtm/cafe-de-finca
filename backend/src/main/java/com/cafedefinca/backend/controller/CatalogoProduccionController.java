package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.OrigenDto;
import com.cafedefinca.backend.dto.PerfilTuesteDto;
import com.cafedefinca.backend.dto.ProveedorDto;
import com.cafedefinca.backend.service.CatalogoProduccionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de los catálogos de producción (desplegables de lotes verdes y órdenes de tueste).
 * GET /api/origenes/stock vive aparte, en OrigenController.
 */
@RestController
@RequestMapping("/api")
public class CatalogoProduccionController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final CatalogoProduccionService catalogoProduccionService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public CatalogoProduccionController(CatalogoProduccionService catalogoProduccionService) {
		this.catalogoProduccionService = catalogoProduccionService;
	}

	// ---------------------------------------------------------------------
	// GET /api/proveedores -> proveedores ordenados por razón social
	// ---------------------------------------------------------------------
	@GetMapping("/proveedores")
	public List<ProveedorDto> listarProveedores() {
		return catalogoProduccionService.listarProveedores();
	}

	// ---------------------------------------------------------------------
	// GET /api/origenes -> orígenes ordenados por país y región
	// ---------------------------------------------------------------------
	@GetMapping("/origenes")
	public List<OrigenDto> listarOrigenes() {
		return catalogoProduccionService.listarOrigenes();
	}

	// ---------------------------------------------------------------------
	// GET /api/perfiles-tueste -> Claro, Medio y Oscuro
	// ---------------------------------------------------------------------
	@GetMapping("/perfiles-tueste")
	public List<PerfilTuesteDto> listarPerfilesTueste() {
		return catalogoProduccionService.listarPerfilesTueste();
	}
}
