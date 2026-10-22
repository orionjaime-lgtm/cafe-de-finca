package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabla "presentacion": envases de venta (Bolsa 250g, Bolsa 1kg, etc.).
 * El peso en gramos es la base de toda conversión de unidades.
 */
@Entity
@Table(name = "presentacion")
public class Presentacion {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_presentacion")
	private Integer idPresentacion;

	@Column(name = "nombre_envase", nullable = false)
	private String nombreEnvase;

	@Column(name = "peso_gramos", nullable = false)
	private BigDecimal pesoGramos;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected Presentacion() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdPresentacion() {
		return idPresentacion;
	}

	public String getNombreEnvase() {
		return nombreEnvase;
	}

	public BigDecimal getPesoGramos() {
		return pesoGramos;
	}
}
