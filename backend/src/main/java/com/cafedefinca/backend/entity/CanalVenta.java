package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla "canal_venta": origen comercial de un pedido.
 */
@Entity
@Table(name = "canal_venta")
public class CanalVenta {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_canal_venta")
	private Integer idCanalVenta;

	@Column(name = "nombre_canal", nullable = false)
	private String nombreCanal;

	@Column(name = "requiere_datos_fiscales")
	private Boolean requiereDatosFiscales;

	@Column(name = "permite_cta_cte")
	private Boolean permiteCtaCte;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected CanalVenta() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdCanalVenta() {
		return idCanalVenta;
	}

	public String getNombreCanal() {
		return nombreCanal;
	}

	public Boolean getRequiereDatosFiscales() {
		return requiereDatosFiscales;
	}

	public Boolean getPermiteCtaCte() {
		return permiteCtaCte;
	}
}
