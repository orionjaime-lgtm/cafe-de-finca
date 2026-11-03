package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevoPagoDto;
import com.cafedefinca.backend.dto.PagoAcumuladoDto;
import com.cafedefinca.backend.entity.CanalVenta;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.MedioPago;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.repository.MedioPagoRepository;
import com.cafedefinca.backend.repository.PagoRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PagoServiceTest {

	private final Clock relojFijo = Clock.fixed(
			Instant.parse("2026-10-28T15:00:00Z"),
			ZoneId.of("America/Argentina/Buenos_Aires"));
	private PagoRepository pagoRepository;
	private PedidoRepository pedidoRepository;
	private MedioPagoRepository medioPagoRepository;
	private PagoService servicio;
	private Pedido pedido;
	private MedioPago medioPago;

	@BeforeEach
	void preparar() {
		pagoRepository = mock(PagoRepository.class);
		pedidoRepository = mock(PedidoRepository.class);
		medioPagoRepository = mock(MedioPagoRepository.class);
		servicio = new PagoService(pagoRepository, pedidoRepository, medioPagoRepository, relojFijo);

		pedido = new Pedido(mock(Cliente.class), mock(CanalVenta.class),
				LocalDateTime.of(2026, 10, 28, 12, 0), new BigDecimal("100.00"));
		ReflectionTestUtils.setField(pedido, "idPedido", 9);
		medioPago = mock(MedioPago.class);
		when(medioPago.getIdMedioPago()).thenReturn(2);
		when(medioPago.getNombreMedio()).thenReturn("Efectivo");
		when(pedidoRepository.buscarPorIdConBloqueo(9)).thenReturn(Optional.of(pedido));
		when(medioPagoRepository.findById(2)).thenReturn(Optional.of(medioPago));
		when(pagoRepository.sumarPorPedido(9))
				.thenReturn(List.of(new PagoAcumuladoDto(9, new BigDecimal("40.00"))));
		when(pagoRepository.save(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	@Test
	void unPagoParcialActualizaElEstadoDelPedido() {
		var pago = servicio.registrar(new NuevoPagoDto(9, 2, new BigDecimal("25.00")));

		assertThat(pago.estadoCobro()).isEqualTo("PARCIAL");
		assertThat(pedido.getEstadoCobro()).isEqualTo("PARCIAL");
		assertThat(pago.fechaHoraPago()).isEqualTo(LocalDateTime.of(2026, 10, 28, 12, 0));
	}

	@Test
	void pagarElSaldoCompletoActualizaElEstadoAPagado() {
		var pago = servicio.registrar(new NuevoPagoDto(9, 2, new BigDecimal("60.00")));

		assertThat(pago.estadoCobro()).isEqualTo("PAGADO");
		assertThat(pedido.getEstadoCobro()).isEqualTo("PAGADO");
	}

	@Test
	void rechazaUnPagoSuperiorAlSaldoSinGuardarlo() {
		assertThatThrownBy(() -> servicio.registrar(new NuevoPagoDto(9, 2, new BigDecimal("60.01"))))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("supera el saldo pendiente");

		verify(pagoRepository, never()).save(any());
		assertThat(pedido.getEstadoCobro()).isEqualTo("PENDIENTE");
	}
}
