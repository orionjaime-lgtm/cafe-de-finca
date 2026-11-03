package com.cafedefinca.backend.entity;

import com.cafedefinca.backend.enums.EstadoSuscripcion;
import com.cafedefinca.backend.enums.FrecuenciaSuscripcion;
import com.cafedefinca.backend.exception.ConflictoException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Prueba las reglas de negocio de un abono: cuándo le toca generar un pedido, cómo avanza su fecha
 * y qué cambios de estado se permiten. Sin base de datos.
 * Referencias de calendario: el 02/11/2026 es lunes y el 04/11/2026 es miércoles.
 */
class SuscripcionTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final LocalDate hoy = LocalDate.of(2026, 11, 4);
	private final Cliente cliente = Mockito.mock(Cliente.class);

	// ---------------------------------------------------------------------
	// ¿LE TOCA GENERAR UN PEDIDO?
	// ---------------------------------------------------------------------
	@Test
	void unAbonoActivoConLaEntregaDeHoyDebeGenerar() {
		assertThat(semanal(hoy, null).debeGenerarPedido(hoy)).isTrue();
	}

	@Test
	void unAbonoActivoConLaEntregaVencidaDebeGenerar() {
		assertThat(semanal(hoy.minusDays(3), null).debeGenerarPedido(hoy)).isTrue();
	}

	@Test
	void unAbonoConLaEntregaDeMananaTodaviaNoDebeGenerar() {
		assertThat(semanal(hoy.plusDays(1), null).debeGenerarPedido(hoy)).isFalse();
	}

	@Test
	void unAbonoPausadoNoGeneraAunqueEsteVencido() {
		Suscripcion abono = semanal(hoy.minusDays(1), null);
		abono.pausar();

		assertThat(abono.debeGenerarPedido(hoy)).isFalse();
	}

	@Test
	void unAbonoCanceladoNoGenera() {
		Suscripcion abono = semanal(hoy, null);
		abono.cancelar();

		assertThat(abono.debeGenerarPedido(hoy)).isFalse();
	}

	@Test
	void noGeneraEntregasPosterioresALaFechaDeFin() {
		assertThat(semanal(hoy, hoy.minusDays(1)).debeGenerarPedido(hoy)).isFalse();
	}

	@Test
	void laEntregaDelDiaDeLaFechaDeFinTodaviaSeGenera() {
		assertThat(semanal(hoy, hoy).debeGenerarPedido(hoy)).isTrue();
	}

	// ---------------------------------------------------------------------
	// AVANZAR LA PRÓXIMA ENTREGA
	// ---------------------------------------------------------------------
	@Test
	void unAbonoSemanalAvanzaSieteDias() {
		Suscripcion abono = semanal(LocalDate.of(2026, 11, 2), null);

		abono.avanzarProximaFecha();

		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(LocalDate.of(2026, 11, 9));
	}

	@Test
	void unAbonoMensualAvanzaAlMismoDiaDelMesSiguiente() {
		Suscripcion abono = new Suscripcion(cliente, FrecuenciaSuscripcion.MENSUAL, 10,
				LocalDate.of(2026, 1, 1), null, LocalDate.of(2026, 12, 10));

		abono.avanzarProximaFecha();

		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(LocalDate.of(2027, 1, 10));
	}

	// ---------------------------------------------------------------------
	// CAMBIOS DE ESTADO PERMITIDOS
	// ---------------------------------------------------------------------
	@Test
	void unAbonoNuevoNaceActivo() {
		assertThat(semanal(hoy, null).getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
	}

	@Test
	void unAbonoActivoSePuedePausarYReactivar() {
		Suscripcion abono = semanal(hoy.plusDays(5), null);

		abono.pausar();
		assertThat(abono.getEstado()).isEqualTo(EstadoSuscripcion.PAUSADA);

		abono.reactivar(hoy);
		assertThat(abono.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(hoy.plusDays(5));
	}

	@Test
	void alReactivarConLaEntregaVencidaSeRecalculaParaNoGenerarDeGolpe() {
		// El abono es los lunes y estuvo pausado desde el 02/10; hoy es miércoles 04/11
		Suscripcion abono = new Suscripcion(cliente, FrecuenciaSuscripcion.SEMANAL, 1,
				LocalDate.of(2026, 1, 1), null, LocalDate.of(2026, 10, 5));
		abono.pausar();

		abono.reactivar(hoy);

		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(LocalDate.of(2026, 11, 9));
	}

	@Test
	void alReactivarConLaEntregaDeHoyNoSeCambiaLaFecha() {
		Suscripcion abono = semanal(hoy, null);
		abono.pausar();

		abono.reactivar(hoy);

		assertThat(abono.getProximaFechaGeneracion()).isEqualTo(hoy);
	}

	@Test
	void unAbonoActivoOPausadoSePuedeCancelar() {
		Suscripcion activo = semanal(hoy, null);
		Suscripcion pausado = semanal(hoy, null);
		pausado.pausar();

		activo.cancelar();
		pausado.cancelar();

		assertThat(activo.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
		assertThat(pausado.getEstado()).isEqualTo(EstadoSuscripcion.CANCELADA);
	}

	// ---------------------------------------------------------------------
	// CAMBIOS DE ESTADO NO PERMITIDOS (409)
	// ---------------------------------------------------------------------
	@Test
	void noSePuedePausarUnAbonoPausadoNiCancelado() {
		Suscripcion pausado = semanal(hoy, null);
		pausado.pausar();
		Suscripcion cancelado = semanal(hoy, null);
		cancelado.cancelar();

		assertThatThrownBy(pausado::pausar).isInstanceOf(ConflictoException.class);
		assertThatThrownBy(cancelado::pausar).isInstanceOf(ConflictoException.class);
	}

	@Test
	void noSePuedeReactivarUnAbonoActivoNiCancelado() {
		Suscripcion activo = semanal(hoy, null);
		Suscripcion cancelado = semanal(hoy, null);
		cancelado.cancelar();

		assertThatThrownBy(() -> activo.reactivar(hoy)).isInstanceOf(ConflictoException.class);
		assertThatThrownBy(() -> cancelado.reactivar(hoy))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("CANCELADA");
	}

	@Test
	void noSePuedeCancelarDosVeces() {
		Suscripcion abono = semanal(hoy, null);
		abono.cancelar();

		assertThatThrownBy(abono::cancelar)
				.isInstanceOf(ConflictoException.class)
				.hasMessage("La suscripción ya está cancelada.");
	}

	// ---------------------------------------------------------------------
	// AYUDANTE
	// ---------------------------------------------------------------------
	private Suscripcion semanal(LocalDate proxima, LocalDate fechaFin) {
		return new Suscripcion(cliente, FrecuenciaSuscripcion.SEMANAL, 3, LocalDate.of(2026, 1, 1), fechaFin, proxima);
	}
}
