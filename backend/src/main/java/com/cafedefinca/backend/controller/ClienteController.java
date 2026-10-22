package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.ClienteDto;
import com.cafedefinca.backend.service.ClienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de clientes.
 */
@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final ClienteService clienteService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public ClienteController(ClienteService clienteService) {
		this.clienteService = clienteService;
	}

	// ---------------------------------------------------------------------
	// GET /api/clientes            -> todos los clientes
	// GET /api/clientes?tipo=B2B   -> solo mayoristas (o B2C para consumidores finales)
	// ---------------------------------------------------------------------
	@GetMapping
	public List<ClienteDto> listar(@RequestParam(required = false) String tipo) {
		return clienteService.listar(tipo);
	}
}
