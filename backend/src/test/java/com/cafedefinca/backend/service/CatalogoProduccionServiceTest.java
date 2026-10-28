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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Prueba que los catálogos de producción pasen bien de entidad a DTO (sin base de datos).
 */
class CatalogoProduccionServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private ProveedorRepository proveedorRepository;
	private OrigenCafeRepository origenRepository;
	private PerfilTuesteRepository perfilRepository;
	private CatalogoProduccionService servicio;

	@BeforeEach
	void preparar() {
		proveedorRepository = Mockito.mock(ProveedorRepository.class);
		origenRepository = Mockito.mock(OrigenCafeRepository.class);
		perfilRepository = Mockito.mock(PerfilTuesteRepository.class);
		servicio = new CatalogoProduccionService(proveedorRepository, origenRepository, perfilRepository);
	}

	// ---------------------------------------------------------------------
	// PROVEEDORES
	// ---------------------------------------------------------------------
	@Test
	void losProveedoresSeConviertenADto() {
		Proveedor proveedor = Mockito.mock(Proveedor.class);
		when(proveedor.getIdProveedor()).thenReturn(1);
		when(proveedor.getCuit()).thenReturn("30-71000001-9");
		when(proveedor.getRazonSocial()).thenReturn("Exportadora Huila Café S.A.S.");
		when(proveedor.getPaisOrigen()).thenReturn("Colombia");
		when(proveedor.getTelefonoEmail()).thenReturn("ventas@huilacafe.example");
		when(proveedorRepository.findAllByOrderByRazonSocialAsc()).thenReturn(List.of(proveedor));

		List<ProveedorDto> proveedores = servicio.listarProveedores();

		assertThat(proveedores).containsExactly(new ProveedorDto(
				1, "30-71000001-9", "Exportadora Huila Café S.A.S.", "Colombia", "ventas@huilacafe.example"));
	}

	// ---------------------------------------------------------------------
	// ORÍGENES
	// ---------------------------------------------------------------------
	@Test
	void losOrigenesSeConviertenADtoConSuMinimoEnGramos() {
		OrigenCafe origen = Mockito.mock(OrigenCafe.class);
		when(origen.getIdOrigen()).thenReturn(1);
		when(origen.getPais()).thenReturn("Colombia");
		when(origen.getRegion()).thenReturn("Huila");
		when(origen.getVariedad()).thenReturn("Caturra");
		when(origen.getAlturaMsnm()).thenReturn(1750);
		when(origen.getNotasCata()).thenReturn("Caramelo");
		when(origen.getStockMinimoAlertaGramos()).thenReturn(new BigDecimal("5000"));
		when(origenRepository.findAllByOrderByPaisAscRegionAsc()).thenReturn(List.of(origen));

		List<OrigenDto> origenes = servicio.listarOrigenes();

		assertThat(origenes).containsExactly(new OrigenDto(
				1, "Colombia", "Huila", "Caturra", 1750, "Caramelo", new BigDecimal("5000")));
	}

	// ---------------------------------------------------------------------
	// PERFILES DE TUESTE
	// ---------------------------------------------------------------------
	@Test
	void losPerfilesDeTuesteSeConviertenADto() {
		PerfilTueste perfil = Mockito.mock(PerfilTueste.class);
		when(perfil.getIdPerfilTueste()).thenReturn(2);
		when(perfil.getNombrePerfil()).thenReturn("Tueste Medio");
		when(perfil.getTempObjetivoC()).thenReturn(new BigDecimal("205.00"));
		when(perfil.getTiempoTuesteMin()).thenReturn(12);
		when(perfil.getMermaEsperadaPct()).thenReturn(new BigDecimal("15.00"));
		when(perfil.getHorasReposoMinimas()).thenReturn(48);
		when(perfilRepository.findAllByOrderByIdPerfilTuesteAsc()).thenReturn(List.of(perfil));

		List<PerfilTuesteDto> perfiles = servicio.listarPerfilesTueste();

		assertThat(perfiles).containsExactly(new PerfilTuesteDto(
				2, "Tueste Medio", new BigDecimal("205.00"), 12, new BigDecimal("15.00"), 48));
	}
}
