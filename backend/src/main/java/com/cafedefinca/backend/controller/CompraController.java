package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.CompraDto;
import com.cafedefinca.backend.dto.NuevaCompraDto;
import com.cafedefinca.backend.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compras")
public class CompraController {
	private final CompraService compraService;

	public CompraController(CompraService compraService) {
		this.compraService = compraService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CompraDto procesar(
			@RequestAttribute(name = "sesionClienteId", required = false) Integer clienteId,
			@Valid @RequestBody NuevaCompraDto datos) {
		return compraService.procesar(datos, clienteId);
	}
}
