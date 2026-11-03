package com.cafedefinca.backend.entity;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba lo que se agregó a Pedido para el reparto: asignarlo a un abono y a una hoja de ruta.
 */
class PedidoTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final Pedido pedido = new Pedido(Mockito.mock(Cliente.class), Mockito.mock(CanalVenta.class),
			LocalDateTime.of(2026, 11, 4, 15, 0), new BigDecimal("240000.00"));

	// ---------------------------------------------------------------------
	// UN PEDIDO COMÚN NO TIENE REPARTO
	// ---------------------------------------------------------------------
	@Test
	void unPedidoNuevoNoTieneAbonoNiHojaDeRuta() {
		assertThat(pedido.getIdSuscripcion()).isNull();
		assertThat(pedido.getFechaEntregaPrevista()).isNull();
		assertThat(pedido.getIdHojaRuta()).isNull();
		assertThat(pedido.getOrdenVisita()).isNull();
	}

	// ---------------------------------------------------------------------
	// ASIGNAR AL ABONO Y A LA RUTA
	// ---------------------------------------------------------------------
	@Test
	void asignarloAUnAbonoGuardaElAbonoYLaFechaDeEntrega() {
		pedido.asignarASuscripcion(7, LocalDate.of(2026, 11, 9));

		assertThat(pedido.getIdSuscripcion()).isEqualTo(7);
		assertThat(pedido.getFechaEntregaPrevista()).isEqualTo(LocalDate.of(2026, 11, 9));
	}

	@Test
	void asignarloAUnaRutaGuardaLaHojaYElOrdenDeVisita() {
		pedido.asignarARuta(9, 3);

		assertThat(pedido.getIdHojaRuta()).isEqualTo(9);
		assertThat(pedido.getOrdenVisita()).isEqualTo(3);
	}
}
