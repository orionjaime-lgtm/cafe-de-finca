package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.LoteTostadoDto;
import com.cafedefinca.backend.service.LoteTostadoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de lotes de café tostado.
 */
@RestController
@RequestMapping("/api/lotes-tostados")
public class LoteTostadoController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final LoteTostadoService loteTostadoService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public LoteTostadoController(LoteTostadoService loteTostadoService) {
		this.loteTostadoService = loteTostadoService;
	}

	// ---------------------------------------------------------------------
	// GET /api/lotes-tostados                       -> lotes en inventario (panel de caducidad)
	// GET /api/lotes-tostados?soloDisponibles=true  -> solo los que hoy se pueden vender ("Café disponible")
	// ---------------------------------------------------------------------
	@GetMapping
	public List<LoteTostadoDto> listar(@RequestParam(defaultValue = "false") boolean soloDisponibles) {
		return loteTostadoService.listar(soloDisponibles);
	}
}
