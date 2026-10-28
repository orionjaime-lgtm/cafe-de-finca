package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.LoteVerdeDto;
import com.cafedefinca.backend.dto.NuevoLoteVerdeDto;
import com.cafedefinca.backend.entity.LoteCafeVerde;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.entity.Proveedor;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.LoteCafeVerdeRepository;
import com.cafedefinca.backend.repository.OrigenCafeRepository;
import com.cafedefinca.backend.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba el ingreso y el listado de lotes de café verde, con repositories simulados (sin base de datos).
 */
class LoteVerdeServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final LocalDate hoy = LocalDate.of(2026, 10, 28);
	private final Clock relojFijo = Clock.fixed(Instant.parse("2026-10-28T15:00:00Z"), ZoneId.of("UTC"));
	private LoteCafeVerdeRepository loteRepository;
	private OrigenCafeRepository origenRepository;
	private ProveedorRepository proveedorRepository;
	private LoteVerdeService servicio;

	@BeforeEach
	void preparar() {
		OrigenCafe origen = Mockito.mock(OrigenCafe.class);
		Proveedor proveedor = Mockito.mock(Proveedor.class);

		loteRepository = Mockito.mock(LoteCafeVerdeRepository.class);
		origenRepository = Mockito.mock(OrigenCafeRepository.class);
		proveedorRepository = Mockito.mock(ProveedorRepository.class);
		servicio = new LoteVerdeService(loteRepository, origenRepository, proveedorRepository, relojFijo);

		when(origen.getIdOrigen()).thenReturn(1);
		when(origen.getPais()).thenReturn("Colombia");
		when(origen.getRegion()).thenReturn("Huila");
		when(proveedor.getIdProveedor()).thenReturn(2);
		when(proveedor.getRazonSocial()).thenReturn("Exportadora Huila Café S.A.S.");
		when(origenRepository.findById(1)).thenReturn(Optional.of(origen));
		when(proveedorRepository.findById(2)).thenReturn(Optional.of(proveedor));
		when(loteRepository.save(any(LoteCafeVerde.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	// ---------------------------------------------------------------------
	// INGRESO: CONVERSIÓN A GRAMOS
	// ---------------------------------------------------------------------
	@Test
	void unLoteEnKilosSeGuardaEnGramosYElStockArrancaIgualAlPesoInicial() {
		LoteVerdeDto lote = servicio.crear(pedido("LV-HUI-002", "150", "kg", null));

		assertThat(lote.pesoInicialGramos()).isEqualByComparingTo("150000");
		assertThat(lote.stockActualGramos()).isEqualByComparingTo("150000");
		assertThat(lote.origen()).isEqualTo("Colombia Huila");
		assertThat(lote.proveedor()).isEqualTo("Exportadora Huila Café S.A.S.");
	}

	@Test
	void unLoteEnLibrasSeConvierteConTresDecimales() {
		LoteVerdeDto lote = servicio.crear(pedido("LV-HUI-003", "3", "lb", null));

		assertThat(lote.pesoInicialGramos()).isEqualByComparingTo("1360.777");
	}

	@Test
	void laUnidadSeAceptaSinImportarMayusculasNiEspacios() {
		LoteVerdeDto lote = servicio.crear(pedido("LV-HUI-004", "2", " KG ", null));

		assertThat(lote.pesoInicialGramos()).isEqualByComparingTo("2000");
	}

	// ---------------------------------------------------------------------
	// INGRESO: FECHA Y CÓDIGO
	// ---------------------------------------------------------------------
	@Test
	void sinFechaDeIngresoSeUsaLaDeHoy() {
		LoteVerdeDto lote = servicio.crear(pedido("LV-HUI-005", "10", "kg", null));

		assertThat(lote.fechaIngreso()).isEqualTo(hoy);
	}

	@Test
	void unaFechaPasadaSeRespeta() {
		LoteVerdeDto lote = servicio.crear(pedido("LV-HUI-006", "10", "kg", hoy.minusDays(3)));

		assertThat(lote.fechaIngreso()).isEqualTo(hoy.minusDays(3));
	}

	@Test
	void elCodigoSeGuardaSinEspaciosAlRededor() {
		ArgumentCaptor<LoteCafeVerde> guardado = ArgumentCaptor.forClass(LoteCafeVerde.class);

		servicio.crear(pedido("  LV-HUI-007  ", "10", "kg", null));

		verify(loteRepository).save(guardado.capture());
		assertThat(guardado.getValue().getCodigoLote()).isEqualTo("LV-HUI-007");
	}

	// ---------------------------------------------------------------------
	// INGRESO: DATOS INVÁLIDOS (400 y no se guarda nada)
	// ---------------------------------------------------------------------
	@Test
	void unaFechaFuturaSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(pedido("LV-HUI-008", "10", "kg", hoy.plusDays(1))))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("posterior a hoy");
		verify(loteRepository, never()).save(any());
	}

	@Test
	void hoyEsLaFechaLimiteYSeAcepta() {
		LoteVerdeDto lote = servicio.crear(pedido("LV-HUI-009", "10", "kg", hoy));

		assertThat(lote.fechaIngreso()).isEqualTo(hoy);
	}

	@Test
	void unOrigenQueNoExisteSeRechaza() {
		NuevoLoteVerdeDto datos = new NuevoLoteVerdeDto(99, 2, "LV-X-001", new BigDecimal("10"), "kg", 1, null);

		assertThatThrownBy(() -> servicio.crear(datos))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("origen");
		verify(loteRepository, never()).save(any());
	}

	@Test
	void unProveedorQueNoExisteSeRechaza() {
		NuevoLoteVerdeDto datos = new NuevoLoteVerdeDto(1, 99, "LV-X-002", new BigDecimal("10"), "kg", 1, null);

		assertThatThrownBy(() -> servicio.crear(datos))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("proveedor");
		verify(loteRepository, never()).save(any());
	}

	@Test
	void unaUnidadDesconocidaSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(pedido("LV-X-003", "10", "toneladas", null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("Unidad desconocida");
		verify(loteRepository, never()).save(any());
	}

	@Test
	void unaCantidadQueRedondeaACeroGramosSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(pedido("LV-X-004", "0.0001", "g", null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("demasiado pequeña");
		verify(loteRepository, never()).save(any());
	}

	@Test
	void unaCantidadMayorAlMaximoDeLaColumnaSeRechazaYElMaximoExactoSeAcepta() {
		// Borde: 999999999.999 g entra justo; un gramo más ya no
		LoteVerdeDto justo = servicio.crear(pedido("LV-X-005", "999999999.999", "g", null));
		assertThat(justo.pesoInicialGramos()).isEqualByComparingTo("999999999.999");

		assertThatThrownBy(() -> servicio.crear(pedido("LV-X-006", "1000000000", "g", null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("demasiado grande");
	}

	// ---------------------------------------------------------------------
	// LISTADO
	// ---------------------------------------------------------------------
	@Test
	void elListadoTraeElNombreDelProveedorYUnProveedorFaltanteQuedaEnNull() {
		LoteCafeVerde conProveedor = loteGuardado(2);
		LoteCafeVerde sinProveedor = loteGuardado(77);
		Proveedor proveedor = proveedorRepository.findById(2).orElseThrow();
		when(loteRepository.buscarTodosConOrigen()).thenReturn(List.of(conProveedor, sinProveedor));
		when(proveedorRepository.findAll()).thenReturn(List.of(proveedor));

		List<LoteVerdeDto> lotes = servicio.listar();

		assertThat(lotes).hasSize(2);
		assertThat(lotes.get(0).proveedor()).isEqualTo("Exportadora Huila Café S.A.S.");
		assertThat(lotes.get(1).proveedor()).isNull();
	}

	// ---------------------------------------------------------------------
	// AYUDANTES
	// ---------------------------------------------------------------------
	private NuevoLoteVerdeDto pedido(String codigo, String cantidad, String unidad, LocalDate fecha) {
		return new NuevoLoteVerdeDto(1, 2, codigo, new BigDecimal(cantidad), unidad, 5, fecha);
	}

	private LoteCafeVerde loteGuardado(int idProveedor) {
		OrigenCafe origen = origenRepository.findById(1).orElseThrow();
		return new LoteCafeVerde(origen, idProveedor, "LV-L-" + idProveedor, hoy, 1, new BigDecimal("1000"));
	}
}
