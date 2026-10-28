package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.OrigenDto;
import com.cafedefinca.backend.dto.PerfilTuesteDto;
import com.cafedefinca.backend.dto.ProveedorDto;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.entity.PerfilTueste;
import com.cafedefinca.backend.entity.Proveedor;
import com.cafedefinca.backend.repository.OrigenCafeRepository;
import com.cafedefinca.backend.repository.PerfilTuesteRepository;
import com.cafedefinca.backend.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catálogos del lado de producción: proveedores, orígenes y perfiles de tueste.
 * Son datos base que alimentan los desplegables de ingreso de café verde y de órdenes de tueste.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoProduccionService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final ProveedorRepository proveedorRepository;
	private final OrigenCafeRepository origenCafeRepository;
	private final PerfilTuesteRepository perfilTuesteRepository;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories)
	// ---------------------------------------------------------------------
	public CatalogoProduccionService(ProveedorRepository proveedorRepository,
			OrigenCafeRepository origenCafeRepository,
			PerfilTuesteRepository perfilTuesteRepository) {
		this.proveedorRepository = proveedorRepository;
		this.origenCafeRepository = origenCafeRepository;
		this.perfilTuesteRepository = perfilTuesteRepository;
	}

	// ---------------------------------------------------------------------
	// PROVEEDORES
	// ---------------------------------------------------------------------
	public List<ProveedorDto> listarProveedores() {
		List<Proveedor> proveedores = proveedorRepository.findAllByOrderByRazonSocialAsc();
		return proveedores.stream().map(this::aDto).toList();
	}

	// ---------------------------------------------------------------------
	// ORÍGENES
	// ---------------------------------------------------------------------
	public List<OrigenDto> listarOrigenes() {
		List<OrigenCafe> origenes = origenCafeRepository.findAllByOrderByPaisAscRegionAsc();
		return origenes.stream().map(this::aDto).toList();
	}

	// ---------------------------------------------------------------------
	// PERFILES DE TUESTE
	// ---------------------------------------------------------------------
	public List<PerfilTuesteDto> listarPerfilesTueste() {
		List<PerfilTueste> perfiles = perfilTuesteRepository.findAllByOrderByIdPerfilTuesteAsc();
		return perfiles.stream().map(this::aDto).toList();
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private ProveedorDto aDto(Proveedor proveedor) {
		return new ProveedorDto(
				proveedor.getIdProveedor(),
				proveedor.getCuit(),
				proveedor.getRazonSocial(),
				proveedor.getPaisOrigen(),
				proveedor.getTelefonoEmail());
	}

	private OrigenDto aDto(OrigenCafe origen) {
		return new OrigenDto(
				origen.getIdOrigen(),
				origen.getPais(),
				origen.getRegion(),
				origen.getVariedad(),
				origen.getAlturaMsnm(),
				origen.getNotasCata(),
				origen.getStockMinimoAlertaGramos());
	}

	private PerfilTuesteDto aDto(PerfilTueste perfil) {
		return new PerfilTuesteDto(
				perfil.getIdPerfilTueste(),
				perfil.getNombrePerfil(),
				perfil.getTempObjetivoC(),
				perfil.getTiempoTuesteMin(),
				perfil.getMermaEsperadaPct(),
				perfil.getHorasReposoMinimas());
	}
}
