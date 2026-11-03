package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.HojaRutaDto;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.HojaRuta;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.HojaRutaRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
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
 * Prueba la consulta de hojas de ruta con repositories simulados (sin base de datos):
 * qué paradas trae cada hoja, en qué orden y con qué datos del cliente.
 */
class HojaRutaServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final LocalDate lunes = LocalDate.of(2026, 11, 9);
	private final LocalDate martes = LocalDate.of(2026, 11, 10);
	private HojaRutaRepository hojaRutaRepository;
	private PedidoRepository pedidoRepository;
	private HojaRutaService servicio;
	private HojaRuta hojaLunes;
	private HojaRuta hojaMartes;

	@BeforeEach
	void preparar() {
		hojaRutaRepository = Mockito.mock(HojaRutaRepository.class);
		pedidoRepository = Mockito.mock(PedidoRepository.class);
		servicio = new HojaRutaService(hojaRutaRepository, pedidoRepository);

		hojaLunes = hoja(1, lunes, "PENDIENTE");
		hojaMartes = hoja(2, martes, "EN_CURSO");
		when(hojaRutaRepository.findById(1)).thenReturn(Optional.of(hojaLunes));
		when(hojaRutaRepository.findByFechaReparto(lunes)).thenReturn(Optional.of(hojaLunes));
	}

	// ---------------------------------------------------------------------
	// UNA HOJA CON SUS PARADAS
	// ---------------------------------------------------------------------
	@Test
	void unaHojaTraeSusParadasEnOrdenDeVisitaConElDomicilioDelCliente() {
		Pedido primero = pedido(10, 1, 1, "Hotel Andino", "Av. Libertador 1200", "Vicente López", "324000.00");
		Pedido segundo = pedido(11, 1, 2, "Café Cortázar S.A.S.", "Thames 1500", "Palermo", "125000.00");
		when(pedidoRepository.buscarPorHojasConCliente(List.of(1))).thenReturn(List.of(primero, segundo));

		HojaRutaDto hoja = servicio.obtener(1);

		assertThat(hoja.idHojaRuta()).isEqualTo(1);
		assertThat(hoja.fechaReparto()).isEqualTo(lunes);
		assertThat(hoja.estadoRuta()).isEqualTo("PENDIENTE");
		assertThat(hoja.paradas()).extracting(HojaRutaDto.Parada::ordenVisita).containsExactly(1, 2);
		assertThat(hoja.paradas().get(0).cliente()).isEqualTo("Hotel Andino");
		assertThat(hoja.paradas().get(0).domicilio()).isEqualTo("Av. Libertador 1200");
		assertThat(hoja.paradas().get(0).localidad()).isEqualTo("Vicente López");
		assertThat(hoja.paradas().get(0).pedidoId()).isEqualTo(10);
		assertThat(hoja.paradas().get(0).montoTotal()).isEqualByComparingTo("324000.00");
		assertThat(hoja.paradas().get(1).cliente()).isEqualTo("Café Cortázar S.A.S.");
	}

	@Test
	void unaHojaSinPedidosTieneUnaListaVaciaDeParadas() {
		when(pedidoRepository.buscarPorHojasConCliente(List.of(1))).thenReturn(List.of());

		assertThat(servicio.obtener(1).paradas()).isEmpty();
	}

	@Test
	void unaHojaQueNoExisteSeRechazaConUnMensajeClaro() {
		when(hojaRutaRepository.findById(99)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicio.obtener(99))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessage("No existe la hoja de ruta 99.");
	}

	// ---------------------------------------------------------------------
	// LISTAR
	// ---------------------------------------------------------------------
	@Test
	void sinFechaListaTodasLasHojasConSusParadasCadaUnaEnLaSuya() {
		Pedido deLunes = pedido(10, 1, 1, "Hotel Andino", "Av. Libertador 1200", "Vicente López", "324000.00");
		Pedido deMartes = pedido(20, 2, 1, "Café Cortázar S.A.S.", "Thames 1500", "Palermo", "125000.00");
		when(hojaRutaRepository.findAllByOrderByFechaRepartoDesc()).thenReturn(List.of(hojaMartes, hojaLunes));
		when(pedidoRepository.buscarPorHojasConCliente(List.of(2, 1))).thenReturn(List.of(deMartes, deLunes));

		List<HojaRutaDto> hojas = servicio.listar(null);

		assertThat(hojas).extracting(HojaRutaDto::idHojaRuta).containsExactly(2, 1);
		assertThat(hojas.get(0).paradas()).extracting(HojaRutaDto.Parada::pedidoId).containsExactly(20);
		assertThat(hojas.get(1).paradas()).extracting(HojaRutaDto.Parada::pedidoId).containsExactly(10);
	}

	@Test
	void todosLosPedidosDeTodasLasHojasSeTraenEnUnaSolaConsulta() {
		when(hojaRutaRepository.findAllByOrderByFechaRepartoDesc()).thenReturn(List.of(hojaMartes, hojaLunes));
		when(pedidoRepository.buscarPorHojasConCliente(any())).thenReturn(List.of());

		servicio.listar(null);

		verify(pedidoRepository, times(1)).buscarPorHojasConCliente(any());
	}

	@Test
	void conFechaDevuelveSoloLaHojaDeEseDia() {
		Pedido deLunes = pedido(10, 1, 1, "Hotel Andino", "Av. Libertador 1200", "Vicente López", "324000.00");
		when(pedidoRepository.buscarPorHojasConCliente(List.of(1))).thenReturn(List.of(deLunes));

		List<HojaRutaDto> hojas = servicio.listar(lunes);

		assertThat(hojas).hasSize(1);
		assertThat(hojas.get(0).fechaReparto()).isEqualTo(lunes);
		verify(hojaRutaRepository, never()).findAllByOrderByFechaRepartoDesc();
	}

	@Test
	void conUnaFechaSinRepartoDevuelveUnaListaVaciaYNoConsultaPedidos() {
		when(hojaRutaRepository.findByFechaReparto(martes)).thenReturn(Optional.empty());

		assertThat(servicio.listar(martes)).isEmpty();
		verify(pedidoRepository, never()).buscarPorHojasConCliente(any());
	}

	@Test
	void sinNingunaHojaDevuelveUnaListaVacia() {
		when(hojaRutaRepository.findAllByOrderByFechaRepartoDesc()).thenReturn(List.of());

		assertThat(servicio.listar(null)).isEmpty();
		verify(pedidoRepository, never()).buscarPorHojasConCliente(any());
	}

	// ---------------------------------------------------------------------
	// AYUDANTES
	// ---------------------------------------------------------------------
	private HojaRuta hoja(int id, LocalDate fecha, String estado) {
		HojaRuta hoja = Mockito.mock(HojaRuta.class);
		when(hoja.getIdHojaRuta()).thenReturn(id);
		when(hoja.getFechaReparto()).thenReturn(fecha);
		when(hoja.getEstadoRuta()).thenReturn(estado);
		return hoja;
	}

	private Pedido pedido(int id, int idHoja, int orden, String cliente, String domicilio, String localidad, String monto) {
		Cliente clienteMock = Mockito.mock(Cliente.class);
		Pedido pedido = Mockito.mock(Pedido.class);
		when(clienteMock.getIdCliente()).thenReturn(id + 100);
		when(clienteMock.getNombreRazonSocial()).thenReturn(cliente);
		when(clienteMock.getDomicilio()).thenReturn(domicilio);
		when(clienteMock.getLocalidad()).thenReturn(localidad);
		when(pedido.getIdPedido()).thenReturn(id);
		when(pedido.getIdHojaRuta()).thenReturn(idHoja);
		when(pedido.getOrdenVisita()).thenReturn(orden);
		when(pedido.getCliente()).thenReturn(clienteMock);
		when(pedido.getMontoTotal()).thenReturn(new BigDecimal(monto));
		when(pedido.getEstadoPreparacion()).thenReturn("PENDIENTE");
		return pedido;
	}
}
