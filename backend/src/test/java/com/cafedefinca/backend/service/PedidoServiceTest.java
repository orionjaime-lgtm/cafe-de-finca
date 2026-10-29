package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevoPedidoDto;
import com.cafedefinca.backend.dto.NuevoPedidoItemDto;
import com.cafedefinca.backend.dto.PedidoDto;
import com.cafedefinca.backend.entity.CanalVenta;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.DetallePedido;
import com.cafedefinca.backend.entity.Factura;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.entity.OrdenTueste;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.entity.Presentacion;
import com.cafedefinca.backend.entity.TipoMolienda;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.exception.StockInsuficienteException;
import com.cafedefinca.backend.repository.CanalVentaRepository;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.DetallePedidoRepository;
import com.cafedefinca.backend.repository.FacturaRepository;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import com.cafedefinca.backend.repository.PresentacionRepository;
import com.cafedefinca.backend.repository.TipoMoliendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PedidoServiceTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final Clock relojFijo = Clock.fixed(
			Instant.parse("2026-10-28T15:00:00Z"),
			ZoneId.of("America/Argentina/Buenos_Aires"));
	private ClienteRepository clienteRepository;
	private CanalVentaRepository canalVentaRepository;
	private PresentacionRepository presentacionRepository;
	private TipoMoliendaRepository tipoMoliendaRepository;
	private LoteCafeTostadoRepository loteRepository;
	private PedidoRepository pedidoRepository;
	private DetallePedidoRepository detalleRepository;
	private FacturaRepository facturaRepository;
	private PedidoService servicio;
	private Cliente cliente;
	private CanalVenta canal;
	private Presentacion presentacion;
	private TipoMolienda molienda;
	private LoteCafeTostado lote;

	@BeforeEach
	void preparar() {
		clienteRepository = org.mockito.Mockito.mock(ClienteRepository.class);
		canalVentaRepository = org.mockito.Mockito.mock(CanalVentaRepository.class);
		presentacionRepository = org.mockito.Mockito.mock(PresentacionRepository.class);
		tipoMoliendaRepository = org.mockito.Mockito.mock(TipoMoliendaRepository.class);
		loteRepository = org.mockito.Mockito.mock(LoteCafeTostadoRepository.class);
		pedidoRepository = org.mockito.Mockito.mock(PedidoRepository.class);
		detalleRepository = org.mockito.Mockito.mock(DetallePedidoRepository.class);
		facturaRepository = org.mockito.Mockito.mock(FacturaRepository.class);

		servicio = new PedidoService(
				clienteRepository,
				canalVentaRepository,
				presentacionRepository,
				tipoMoliendaRepository,
				loteRepository,
				pedidoRepository,
				detalleRepository,
				facturaRepository,
				relojFijo);

		cliente = org.mockito.Mockito.mock(Cliente.class);
		when(cliente.getIdCliente()).thenReturn(1);
		when(cliente.getTipoCliente()).thenReturn("B2C");
		canal = org.mockito.Mockito.mock(CanalVenta.class);
		when(canal.getIdCanalVenta()).thenReturn(2);
		presentacion = org.mockito.Mockito.mock(Presentacion.class);
		when(presentacion.getIdPresentacion()).thenReturn(3);
		when(presentacion.getPesoGramos()).thenReturn(new BigDecimal("250"));
		molienda = org.mockito.Mockito.mock(TipoMolienda.class);
		when(molienda.getIdTipoMolienda()).thenReturn(4);
		lote = new LoteCafeTostado(
				org.mockito.Mockito.mock(OrdenTueste.class),
				"LT-1001",
				LocalDateTime.of(2026, 10, 1, 10, 0),
				LocalDateTime.of(2026, 10, 2, 10, 0),
				LocalDate.of(2026, 11, 1),
				new BigDecimal("500"));
		ReflectionTestUtils.setField(lote, "idLoteTostado", 5);

		when(clienteRepository.findById(1)).thenReturn(Optional.of(cliente));
		when(canalVentaRepository.findById(2)).thenReturn(Optional.of(canal));
		when(presentacionRepository.findById(3)).thenReturn(Optional.of(presentacion));
		when(tipoMoliendaRepository.findById(4)).thenReturn(Optional.of(molienda));
		when(loteRepository.buscarPorIdsConBloqueo(anyCollection())).thenReturn(List.of(lote));
		when(pedidoRepository.saveAndFlush(any(Pedido.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		when(detalleRepository.saveAll(anyList()))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
		when(facturaRepository.save(any(Factura.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	// ---------------------------------------------------------------------
	// STOCK: LOS BORDES
	// ---------------------------------------------------------------------
	@Test
	void venderElStockJustoLoDescuentaCompleto() {
		PedidoDto pedido = servicio.crear(datosPedido());

		assertThat(lote.getStockDisponibleGramos()).isEqualByComparingTo("0");
		assertThat(lote.getEstadoDisponibilidad()).isEqualTo("AGOTADO");
		assertThat(pedido.montoTotal()).isEqualByComparingTo("2000");
		assertThat(pedido.items()).hasSize(1);
		assertThat(pedido.items().getFirst().pesoTotalGramos()).isEqualByComparingTo("500");
		verify(pedidoRepository).saveAndFlush(any(Pedido.class));
		verify(detalleRepository).saveAll(anyList());
	}

	@Test
	void stockInsuficienteNoModificaElLoteNiGuardaElPedido() {
		lote = loteConStock("499");

		assertThatThrownBy(() -> servicio.crear(datosPedido()))
				.isInstanceOf(StockInsuficienteException.class)
				.hasMessageContaining("Stock insuficiente");

		assertThat(lote.getStockDisponibleGramos()).isEqualByComparingTo("499");
		verify(pedidoRepository, never()).saveAndFlush(any(Pedido.class));
		verify(detalleRepository, never()).saveAll(anyList());
	}

	@Test
	void noPermiteVenderUnLoteVencido() {
		lote = loteConCaducidad(LocalDate.of(2026, 10, 27));

		assertThatThrownBy(() -> servicio.crear(datosPedido()))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("vencido");

		assertThat(lote.getStockDisponibleGramos()).isEqualByComparingTo("500");
		verify(pedidoRepository, never()).saveAndFlush(any(Pedido.class));
	}

	@Test
	void noPermiteVenderUnLoteQueSigueEnReposo() {
		lote = loteConDisponibilidad(LocalDateTime.of(2026, 10, 29, 15, 0));

		assertThatThrownBy(() -> servicio.crear(datosPedido()))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("en reposo");

		assertThat(lote.getStockDisponibleGramos()).isEqualByComparingTo("500");
		verify(pedidoRepository, never()).saveAndFlush(any(Pedido.class));
	}

	@Test
	void creaFacturaB2BConVencimientoYNumeroCorrelativo() {
		when(cliente.getTipoCliente()).thenReturn("B2B");
		when(cliente.getPlazoCtaCteDias()).thenReturn(15);
		when(facturaRepository.buscarNumeroMaximo("Factura A")).thenReturn(12L);

		PedidoDto pedido = servicio.crear(datosPedido());

		assertThat(pedido.factura()).isNotNull();
		assertThat(pedido.factura().tipoComprobante()).isEqualTo("Factura A");
		assertThat(pedido.factura().numeroComprobante()).isEqualTo("0013");
		assertThat(pedido.factura().fechaEmision()).isEqualTo(LocalDate.of(2026, 10, 28));
		assertThat(pedido.factura().fechaVencimiento()).isEqualTo(LocalDate.of(2026, 11, 12));
		assertThat(pedido.factura().montoTotal()).isEqualByComparingTo("2000");
		verify(facturaRepository).bloquearGeneracionDeNumero(7300002L);
	}

	// ---------------------------------------------------------------------
	// DATOS DE PRUEBA
	// ---------------------------------------------------------------------
	private NuevoPedidoDto datosPedido() {
		return new NuevoPedidoDto(
				1,
				2,
				List.of(new NuevoPedidoItemDto(5, 3, 4, 2, new BigDecimal("1000.00"))));
	}

	private LoteCafeTostado loteConStock(String stock) {
		LoteCafeTostado nuevo = loteConCaducidad(LocalDate.of(2026, 11, 1));
		ReflectionTestUtils.setField(nuevo, "stockDisponibleGramos", new BigDecimal(stock));
		return nuevo;
	}

	private LoteCafeTostado loteConCaducidad(LocalDate caducidad) {
		LoteCafeTostado nuevo = new LoteCafeTostado(
				org.mockito.Mockito.mock(OrdenTueste.class),
				"LT-1001",
				LocalDateTime.of(2026, 10, 1, 10, 0),
				LocalDateTime.of(2026, 10, 2, 10, 0),
				caducidad,
				new BigDecimal("500"));
		ReflectionTestUtils.setField(nuevo, "idLoteTostado", 5);
		when(loteRepository.buscarPorIdsConBloqueo(anyCollection())).thenReturn(List.of(nuevo));
		return nuevo;
	}

	private LoteCafeTostado loteConDisponibilidad(LocalDateTime disponibleVenta) {
		LoteCafeTostado nuevo = new LoteCafeTostado(
				org.mockito.Mockito.mock(OrdenTueste.class),
				"LT-1001",
				LocalDateTime.of(2026, 10, 1, 10, 0),
				disponibleVenta,
				LocalDate.of(2026, 11, 1),
				new BigDecimal("500"));
		ReflectionTestUtils.setField(nuevo, "idLoteTostado", 5);
		when(loteRepository.buscarPorIdsConBloqueo(anyCollection())).thenReturn(List.of(nuevo));
		return nuevo;
	}
}
