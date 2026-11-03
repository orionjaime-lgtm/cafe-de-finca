package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.PagoAcumuladoDto;
import com.cafedefinca.backend.entity.CanalVenta;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.Factura;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.repository.PagoRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CobroServiceTest {

	private PedidoRepository pedidoRepository;
	private PagoRepository pagoRepository;
	private CobroService servicio;

	@BeforeEach
	void preparar() {
		pedidoRepository = mock(PedidoRepository.class);
		pagoRepository = mock(PagoRepository.class);
		Clock reloj = Clock.fixed(
				Instant.parse("2026-10-28T15:00:00Z"),
				ZoneId.of("America/Argentina/Buenos_Aires"));
		servicio = new CobroService(pedidoRepository, pagoRepository, reloj);
	}

	@Test
	void clasificaLosVencimientosYCalculaLaDeudaPendiente() {
		Pedido venceHoy = pedido(1, LocalDate.of(2026, 10, 28));
		Pedido vencido = pedido(2, LocalDate.of(2026, 10, 27));
		Pedido venceEnOchoDias = pedido(3, LocalDate.of(2026, 11, 5));
		when(pedidoRepository.buscarPendientesB2b()).thenReturn(List.of(venceHoy, vencido, venceEnOchoDias));
		when(pagoRepository.sumarPorPedidos(List.of(1, 2, 3)))
				.thenReturn(List.of(new PagoAcumuladoDto(1, new BigDecimal("25.00"))));

		var cobros = servicio.listarB2b();

		assertThat(cobros).extracting("estado").containsExactly("POR_VENCER", "VENCIDO", "AL_DIA");
		assertThat(cobros.getFirst().deuda()).isEqualByComparingTo("75.00");
		assertThat(cobros.getFirst().vencimiento()).isEqualTo(LocalDate.of(2026, 10, 28));
	}

	private Pedido pedido(Integer id, LocalDate vencimiento) {
		Cliente cliente = mock(Cliente.class);
		when(cliente.getIdCliente()).thenReturn(id);
		when(cliente.getNombreRazonSocial()).thenReturn("Cliente " + id);
		Pedido pedido = new Pedido(cliente, mock(CanalVenta.class),
				LocalDateTime.of(2026, 10, 1, 12, 0), new BigDecimal("100.00"));
		ReflectionTestUtils.setField(pedido, "idPedido", id);
		Factura factura = new Factura(pedido, "Factura A", "000" + id,
				LocalDate.of(2026, 10, 1), vencimiento, new BigDecimal("100.00"));
		ReflectionTestUtils.setField(factura, "idFactura", id);
		ReflectionTestUtils.setField(pedido, "factura", factura);
		return pedido;
	}
}
