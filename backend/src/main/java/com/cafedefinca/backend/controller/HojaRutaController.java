package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.HojaRutaDto;
import com.cafedefinca.backend.service.HojaRutaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints de hojas de ruta (el reparto de cada día).
 */
@RestController
@RequestMapping("/api/hojas-ruta")
public class HojaRutaController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final HojaRutaService hojaRutaService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public HojaRutaController(HojaRutaService hojaRutaService) {
		this.hojaRutaService = hojaRutaService;
	}

	// ---------------------------------------------------------------------
	// GET /api/hojas-ruta                    -> todas, de la más reciente a la más antigua
	// GET /api/hojas-ruta?fecha=2026-11-09   -> solo la de ese día (lista vacía si no hay reparto)
	// ---------------------------------------------------------------------
	@GetMapping
	public List<HojaRutaDto> listar(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
		return hojaRutaService.listar(fecha);
	}

	// ---------------------------------------------------------------------
	// GET /api/hojas-ruta/{id} -> una hoja con sus paradas en orden de visita
	// ---------------------------------------------------------------------
	@GetMapping("/{id}")
	public HojaRutaDto obtener(@PathVariable Integer id) {
		return hojaRutaService.obtener(id);
	}
}
