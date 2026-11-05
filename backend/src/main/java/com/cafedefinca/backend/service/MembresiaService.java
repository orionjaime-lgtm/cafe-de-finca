package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.MembresiaDto;
import com.cafedefinca.backend.dto.NuevaMembresiaDto;
import com.cafedefinca.backend.dto.PlanSuscripcionDto;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.MembresiaCliente;
import com.cafedefinca.backend.entity.PlanMembresia;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.MembresiaClienteRepository;
import com.cafedefinca.backend.repository.PlanMembresiaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MembresiaService {

	private final PlanMembresiaRepository planRepository;
	private final MembresiaClienteRepository membresiaRepository;
	private final ClienteRepository clienteRepository;
	private final Clock reloj;

	public MembresiaService(PlanMembresiaRepository planRepository,
			MembresiaClienteRepository membresiaRepository,
			ClienteRepository clienteRepository,
			Clock reloj) {
		this.planRepository = planRepository;
		this.membresiaRepository = membresiaRepository;
		this.clienteRepository = clienteRepository;
		this.reloj = reloj;
	}

	public List<MembresiaDto> listar() {
		return membresiaRepository.buscarTodasConClienteYPlan().stream().map(this::aDto).toList();
	}

	public List<PlanSuscripcionDto> listarPlanes() {
		return planRepository.findAllByActivoTrueOrderByIdPlanAsc().stream()
				.map(plan -> new PlanSuscripcionDto(plan.getIdPlan(), plan.getNombrePlan(),
						plan.getPorcentajeDescuento(), plan.getDescripcion()))
				.toList();
	}

	@Transactional
	public MembresiaDto activar(Integer clienteId, Integer planId) {
		return crear(new NuevaMembresiaDto(clienteId, planId));
	}

	@Transactional
	public MembresiaDto crear(NuevaMembresiaDto datos) {
		Cliente cliente = clienteRepository.findById(datos.clienteId())
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el cliente con id " + datos.clienteId() + "."));
		PlanMembresia plan = planRepository.findById(datos.planId())
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el plan con id " + datos.planId() + "."));
		if (!plan.isActivo()) {
			throw new SolicitudInvalidaException("El plan seleccionado no está activo.");
		}
		if (membresiaRepository.existsByCliente_IdClienteAndEstado(cliente.getIdCliente(), "ACTIVA")) {
			throw new ConflictoException("El cliente ya tiene una membresía activa.");
		}
		MembresiaCliente membresia = membresiaRepository.save(
				new MembresiaCliente(cliente, plan, LocalDate.now(reloj)));
		return aDto(membresia);
	}

	@Transactional
	public MembresiaDto cancelar(Integer idMembresia) {
		MembresiaCliente membresia = membresiaRepository.buscarPorIdConBloqueo(idMembresia)
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe la membresía con id " + idMembresia + "."));
		if (!"ACTIVA".equals(membresia.getEstado())) {
			throw new ConflictoException("La membresía ya está cancelada.");
		}
		membresia.cancelar(LocalDate.now(reloj));
		return aDto(membresia);
	}

	private MembresiaDto aDto(MembresiaCliente membresia) {
		return new MembresiaDto(
				membresia.getIdMembresia(),
				membresia.getCliente().getIdCliente(),
				membresia.getCliente().getNombreRazonSocial(),
				membresia.getPlan().getIdPlan(),
				membresia.getPlan().getNombrePlan(),
				membresia.getPlan().getPorcentajeDescuento(),
				membresia.getFechaInicio(),
				membresia.getFechaFin(),
				membresia.getEstado());
	}
}
