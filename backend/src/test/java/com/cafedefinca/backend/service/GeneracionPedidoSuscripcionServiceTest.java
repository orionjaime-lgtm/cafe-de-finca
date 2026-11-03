package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevoPedidoDto;
import com.cafedefinca.backend.dto.PedidoDto;
import com.cafedefinca.backend.dto.ResultadoGeneracionDto.PedidoGenerado;
import com.cafedefinca.backend.entity.CanalVenta;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.HojaRuta;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.entity.Presentacion;
import com.cafedefinca.backend.entity.Suscripcion;
import com.cafedefinca.backend.entity.TipoMolienda;
import com.cafedefinca.backend.enums.EstadoSuscripcion;
import com.cafedefinca.backend.enums.FrecuenciaSuscripcion;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.repository.CanalVentaRepository;
import com.cafedefinca.backend.repository.HojaRutaRepository;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import com.cafedefinca.backend.repository.SuscripcionRepository;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba la generación del pedido de UN abono con repositories y services simulados (sin base de datos):
 * qué lote elige, a qué hoja de ruta lo asigna, cuándo avanza la fecha y cuándo no hace nada.
 * "Ahora" es el miércoles 04/11/2026 a las 15:00.
 */
class GeneracionPedidoSuscripcionServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final Clock relojFijo = Clock.fixed(Instant.parse("2026-11-04T15:00:00Z"), ZoneId.of("UTC"));
	private final LocalDateTime ahora = LocalDateTime.of(2026, 11, 4, 15, 0);
	private final LocalDate hoy = LocalDate.of(2026, 11, 4);
	private SuscripcionRepository suscripcionRepository;
	private PedidoService pedidoService;
	private PedidoRepository pedidoRepository;
	private HojaRutaRepository hojaRutaRepository;
	private LoteCafeTostadoRepository loteRepository;
	private GeneracionPedidoSuscripcionService servicio;
	private Cliente cliente;
	private Presentacion bolsa5k;
	private TipoMolienda filtro;
	private Pedido pedidoGuardado;

	@BeforeEach
	void preparar() {
		CanalVentaRepository canalRepository = Mockito.mock(CanalVentaRepository.class);
		CanalVenta canal = Mockito.mock(CanalVenta.class);
		HojaRuta hoja = Mockito.mock(HojaRuta.class);
		PedidoDto pedidoCreado = new PedidoDto(50, 1, 3, ahora, "PENDIENTE", "PENDIENTE",
				new BigDecimal("240000.00"), BigDecimal.ZERO, BigDecimal.ZERO, List.of(), null);

		suscripcionRepository = Mockito.mock(SuscripcionRepository.class);
		pedidoService = Mockito.mock(PedidoService.class);
		pedidoRepository = Mockito.mock(PedidoRepository.class);
		hojaRutaRepository = Mockito.mock(HojaRutaRepository.class);
		loteRepository = Mockito.mock(LoteCafeTostadoRepository.class);
		servicio = new GeneracionPedidoSuscripcionService(suscripcionRepository, pedidoService, pedidoRepository,
				hojaRutaRepository, canalRepository, loteRepository, relojFijo);

		cliente = Mockito.mock(Cliente.class);
		bolsa5k = Mockito.mock(Presentacion.class);
		filtro = Mockito.mock(TipoMolienda.class);
		pedidoGuardado = Mockito.mock(Pedido.class);
		when(cliente.getIdCliente()).thenReturn(1);
		when(bolsa5k.getIdPresentacion()).thenReturn(3);
		when(bolsa5k.getNombreEnvase()).thenReturn("Bolsa 5kg");
		when(bolsa5k.getPesoGramos()).thenReturn(new BigDecimal("5000"));
		when(filtro.getIdTipoMolienda()).thenReturn(4);
		when(filtro.getNombreMolienda()).thenReturn("Filtro");
		when(canal.getIdCanalVenta()).thenReturn(3);
		when(hoja.getIdHojaRuta()).thenReturn(9);

		when(canalRepository.findByNombreCanal("Mayorista (B2B)")).thenReturn(Optional.of(canal));
		when(pedidoService.crear(any(NuevoPedidoDto.class))).thenReturn(pedidoCreado);
		when(pedidoRepository.findById(50)).thenReturn(Optional.of(pedidoGuardado));
		when(hojaRutaRepository.buscarPorFechaConBloqueo(any(LocalDate.class))).thenReturn(Optional.of(hoja));
		when(pedidoRepository.buscarUltimoOrdenVisita(9)).thenReturn(2);

		// Por defecto hay un lote vendible con 20 kg
		conLotes(lote(10, "20000", hoy.plusDays(20), ahora.minusDays(3), "DISPONIBLE"));
	}

	// ---------------------------------------------------------------------
	// CASO FELIZ: PRECIOS PACTADOS, CLIENTE Y CANAL DEL ABONO
	// ---------------------------------------------------------------------
	@Test
	void creaElPedidoConElClienteElCanalMayoristaYLosPreciosPactados() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		ArgumentCaptor<NuevoPedidoDto> enviado = ArgumentCaptor.forClass(NuevoPedidoDto.class);

		Optional<PedidoGenerado> resultado = servicio.generarPedido(7);

		verify(pedidoService).crear(enviado.capture());
		assertThat(enviado.getValue().clienteId()).isEqualTo(1);
		assertThat(enviado.getValue().canalVentaId()).isEqualTo(3);
		assertThat(enviado.getValue().items()).hasSize(1);
		assertThat(enviado.getValue().items().get(0).presentacionId()).isEqualTo(3);
		assertThat(enviado.getValue().items().get(0).tipoMoliendaId()).isEqualTo(4);
		assertThat(enviado.getValue().items().get(0).cantidadEnvases()).isEqualTo(2);
		assertThat(enviado.getValue().items().get(0).precioUnitario()).isEqualByComparingTo("120000.00");
		assertThat(resultado).isPresent();
		assertThat(resultado.get().pedidoId()).isEqualTo(50);
		assertThat(resultado.get().montoTotal()).isEqualByComparingTo("240000.00");
	}

	// ---------------------------------------------------------------------
	// HOJA DE RUTA Y ORDEN DE VISITA
	// ---------------------------------------------------------------------
	@Test
	void asignaElPedidoALaHojaDeRutaDeLaFechaDeEntregaConElSiguienteOrdenDeVisita() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);

		Optional<PedidoGenerado> resultado = servicio.generarPedido(7);

		// La hoja ya tenía 2 pedidos: este es el tercero en la visita
		verify(hojaRutaRepository).crearSiNoExiste(hoy);
		verify(pedidoGuardado).asignarASuscripcion(7, hoy);
		verify(pedidoGuardado).asignarARuta(9, 3);
		assertThat(resultado.get().hojaRutaId()).isEqualTo(9);
		assertThat(resultado.get().ordenVisita()).isEqualTo(3);
		assertThat(resultado.get().fechaEntrega()).isEqualTo(hoy);
	}

	@Test
	void unaEntregaAtrasadaVaALaHojaDeRutaDeSuFechaYNoDeHoy() {
		LocalDate atrasada = hoy.minusDays(3);
		abono(FrecuenciaSuscripcion.SEMANAL, atrasada);

		servicio.generarPedido(7);

		verify(hojaRutaRepository).crearSiNoExiste(atrasada);
		verify(pedidoGuardado).asignarASuscripcion(7, atrasada);
	}

	// ---------------------------------------------------------------------
	// AVANZAR LA PRÓXIMA FECHA
	// ---------------------------------------------------------------------
	@Test
	void unAbonoSemanalAvanzaSieteDiasDespuesDeGenerar() {
		Suscripcion abono = abono(FrecuenciaSuscripcion.SEMANAL, hoy);

		servicio.generarPedido(7);

		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(LocalDate.of(2026, 11, 11));
	}

	@Test
	void unAbonoMensualAvanzaAlMismoDiaDelMesSiguienteDespuesDeGenerar() {
		Suscripcion abono = abono(FrecuenciaSuscripcion.MENSUAL, hoy);

		servicio.generarPedido(7);

		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(LocalDate.of(2026, 12, 4));
	}

	// ---------------------------------------------------------------------
	// NO DUPLICAR: CORRER DOS VECES NO GENERA DOS PEDIDOS
	// ---------------------------------------------------------------------
	@Test
	void correrloDosVecesSoloGeneraUnPedido() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);

		Optional<PedidoGenerado> primera = servicio.generarPedido(7);
		Optional<PedidoGenerado> segunda = servicio.generarPedido(7);

		assertThat(primera).isPresent();
		assertThat(segunda).isEmpty();
		verify(pedidoService, times(1)).crear(any());
	}

	// ---------------------------------------------------------------------
	// CUÁNDO NO HACE NADA
	// ---------------------------------------------------------------------
	@Test
	void unAbonoPausadoNoGeneraNada() {
		Suscripcion abono = abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		abono.pausar();

		assertThat(servicio.generarPedido(7)).isEmpty();
		verify(pedidoService, never()).crear(any());
		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(hoy);
	}

	@Test
	void unAbonoCanceladoNoGeneraNada() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy).cancelar();

		assertThat(servicio.generarPedido(7)).isEmpty();
		verify(pedidoService, never()).crear(any());
	}

	@Test
	void unAbonoCuyaEntregaEsDeMananaTodaviaNoGenera() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy.plusDays(1));

		assertThat(servicio.generarPedido(7)).isEmpty();
		verify(pedidoService, never()).crear(any());
	}

	@Test
	void unAbonoPasadoDeSuFechaDeFinNoGenera() {
		Suscripcion abono = new Suscripcion(cliente, FrecuenciaSuscripcion.SEMANAL, 3,
				LocalDate.of(2026, 1, 1), hoy.minusDays(1), hoy);
		abono.agregarRenglon(bolsa5k, filtro, 2, new BigDecimal("120000.00"));
		when(suscripcionRepository.buscarPorIdConBloqueo(7)).thenReturn(Optional.of(abono));

		assertThat(servicio.generarPedido(7)).isEmpty();
		verify(pedidoService, never()).crear(any());
	}

	// ---------------------------------------------------------------------
	// SIN STOCK: NO SE GENERA, NO SE AVANZA LA FECHA Y NO SE TOCA NADA
	// ---------------------------------------------------------------------
	@Test
	void sinUnLoteConStockSuficienteLanzaStockInsuficienteYNoAvanzaLaFecha() {
		Suscripcion abono = abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		// El renglón pide 10 kg y el único lote vendible tiene 9,9 kg
		conLotes(lote(10, "9900", hoy.plusDays(20), ahora.minusDays(3), "DISPONIBLE"));

		assertThatThrownBy(() -> servicio.generarPedido(7))
				.isInstanceOf(StockInsuficienteException.class)
				.hasMessageContaining("10000 g")
				.hasMessageContaining("Bolsa 5kg");

		verify(pedidoService, never()).crear(any());
		verify(hojaRutaRepository, never()).crearSiNoExiste(any());
		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(hoy);
		assertThat(abono.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
	}

	@Test
	void sinNingunLoteVendibleTambienLanzaStockInsuficiente() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		conLotes();

		assertThatThrownBy(() -> servicio.generarPedido(7)).isInstanceOf(StockInsuficienteException.class);
	}

	// ---------------------------------------------------------------------
	// ELEGIR EL LOTE: FIFO, SOLO LOS VENDIBLES, SIN OFRECER DOS VECES EL MISMO CAFÉ
	// ---------------------------------------------------------------------
	@Test
	void eligeElLoteMasAntiguoQueAlcanzaSaltandoUnoQueTieneMuyPoco() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		conLotes(
				lote(10, "4000", hoy.plusDays(20), ahora.minusDays(9), "DISPONIBLE"),
				lote(11, "15500", hoy.plusDays(20), ahora.minusDays(8), "DISPONIBLE"),
				lote(12, "20000", hoy.plusDays(20), ahora.minusDays(7), "DISPONIBLE"));

		assertThat(loteElegido()).isEqualTo(11);
	}

	@Test
	void noElegiLotesVencidosEnReposoNiAgotados() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		conLotes(
				lote(10, "50000", hoy.minusDays(1), ahora.minusDays(30), "DISPONIBLE"),
				lote(11, "50000", hoy.plusDays(20), ahora.plusHours(1), "REPOSO"),
				lote(12, "50000", hoy.plusDays(20), ahora.minusDays(5), "AGOTADO"),
				lote(13, "50000", hoy.plusDays(20), ahora.minusDays(5), "VENCIDO"),
				lote(14, "10000", hoy.plusDays(20), ahora.minusDays(5), "DISPONIBLE"));

		assertThat(loteElegido()).isEqualTo(14);
	}

	@Test
	void unLoteQueCaducaHoyTodaviaSePuedeElegir() {
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		conLotes(lote(10, "20000", hoy, ahora.minusDays(30), "DISPONIBLE"));

		assertThat(loteElegido()).isEqualTo(10);
	}

	@Test
	void conVariosRenglonesNoOfreceDosVecesElMismoCafe() {
		// Dos renglones de 10 kg cada uno: el primer lote (15 kg) cubre solo uno, el otro va al segundo lote
		Suscripcion abono = abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		abono.agregarRenglon(bolsa5k, filtro, 2, new BigDecimal("118000.00"));
		conLotes(
				lote(11, "15000", hoy.plusDays(20), ahora.minusDays(8), "DISPONIBLE"),
				lote(12, "20000", hoy.plusDays(20), ahora.minusDays(7), "DISPONIBLE"));
		ArgumentCaptor<NuevoPedidoDto> enviado = ArgumentCaptor.forClass(NuevoPedidoDto.class);

		servicio.generarPedido(7);

		verify(pedidoService).crear(enviado.capture());
		assertThat(enviado.getValue().items()).hasSize(2);
		assertThat(enviado.getValue().items().get(0).loteTostadoId()).isEqualTo(11);
		assertThat(enviado.getValue().items().get(1).loteTostadoId()).isEqualTo(12);
		assertThat(enviado.getValue().items().get(1).precioUnitario()).isEqualByComparingTo("118000.00");
	}

	@Test
	void dosRenglonesChicosPuedenSalirDelMismoLoteSiAlcanza() {
		Suscripcion abono = abono(FrecuenciaSuscripcion.SEMANAL, hoy);
		abono.agregarRenglon(bolsa5k, filtro, 2, new BigDecimal("118000.00"));
		conLotes(lote(11, "20000", hoy.plusDays(20), ahora.minusDays(8), "DISPONIBLE"));
		ArgumentCaptor<NuevoPedidoDto> enviado = ArgumentCaptor.forClass(NuevoPedidoDto.class);

		servicio.generarPedido(7);

		verify(pedidoService).crear(enviado.capture());
		assertThat(enviado.getValue().items()).extracting(item -> item.loteTostadoId()).containsExactly(11, 11);
	}

	// ---------------------------------------------------------------------
	// DATOS QUE FALTAN
	// ---------------------------------------------------------------------
	@Test
	void unaSuscripcionQueNoExisteSeRechaza() {
		when(suscripcionRepository.buscarPorIdConBloqueo(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.generarPedido(99))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessage("No existe la suscripción 99.");
	}

	@Test
	void siNoExisteElCanalMayoristaSeRechazaConUnMensajeClaro() {
		CanalVentaRepository sinCanal = Mockito.mock(CanalVentaRepository.class);
		GeneracionPedidoSuscripcionService sinCanalServicio = new GeneracionPedidoSuscripcionService(
				suscripcionRepository, pedidoService, pedidoRepository, hojaRutaRepository, sinCanal,
				loteRepository, relojFijo);
		abono(FrecuenciaSuscripcion.SEMANAL, hoy);

		assertThatThrownBy(() -> sinCanalServicio.generarPedido(7))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("Mayorista (B2B)");
	}

	// ---------------------------------------------------------------------
	// AYUDANTES
	// ---------------------------------------------------------------------

	// Un abono ya guardado con un renglón de 2 bolsas de 5 kg (10 kg) a $120.000 cada una
	private Suscripcion abono(FrecuenciaSuscripcion frecuencia, LocalDate proximaFecha) {
		Suscripcion abono = new Suscripcion(cliente, frecuencia, frecuencia == FrecuenciaSuscripcion.SEMANAL ? 3 : 4,
				LocalDate.of(2026, 1, 1), null, proximaFecha);
		abono.agregarRenglon(bolsa5k, filtro, 2, new BigDecimal("120000.00"));
		when(suscripcionRepository.buscarPorIdConBloqueo(7)).thenReturn(Optional.of(abono));
		return abono;
	}

	private LoteCafeTostado lote(int id, String stock, LocalDate caducidad, LocalDateTime disponibleDesde, String estado) {
		LoteCafeTostado lote = Mockito.mock(LoteCafeTostado.class);
		when(lote.getIdLoteTostado()).thenReturn(id);
		when(lote.getStockDisponibleGramos()).thenReturn(new BigDecimal(stock));
		when(lote.getFechaCaducidad()).thenReturn(caducidad);
		when(lote.getFechaDisponibleVenta()).thenReturn(disponibleDesde);
		when(lote.getEstadoDisponibilidad()).thenReturn(estado);
		return lote;
	}

	// Los lotes se arman antes de stubbear el repository (Mockito no admite stubbings anidados)
	private void conLotes(LoteCafeTostado... lotes) {
		when(loteRepository.buscarEnInventario()).thenReturn(List.of(lotes));
	}

	// Genera el pedido y devuelve el id del lote que se le asignó al primer renglón
	private Integer loteElegido() {
		ArgumentCaptor<NuevoPedidoDto> enviado = ArgumentCaptor.forClass(NuevoPedidoDto.class);

		servicio.generarPedido(7);

		verify(pedidoService).crear(enviado.capture());
		return enviado.getValue().items().get(0).loteTostadoId();
	}
}
