package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.NuevoPedidoDto;
import com.cafedefinca.backend.dto.PedidoDto;
import com.cafedefinca.backend.dto.CambioEstadoPreparacionDto;
import com.cafedefinca.backend.dto.CotizacionPedidoDto;
import com.cafedefinca.backend.dto.CotizarPedidoRequestDto;
import com.cafedefinca.backend.service.PedidoService;
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

	// GET /api/pedidos -> lista los pedidos con sus detalles y factura
	@GetMapping
	public List<PedidoDto> listar() {
		return pedidoService.listar();
	}

	// PATCH /api/pedidos/{id} -> cambia el estado de preparación
	@PatchMapping("/{id}")
	public PedidoDto cambiarEstadoPreparacion(
			@PathVariable Integer id,
			@Valid @RequestBody CambioEstadoPreparacionDto datos) {
		return pedidoService.cambiarEstadoPreparacion(id, datos);
	}

	// POST /api/pedidos/cotizar -> cotiza el pedido y los ahorros de cada plan
	@PostMapping("/cotizar")
	public CotizacionPedidoDto cotizar(@Valid @RequestBody CotizarPedidoRequestDto datos) {
		return pedidoService.cotizar(datos);
	}
}
