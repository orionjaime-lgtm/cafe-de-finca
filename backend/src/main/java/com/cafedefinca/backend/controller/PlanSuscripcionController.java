package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.PlanSuscripcionDto;
import com.cafedefinca.backend.service.MembresiaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/planes-suscripcion")
public class PlanSuscripcionController {

	private final MembresiaService membresiaService;

	public PlanSuscripcionController(MembresiaService membresiaService) {
		this.membresiaService = membresiaService;
	}

	@GetMapping
	public List<PlanSuscripcionDto> listar() {
		return membresiaService.listarPlanes();
	}
}
