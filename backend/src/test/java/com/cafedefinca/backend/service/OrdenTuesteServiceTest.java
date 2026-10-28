package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevaOrdenTuesteDto;
import com.cafedefinca.backend.dto.OrdenTuesteDto;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.entity.LoteCafeVerde;
import com.cafedefinca.backend.entity.OrdenTueste;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.entity.PerfilTueste;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import com.cafedefinca.backend.repository.LoteCafeVerdeRepository;
import com.cafedefinca.backend.repository.OrdenTuesteRepository;
import com.cafedefinca.backend.repository.PerfilTuesteRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
 * Prueba el registro de un tueste con repositories simulados (sin base de datos):
 * descuento de stock, fechas del lote tostado, código LT-xxxx y que un error no deje nada a medias.
 */
class OrdenTuesteServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------

	// Reloj fijo: "ahora" es el 28/10/2026 a las 15:00
	private final Clock relojFijo = Clock.fixed(Instant.parse("2026-10-28T15:00:00Z"), ZoneId.of("UTC"));
	private OrdenTuesteRepository ordenRepository;
	private LoteCafeVerdeRepository verdeRepository;
	private PerfilTuesteRepository perfilRepository;
	private LoteCafeTostadoRepository tostadoRepository;
	private OrdenTuesteService servicio;
	private LoteCafeVerde loteVerde;
	private PerfilTueste perfil;

	@BeforeEach
	void preparar() {
		OrigenCafe origen = Mockito.mock(OrigenCafe.class);

		ordenRepository = Mockito.mock(OrdenTuesteRepository.class);
		verdeRepository = Mockito.mock(LoteCafeVerdeRepository.class);
		perfilRepository = Mockito.mock(PerfilTuesteRepository.class);
		tostadoRepository = Mockito.mock(LoteCafeTostadoRepository.class);
		servicio = new OrdenTuesteService(ordenRepository, verdeRepository, perfilRepository, tostadoRepository,
				Mockito.mock(EntityManager.class), relojFijo);

		when(origen.getPais()).thenReturn("Colombia");
		when(origen.getRegion()).thenReturn("Huila");

		// Lote verde con 10 kg y perfil Medio: 12 minutos de tueste y 48 horas de reposo
		loteVerde = new LoteCafeVerde(origen, 1, "LV-HUI-001", LocalDate.of(2026, 10, 1), 2, new BigDecimal("10000"));
		perfil = Mockito.mock(PerfilTueste.class);
		when(perfil.getIdPerfilTueste()).thenReturn(2);
		when(perfil.getNombrePerfil()).thenReturn("Tueste Medio");
		when(perfil.getTiempoTuesteMin()).thenReturn(12);
		when(perfil.getHorasReposoMinimas()).thenReturn(48);

		when(verdeRepository.buscarPorIdConBloqueo(1)).thenReturn(Optional.of(loteVerde));
		when(perfilRepository.findById(2)).thenReturn(Optional.of(perfil));
		when(ordenRepository.saveAndFlush(any(OrdenTueste.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
		when(tostadoRepository.save(any(LoteCafeTostado.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
		when(tostadoRepository.buscarNumeroMaximoDeCodigo()).thenReturn(1091L);
	}

	// ---------------------------------------------------------------------
	// CASO FELIZ: DESCUENTA, CREA LA ORDEN Y EL LOTE TOSTADO
	// ---------------------------------------------------------------------
	@Test
	void unTuesteDescuentaElVerdeYCreaLaOrdenYElLoteTostado() {
		OrdenTuesteDto orden = servicio.crear(pedido("5", "4.25", "kg", " Marta "));

		assertThat(loteVerde.getStockActualGramos()).isEqualByComparingTo("5000");
		assertThat(orden.pesoVerdeUsadoGramos()).isEqualByComparingTo("5000");
		assertThat(orden.pesoTostadoObtenidoGramos()).isEqualByComparingTo("4250");
		assertThat(orden.estadoOrden()).isEqualTo("COMPLETADO");
		assertThat(orden.operadorTostador()).isEqualTo("Marta");
		assertThat(orden.origen()).isEqualTo("Colombia Huila");
		assertThat(orden.perfilTueste()).isEqualTo("Tueste Medio");
		verify(ordenRepository).saveAndFlush(any(OrdenTueste.class));
		verify(tostadoRepository).save(any(LoteCafeTostado.class));
	}

	@Test
	void elLoteTostadoNaceEnReposoConTodoElCafeDisponible() {
		ArgumentCaptor<LoteCafeTostado> guardado = ArgumentCaptor.forClass(LoteCafeTostado.class);

		servicio.crear(pedido("5", "4.25", "kg", null));

		verify(tostadoRepository).save(guardado.capture());
		assertThat(guardado.getValue().getEstadoDisponibilidad()).isEqualTo("REPOSO");
		assertThat(guardado.getValue().getPesoInicialGramos()).isEqualByComparingTo("4250");
		assertThat(guardado.getValue().getStockDisponibleGramos()).isEqualByComparingTo("4250");
		assertThat(guardado.getValue().getStockReservadoGramos()).isEqualByComparingTo("0");
	}

	// ---------------------------------------------------------------------
	// FECHAS DEL LOTE TOSTADO
	// ---------------------------------------------------------------------
	@Test
	void lasFechasSalenDelRelojElTiempoDeTuesteElReposoYLos30Dias() {
		ArgumentCaptor<LoteCafeTostado> guardado = ArgumentCaptor.forClass(LoteCafeTostado.class);

		OrdenTuesteDto orden = servicio.crear(pedido("5", "4", "kg", null));

		verify(tostadoRepository).save(guardado.capture());
		// Tueste 28/10 15:00 -> fin 15:12 -> disponible 48 horas después -> caduca 30 días después del tueste
		assertThat(orden.fechaHoraTueste()).isEqualTo(LocalDateTime.of(2026, 10, 28, 15, 0));
		assertThat(guardado.getValue().getFechaHoraFinTueste()).isEqualTo(LocalDateTime.of(2026, 10, 28, 15, 12));
		assertThat(guardado.getValue().getFechaDisponibleVenta()).isEqualTo(LocalDateTime.of(2026, 10, 30, 15, 12));
		assertThat(guardado.getValue().getFechaCaducidad()).isEqualTo(LocalDate.of(2026, 11, 27));
	}

	@Test
	void unPerfilSinHorasDeReposoQuedaDisponibleAlTerminarElTueste() {
		ArgumentCaptor<LoteCafeTostado> guardado = ArgumentCaptor.forClass(LoteCafeTostado.class);
		when(perfil.getHorasReposoMinimas()).thenReturn(null);

		servicio.crear(pedido("5", "4", "kg", null));

		verify(tostadoRepository).save(guardado.capture());
		assertThat(guardado.getValue().getFechaDisponibleVenta()).isEqualTo(LocalDateTime.of(2026, 10, 28, 15, 12));
	}

	// ---------------------------------------------------------------------
	// CÓDIGO LT-xxxx
	// ---------------------------------------------------------------------
	@Test
	void elCodigoEsElSiguienteAlMayorUsado() {
		OrdenTuesteDto orden = servicio.crear(pedido("5", "4", "kg", null));

		assertThat(orden.codigoLoteTostado()).isEqualTo("LT-1092");
		verify(tostadoRepository).bloquearGeneracionDeCodigo(7300001L);
	}

	@Test
	void sinNingunCodigoPrevioElPrimeroEsLT1001() {
		when(tostadoRepository.buscarNumeroMaximoDeCodigo()).thenReturn(0L);

		assertThat(servicio.crear(pedido("5", "4", "kg", null)).codigoLoteTostado()).isEqualTo("LT-1001");
	}

	// ---------------------------------------------------------------------
	// STOCK: LOS BORDES
	// ---------------------------------------------------------------------
	@Test
	void usarTodoElStockJustoSeAceptaYDejaElLoteEnCero() {
		servicio.crear(pedido("10", "8", "kg", null));

		assertThat(loteVerde.getStockActualGramos()).isEqualByComparingTo("0");
	}

	@Test
	void sinStockSuficienteLanzaStockInsuficienteYNoSeEscribeNada() {
		assertThatThrownBy(() -> servicio.crear(pedido("10.001", "8", "kg", null)))
				.isInstanceOf(StockInsuficienteException.class)
				.hasMessage("Stock insuficiente: hay 10000 g disponibles y se pidieron 10001 g.");

		assertThat(loteVerde.getStockActualGramos()).isEqualByComparingTo("10000");
		verify(ordenRepository, never()).saveAndFlush(any());
		verify(tostadoRepository, never()).save(any());
	}

	// ---------------------------------------------------------------------
	// DATOS INVÁLIDOS (400 y no se descuenta nada)
	// ---------------------------------------------------------------------
	@Test
	void elTostadoNoPuedePesarMasQueElVerde() {
		assertThatThrownBy(() -> servicio.crear(pedido("5", "5.001", "kg", null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("no puede pesar más que el verde");

		assertThat(loteVerde.getStockActualGramos()).isEqualByComparingTo("10000");
		verify(ordenRepository, never()).saveAndFlush(any());
	}

	@Test
	void elTostadoIgualAlVerdeSeAceptaPorqueLaMermaPuedeSerCero() {
		OrdenTuesteDto orden = servicio.crear(pedido("5", "5", "kg", null));

		assertThat(orden.pesoTostadoObtenidoGramos()).isEqualByComparingTo("5000");
	}

	@Test
	void unLoteVerdeQueNoExisteSeRechaza() {
		NuevaOrdenTuesteDto datos = new NuevaOrdenTuesteDto(99, 2, new BigDecimal("5"), new BigDecimal("4"), "kg", null);

		assertThatThrownBy(() -> servicio.crear(datos))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("lote verde");
	}

	@Test
	void unPerfilQueNoExisteSeRechazaYNoSeDescuentaNada() {
		NuevaOrdenTuesteDto datos = new NuevaOrdenTuesteDto(1, 99, new BigDecimal("5"), new BigDecimal("4"), "kg", null);

		assertThatThrownBy(() -> servicio.crear(datos))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("perfil de tueste");
		assertThat(loteVerde.getStockActualGramos()).isEqualByComparingTo("10000");
	}

	@Test
	void unaUnidadDesconocidaSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(pedido("5", "4", "toneladas", null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("Unidad desconocida");
		assertThat(loteVerde.getStockActualGramos()).isEqualByComparingTo("10000");
	}

	@Test
	void lasLibrasSeConviertenAGramos() {
		OrdenTuesteDto orden = servicio.crear(pedido("10", "8", "lb", null));

		// 10 lb = 4535.924 g (se redondea a 3 decimales)
		assertThat(orden.pesoVerdeUsadoGramos()).isEqualByComparingTo("4535.924");
		assertThat(loteVerde.getStockActualGramos()).isEqualByComparingTo("5464.076");
	}

	@Test
	void unOperadorEnBlancoSeGuardaComoNull() {
		assertThat(servicio.crear(pedido("5", "4", "kg", "   ")).operadorTostador()).isNull();
	}

	// ---------------------------------------------------------------------
	// LISTADO
	// ---------------------------------------------------------------------
	@Test
	void sinOrdenesElListadoEsVacioYNoConsultaLotesTostados() {
		when(ordenRepository.buscarTodasConDetalle()).thenReturn(List.of());

		assertThat(servicio.listar()).isEmpty();
		verify(tostadoRepository, never()).buscarPorOrdenes(any());
	}

	@Test
	void elListadoUneCadaOrdenConSuLoteTostado() {
		OrdenTueste orden = Mockito.mock(OrdenTueste.class);
		LoteCafeTostado tostado = Mockito.mock(LoteCafeTostado.class);
		when(orden.getIdOrdenTueste()).thenReturn(7);
		when(orden.getLoteVerde()).thenReturn(loteVerde);
		when(orden.getPerfilTueste()).thenReturn(perfil);
		when(tostado.getOrdenTueste()).thenReturn(orden);
		when(tostado.getCodigoLoteTostado()).thenReturn("LT-1050");
		when(ordenRepository.buscarTodasConDetalle()).thenReturn(List.of(orden));
		when(tostadoRepository.buscarPorOrdenes(List.of(7))).thenReturn(List.of(tostado));

		List<OrdenTuesteDto> ordenes = servicio.listar();

		assertThat(ordenes).hasSize(1);
		assertThat(ordenes.get(0).codigoLoteTostado()).isEqualTo("LT-1050");
		assertThat(ordenes.get(0).codigoLoteVerde()).isEqualTo("LV-HUI-001");
	}

	// ---------------------------------------------------------------------
	// AYUDANTE
	// ---------------------------------------------------------------------
	private NuevaOrdenTuesteDto pedido(String verde, String tostado, String unidad, String operador) {
		return new NuevaOrdenTuesteDto(1, 2, new BigDecimal(verde), new BigDecimal(tostado), unidad, operador);
	}
}
