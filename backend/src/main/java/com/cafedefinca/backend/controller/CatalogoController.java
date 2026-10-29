package com.cafedefinca.backend.controller;

import com.cafedefinca.backend.dto.PresentacionDto;
import com.cafedefinca.backend.dto.TipoMoliendaDto;
import com.cafedefinca.backend.dto.CanalVentaDto;
import com.cafedefinca.backend.dto.MedioPagoDto;
import com.cafedefinca.backend.service.CatalogoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints del catálogo de venta (desplegables del formulario de pedidos).
 */
@RestController
@RequestMapping("/api")
public class CatalogoController {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final CatalogoService catalogoService;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el service)
	// ---------------------------------------------------------------------
	public CatalogoController(CatalogoService catalogoService) {
		this.catalogoService = catalogoService;
	}

	// ---------------------------------------------------------------------
	// GET /api/presentaciones -> envases ordenados del más chico al más grande
	// ---------------------------------------------------------------------
	@GetMapping("/presentaciones")
	public List<PresentacionDto> listarPresentaciones() {
		return catalogoService.listarPresentaciones();
	}

	// ---------------------------------------------------------------------
	// GET /api/tipos-molienda -> tipos de molienda
	// ---------------------------------------------------------------------
	@GetMapping("/tipos-molienda")
	public List<TipoMoliendaDto> listarTiposMolienda() {
		return catalogoService.listarTiposMolienda();
	}

	// ---------------------------------------------------------------------
	// GET /api/canales-venta -> canales disponibles para crear pedidos
	// ---------------------------------------------------------------------
	@GetMapping("/canales-venta")
	public List<CanalVentaDto> listarCanalesVenta() {
		return catalogoService.listarCanalesVenta();
	}

	// ---------------------------------------------------------------------
	// GET /api/medios-pago -> medios disponibles para registrar cobros
	// ---------------------------------------------------------------------
	@GetMapping("/medios-pago")
	public List<MedioPagoDto> listarMediosPago() {
		return catalogoService.listarMediosPago();
	}
}
