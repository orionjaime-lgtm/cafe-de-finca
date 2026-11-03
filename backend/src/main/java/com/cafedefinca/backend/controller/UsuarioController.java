package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.UsuarioDto;
import com.cafedefinca.backend.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping
	public List<UsuarioDto.Resumen> listar() {
		return usuarioService.listar();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioDto.Resumen crear(@Valid @RequestBody UsuarioDto.Nuevo datos) {
		return usuarioService.crear(datos);
	}

	@PatchMapping("/{id}")
	public UsuarioDto.Resumen modificar(@PathVariable Integer id, @RequestBody UsuarioDto.Cambio cambios) {
		return usuarioService.modificar(id, cambios);
	}
}
