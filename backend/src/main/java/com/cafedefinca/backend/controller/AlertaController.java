package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.AlertaDto;
import com.cafedefinca.backend.service.AlertaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints del panel de alertas predictivas.
 */
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final AlertaService alertaService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public AlertaController(AlertaService alertaService) {
		this.alertaService = alertaService;
	}

	// ---------------------------------------------------------------------
	// GET /api/alertas -> alertas activas, primero las críticas
	// ---------------------------------------------------------------------
	@GetMapping
	public List<AlertaDto> listar() {
		return alertaService.listarAlertas();
	}
}
