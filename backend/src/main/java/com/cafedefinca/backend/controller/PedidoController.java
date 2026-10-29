package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.NuevoPedidoDto;
import com.cafedefinca.backend.dto.PedidoDto;
import com.cafedefinca.backend.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de pedidos.
 */
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final PedidoService pedidoService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR
	// ---------------------------------------------------------------------
	public PedidoController(PedidoService pedidoService) {
		this.pedidoService = pedidoService;
	}

	// ---------------------------------------------------------------------
	// POST /api/pedidos -> crea pedido, detalles y factura B2B
	// ---------------------------------------------------------------------
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PedidoDto crear(@Valid @RequestBody NuevoPedidoDto datos) {
		return pedidoService.crear(datos);
	}
}
