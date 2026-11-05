package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.MembresiaDto;
import com.cafedefinca.backend.dto.NuevaMembresiaDto;
import com.cafedefinca.backend.service.MembresiaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/membresias")
public class MembresiaController {

	private final MembresiaService membresiaService;

	public MembresiaController(MembresiaService membresiaService) {
		this.membresiaService = membresiaService;
	}

	@GetMapping
	public List<MembresiaDto> listar() {
		return membresiaService.listar();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MembresiaDto crear(@Valid @RequestBody NuevaMembresiaDto datos) {
		return membresiaService.crear(datos);
	}

	@DeleteMapping("/{id}")
	public MembresiaDto cancelar(@PathVariable Integer id) {
		return membresiaService.cancelar(id);
	}
}
