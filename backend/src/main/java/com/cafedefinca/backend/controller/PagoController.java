package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.NuevoPagoDto;
import com.cafedefinca.backend.dto.PagoDto;
import com.cafedefinca.backend.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

	private final PagoService pagoService;

	public PagoController(PagoService pagoService) {
		this.pagoService = pagoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PagoDto registrar(
			@RequestAttribute(name = "sesionClienteId", required = false) Integer clienteId,
			@Valid @RequestBody NuevoPagoDto datos) {
		return pagoService.registrar(datos, clienteId);
	}
}
