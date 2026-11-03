package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevaMembresiaDto;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.MembresiaCliente;
import com.cafedefinca.backend.entity.PlanMembresia;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.MembresiaClienteRepository;
import com.cafedefinca.backend.repository.PlanMembresiaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MembresiaServiceTest {

	private ClienteRepository clienteRepository;
	private PlanMembresiaRepository planRepository;
	private MembresiaClienteRepository membresiaRepository;
	private MembresiaService servicio;
	private Cliente cliente;
	private PlanMembresia plan;

	@BeforeEach
	void preparar() {
		clienteRepository = mock(ClienteRepository.class);
		planRepository = mock(PlanMembresiaRepository.class);
		membresiaRepository = mock(MembresiaClienteRepository.class);
		Clock reloj = Clock.fixed(
				Instant.parse("2026-10-28T15:00:00Z"),
				ZoneId.of("America/Argentina/Buenos_Aires"));
		servicio = new MembresiaService(planRepository, membresiaRepository, clienteRepository, reloj);

		cliente = mock(Cliente.class);
		when(cliente.getIdCliente()).thenReturn(7);
		when(cliente.getNombreRazonSocial()).thenReturn("Café del Centro");
		plan = mock(PlanMembresia.class);
		when(plan.getIdPlan()).thenReturn(3);
		when(plan.getNombrePlan()).thenReturn("Plan Premium");
		when(plan.getPorcentajeDescuento()).thenReturn(new BigDecimal("15.00"));
		when(plan.isActivo()).thenReturn(true);
		when(clienteRepository.findById(7)).thenReturn(Optional.of(cliente));
		when(planRepository.findById(3)).thenReturn(Optional.of(plan));
		when(membresiaRepository.save(any(MembresiaCliente.class)))
				.thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	@Test
	void creaMembresiaEnElPlanElegido() {
		var membresia = servicio.crear(new NuevaMembresiaDto(7, 3));

		assertThat(membresia.clienteId()).isEqualTo(7);
		assertThat(membresia.planId()).isEqualTo(3);
		assertThat(membresia.nombrePlan()).isEqualTo("Plan Premium");
		assertThat(membresia.porcentajeDescuento()).isEqualByComparingTo("15.00");
		assertThat(membresia.fechaInicio()).isEqualTo(LocalDate.of(2026, 10, 28));
		assertThat(membresia.estado()).isEqualTo("ACTIVA");
	}

	@Test
	void noPermiteDosMembresiasActivasParaElMismoCliente() {
		when(membresiaRepository.existsByCliente_IdClienteAndEstado(7, "ACTIVA")).thenReturn(true);

		assertThatThrownBy(() -> servicio.crear(new NuevaMembresiaDto(7, 3)))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("ya tiene una membresía activa");

		verify(membresiaRepository, never()).save(any(MembresiaCliente.class));
	}
}
