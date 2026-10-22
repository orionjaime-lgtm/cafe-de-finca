package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla "tipo_molienda": Grano Entero, Espresso, Italiana, Filtro.
 */
@Entity
@Table(name = "tipo_molienda")
public class TipoMolienda {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_tipo_molienda")
	private Integer idTipoMolienda;

	@Column(name = "nombre_molienda", nullable = false)
	private String nombreMolienda;

	@Column(name = "tipo_cafetera")
	private String tipoCafetera;

	@Column(name = "nivel_granulometria")
	private String nivelGranulometria;

	@Column(name = "es_grano_entero")
	private Boolean esGranoEntero;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected TipoMolienda() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdTipoMolienda() {
		return idTipoMolienda;
	}

	public String getNombreMolienda() {
		return nombreMolienda;
	}

	public String getTipoCafetera() {
		return tipoCafetera;
	}

	public String getNivelGranulometria() {
		return nivelGranulometria;
	}

	public Boolean getEsGranoEntero() {
		return esGranoEntero;
	}
}
