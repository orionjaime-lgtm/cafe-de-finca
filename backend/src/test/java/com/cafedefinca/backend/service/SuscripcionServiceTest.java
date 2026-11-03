package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.CambioEstadoSuscripcionDto;
import com.cafedefinca.backend.dto.NuevaSuscripcionDto;
import com.cafedefinca.backend.dto.NuevaSuscripcionItemDto;
import com.cafedefinca.backend.dto.SuscripcionDto;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.Presentacion;
import com.cafedefinca.backend.entity.Suscripcion;
import com.cafedefinca.backend.entity.TipoMolienda;
import com.cafedefinca.backend.enums.FrecuenciaSuscripcion;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.PresentacionRepository;
import com.cafedefinca.backend.repository.SuscripcionRepository;
import com.cafedefinca.backend.repository.TipoMoliendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
 * Prueba el alta y el cambio de estado de los abonos con repositories simulados (sin base de datos).
 * "Hoy" es el miércoles 04/11/2026.
 */
class SuscripcionServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final Clock relojFijo = Clock.fixed(Instant.parse("2026-11-04T15:00:00Z"), ZoneId.of("UTC"));
	private SuscripcionRepository suscripcionRepository;
	private ClienteRepository clienteRepository;
	private PresentacionRepository presentacionRepository;
	private TipoMoliendaRepository tipoMoliendaRepository;
	private SuscripcionService servicio;
	private Cliente hotel;

	@BeforeEach
	void preparar() {
		Cliente consumidor = Mockito.mock(Cliente.class);
		Presentacion bolsa5k = Mockito.mock(Presentacion.class);
		TipoMolienda filtro = Mockito.mock(TipoMolienda.class);

		suscripcionRepository = Mockito.mock(SuscripcionRepository.class);
		clienteRepository = Mockito.mock(ClienteRepository.class);
		presentacionRepository = Mockito.mock(PresentacionRepository.class);
		tipoMoliendaRepository = Mockito.mock(TipoMoliendaRepository.class);
		servicio = new SuscripcionService(suscripcionRepository, clienteRepository, presentacionRepository,
				tipoMoliendaRepository, relojFijo);

		hotel = Mockito.mock(Cliente.class);
		when(hotel.getIdCliente()).thenReturn(1);
		when(hotel.getNombreRazonSocial()).thenReturn("Hotel Andino");
		when(hotel.getTipoCliente()).thenReturn("B2B");
		when(consumidor.getTipoCliente()).thenReturn("B2C");
		when(bolsa5k.getIdPresentacion()).thenReturn(3);
		when(bolsa5k.getNombreEnvase()).thenReturn("Bolsa 5kg");
		when(filtro.getIdTipoMolienda()).thenReturn(4);
		when(filtro.getNombreMolienda()).thenReturn("Filtro");

		when(clienteRepository.findById(1)).thenReturn(Optional.of(hotel));
		when(clienteRepository.findById(2)).thenReturn(Optional.of(consumidor));
		when(presentacionRepository.findById(3)).thenReturn(Optional.of(bolsa5k));
		when(tipoMoliendaRepository.findById(4)).thenReturn(Optional.of(filtro));
		when(suscripcionRepository.save(any(Suscripcion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	// ---------------------------------------------------------------------
	// CREAR: LA PRIMERA ENTREGA
	// ---------------------------------------------------------------------
	@Test
	void unAbonoSemanalDeLosLunesEmpiezaElLunesSiguiente() {
		SuscripcionDto abono = servicio.crear(pedido(1, "SEMANAL", 1, null, null));

		assertThat(abono.estado()).isEqualTo("ACTIVA");
		assertThat(abono.frecuencia()).isEqualTo("SEMANAL");
		assertThat(abono.cliente()).isEqualTo("Hotel Andino");
		assertThat(abono.proximaFechaGeneracion()).isEqualTo(LocalDate.of(2026, 11, 9));
		assertThat(abono.fechaInicio()).isEqualTo(LocalDate.of(2026, 11, 4));
	}

	@Test
	void unAbonoMensualDelDiaDeHoyEmpiezaHoy() {
		SuscripcionDto abono = servicio.crear(pedido(1, "MENSUAL", 4, null, null));

		assertThat(abono.proximaFechaGeneracion()).isEqualTo(LocalDate.of(2026, 11, 4));
	}

	@Test
	void unaFechaDeInicioPasadaNoGeneraEntregasPasadas() {
		SuscripcionDto abono = servicio.crear(pedido(1, "MENSUAL", 10, LocalDate.of(2026, 1, 1), null));

		assertThat(abono.proximaFechaGeneracion()).isEqualTo(LocalDate.of(2026, 11, 10));
		assertThat(abono.fechaInicio()).isEqualTo(LocalDate.of(2026, 1, 1));
	}

	@Test
	void unaFechaDeInicioFuturaEmpiezaDesdeEsaFecha() {
		// 01/01/2027 es viernes: el primer miércoles (día 3) es el 06/01
		SuscripcionDto abono = servicio.crear(pedido(1, "SEMANAL", 3, LocalDate.of(2027, 1, 1), null));

		assertThat(abono.proximaFechaGeneracion()).isEqualTo(LocalDate.of(2027, 1, 6));
	}

	@Test
	void elAbonoGuardaSusRenglonesConElPrecioPactado() {
		SuscripcionDto abono = servicio.crear(pedido(1, "SEMANAL", 1, null, null));

		assertThat(abono.items()).hasSize(1);
		assertThat(abono.items().get(0).presentacion()).isEqualTo("Bolsa 5kg");
		assertThat(abono.items().get(0).tipoMolienda()).isEqualTo("Filtro");
		assertThat(abono.items().get(0).cantidadEnvases()).isEqualTo(2);
		assertThat(abono.items().get(0).precioUnitario()).isEqualByComparingTo("120000.00");
	}

	// ---------------------------------------------------------------------
	// CREAR: DATOS INVÁLIDOS (400 y no se guarda nada)
	// ---------------------------------------------------------------------
	@Test
	void soloLosClientesB2BPuedenTenerUnAbono() {
		assertThatThrownBy(() -> servicio.crear(pedido(2, "SEMANAL", 1, null, null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessage("Solo los clientes B2B pueden tener un abono recurrente.");
		verify(suscripcionRepository, never()).save(any());
	}

	@Test
	void unClienteQueNoExisteSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(pedido(99, "SEMANAL", 1, null, null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("No existe el cliente");
	}

	@Test
	void unaFrecuenciaDesconocidaSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(pedido(1, "DIARIA", 1, null, null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("Frecuencia desconocida");
	}

	@Test
	void elDiaDeEntregaSemanalVaDe1A7() {
		assertThatThrownBy(() -> servicio.crear(pedido(1, "SEMANAL", 8, null, null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("va de 1 a 7");
		assertThatThrownBy(() -> servicio.crear(pedido(1, "SEMANAL", 0, null, null)))
				.isInstanceOf(SolicitudInvalidaException.class);
	}

	@Test
	void elDiaDeEntregaMensualVaDe1A28() {
		assertThatThrownBy(() -> servicio.crear(pedido(1, "MENSUAL", 29, null, null)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("va de 1 a 28");
		assertThat(servicio.crear(pedido(1, "MENSUAL", 28, null, null)).diaEntrega()).isEqualTo(28);
	}

	@Test
	void laFechaDeFinNoPuedeQuedarAntesDeLaPrimeraEntrega() {
		// La primera entrega es el lunes 09/11/2026
		assertThatThrownBy(() -> servicio.crear(pedido(1, "SEMANAL", 1, null, LocalDate.of(2026, 11, 8))))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("anterior a la primera entrega");
		verify(suscripcionRepository, never()).save(any());
	}

	@Test
	void laFechaDeFinIgualALaPrimeraEntregaSeAcepta() {
		SuscripcionDto abono = servicio.crear(pedido(1, "SEMANAL", 1, null, LocalDate.of(2026, 11, 9)));

		assertThat(abono.fechaFin()).isEqualTo(LocalDate.of(2026, 11, 9));
	}

	@Test
	void unaPresentacionQueNoExisteSeRechazaYNoSeGuarda() {
		NuevaSuscripcionDto datos = new NuevaSuscripcionDto(1, "SEMANAL", 1, null, null,
				List.of(new NuevaSuscripcionItemDto(99, 4, 1, new BigDecimal("100"))));

		assertThatThrownBy(() -> servicio.crear(datos))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("No existe la presentación");
		verify(suscripcionRepository, never()).save(any());
	}

	@Test
	void unaMoliendaQueNoExisteSeRechaza() {
		NuevaSuscripcionDto datos = new NuevaSuscripcionDto(1, "SEMANAL", 1, null, null,
				List.of(new NuevaSuscripcionItemDto(3, 99, 1, new BigDecimal("100"))));

		assertThatThrownBy(() -> servicio.crear(datos))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("No existe el tipo de molienda");
	}

	// ---------------------------------------------------------------------
	// CAMBIAR EL ESTADO
	// ---------------------------------------------------------------------
	@Test
	void sePuedePausarReactivarYCancelarUnAbono() {
		Suscripcion abono = existente();

		assertThat(servicio.cambiarEstado(7, estado("PAUSADA")).estado()).isEqualTo("PAUSADA");
		assertThat(servicio.cambiarEstado(7, estado("activa")).estado()).isEqualTo("ACTIVA");
		assertThat(servicio.cambiarEstado(7, estado(" CANCELADA ")).estado()).isEqualTo("CANCELADA");
		assertThat(abono.getEstado().name()).isEqualTo("CANCELADA");
	}

	@Test
	void unAbonoCanceladoNoSePuedeReactivar() {
		existente();
		servicio.cambiarEstado(7, estado("CANCELADA"));

		assertThatThrownBy(() -> servicio.cambiarEstado(7, estado("ACTIVA")))
				.isInstanceOf(ConflictoException.class);
	}

	@Test
	void unEstadoDesconocidoSeRechaza() {
		existente();

		assertThatThrownBy(() -> servicio.cambiarEstado(7, estado("SUSPENDIDA")))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("Estado desconocido");
	}

	@Test
	void unaSuscripcionQueNoExisteSeRechaza() {
		when(suscripcionRepository.buscarPorIdConBloqueo(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.cambiarEstado(99, estado("PAUSADA")))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessage("No existe la suscripción 99.");
	}

	// ---------------------------------------------------------------------
	// LISTAR
	// ---------------------------------------------------------------------
	@Test
	void elListadoDevuelveLosAbonosConSusRenglones() {
		Suscripcion abono = existente();
		when(suscripcionRepository.buscarTodasConDetalle()).thenReturn(List.of(abono));

		List<SuscripcionDto> abonos = servicio.listar();

		assertThat(abonos).hasSize(1);
		assertThat(abonos.get(0).items()).hasSize(1);
		assertThat(abonos.get(0).cliente()).isEqualTo("Hotel Andino");
	}

	// ---------------------------------------------------------------------
	// AYUDANTES
	// ---------------------------------------------------------------------
	private NuevaSuscripcionDto pedido(int clienteId, String frecuencia, int dia, LocalDate inicio, LocalDate fin) {
		return new NuevaSuscripcionDto(clienteId, frecuencia, dia, inicio, fin,
				List.of(new NuevaSuscripcionItemDto(3, 4, 2, new BigDecimal("120000.00"))));
	}

	private CambioEstadoSuscripcionDto estado(String texto) {
		return new CambioEstadoSuscripcionDto(texto);
	}

	// Un abono ya guardado, devuelto por el repository con bloqueo
	private Suscripcion existente() {
		Suscripcion abono = new Suscripcion(hotel, FrecuenciaSuscripcion.SEMANAL, 1,
				LocalDate.of(2026, 11, 4), null, LocalDate.of(2026, 11, 9));
		abono.agregarRenglon(presentacionRepository.findById(3).orElseThrow(),
				tipoMoliendaRepository.findById(4).orElseThrow(), 2, new BigDecimal("120000.00"));
		when(suscripcionRepository.buscarPorIdConBloqueo(7)).thenReturn(Optional.of(abono));
		return abono;
	}
}
