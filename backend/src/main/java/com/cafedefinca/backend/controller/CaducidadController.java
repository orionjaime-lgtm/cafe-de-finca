package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.ResumenCaducidadDto;
import com.cafedefinca.backend.service.CaducidadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del panel de caducidad.
 */
@RestController
@RequestMapping("/api/caducidad")
public class CaducidadController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final CaducidadService caducidadService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public CaducidadController(CaducidadService caducidadService) {
		this.caducidadService = caducidadService;
	}

	// ---------------------------------------------------------------------
	// GET /api/caducidad/resumen -> los 3 números de arriba (activos, por vencer, vencidos)
	// ---------------------------------------------------------------------
	@GetMapping("/resumen")
	public ResumenCaducidadDto obtenerResumen() {
		return caducidadService.obtenerResumen();
	}
}
