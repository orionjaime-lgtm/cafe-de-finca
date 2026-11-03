package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.AuthDto;
import com.cafedefinca.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public AuthDto.Sesion iniciarSesion(@Valid @RequestBody AuthDto.Login datos) {
		return authService.iniciarSesion(datos);
	}

	@PostMapping("/registro")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthDto.Sesion registrar(@Valid @RequestBody AuthDto.Registro datos) {
		return authService.registrar(datos);
	}
}
