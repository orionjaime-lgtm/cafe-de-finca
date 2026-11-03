package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "membresia_cliente")
public class MembresiaCliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_membresia")
	private Integer idMembresia;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_plan", nullable = false)
	private PlanMembresia plan;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin")
	private LocalDate fechaFin;

	@Column(name = "estado", nullable = false)
	private String estado;

	protected MembresiaCliente() {
	}

	public MembresiaCliente(Cliente cliente, PlanMembresia plan, LocalDate fechaInicio) {
		this.cliente = cliente;
		this.plan = plan;
		this.fechaInicio = fechaInicio;
		this.estado = "ACTIVA";
	}

	public void cancelar(LocalDate fechaFin) {
		if (!"ACTIVA".equals(estado)) {
			throw new IllegalStateException("La membresía ya está cancelada.");
		}
		this.estado = "CANCELADA";
		this.fechaFin = fechaFin.isBefore(fechaInicio) ? fechaInicio : fechaFin;
	}

	public Integer getIdMembresia() {
		return idMembresia;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public PlanMembresia getPlan() {
		return plan;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public String getEstado() {
		return estado;
	}
}
