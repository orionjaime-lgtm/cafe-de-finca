package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "plan_membresia")
public class PlanMembresia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_plan")
	private Integer idPlan;

	@Column(name = "nombre_plan", nullable = false)
	private String nombrePlan;

	@Column(name = "porcentaje_descuento", nullable = false)
	private BigDecimal porcentajeDescuento;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "activo", nullable = false)
	private boolean activo;

	protected PlanMembresia() {
	}

	public Integer getIdPlan() {
		return idPlan;
	}

	public String getNombrePlan() {
		return nombrePlan;
	}

	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public boolean isActivo() {
		return activo;
	}
}
