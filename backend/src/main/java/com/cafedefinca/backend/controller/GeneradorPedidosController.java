package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.ResultadoGeneracionDto;
import com.cafedefinca.backend.service.GeneradorPedidosService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint para disparar a mano la generación de pedidos de los abonos
 * (la misma que hace la tarea programada todos los días).
 */
@RestController
@RequestMapping("/api/suscripciones")
public class GeneradorPedidosController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final GeneradorPedidosService generadorPedidosService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public GeneradorPedidosController(GeneradorPedidosService generadorPedidosService) {
		this.generadorPedidosService = generadorPedidosService;
	}

	// ---------------------------------------------------------------------
	// POST /api/suscripciones/generar -> genera los pedidos de los abonos que ya les toca
	// Responde 200 con lo generado y lo omitido (con el motivo). Se puede llamar las veces que haga falta:
	// un abono ya generado no vuelve a generar.
	// ---------------------------------------------------------------------
	@PostMapping("/generar")
	public ResultadoGeneracionDto generar() {
		return generadorPedidosService.generar();
	}
}
