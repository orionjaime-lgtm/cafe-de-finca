package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.CobroB2bDto;
import com.cafedefinca.backend.dto.CobroB2cDto;
import com.cafedefinca.backend.service.CobroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cobros")
public class CobroController {

	private final CobroService cobroService;

	public CobroController(CobroService cobroService) {
		this.cobroService = cobroService;
	}

	@GetMapping("/b2b")
	public List<CobroB2bDto> listarB2b() {
		return cobroService.listarB2b();
	}

	@GetMapping("/b2c")
	public List<CobroB2cDto> listarB2c() {
		return cobroService.listarB2c();
	}
}
