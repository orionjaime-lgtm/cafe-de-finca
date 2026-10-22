package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabla "origen_cafe": país, región y variedad del grano.
 * Cada origen tiene su propio umbral de stock mínimo (en gramos) para las alertas.
 */
@Entity
@Table(name = "origen_cafe")
public class OrigenCafe {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_origen")
	private Integer idOrigen;

	@Column(name = "pais", nullable = false)
	private String pais;

	@Column(name = "region", nullable = false)
	private String region;

	@Column(name = "variedad")
	private String variedad;

	@Column(name = "altura_msnm")
	private Integer alturaMsnm;

	@Column(name = "notas_cata")
	private String notasCata;

	@Column(name = "stock_minimo_alerta_gramos")
	private BigDecimal stockMinimoAlertaGramos;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected OrigenCafe() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdOrigen() {
		return idOrigen;
	}

	public String getPais() {
		return pais;
	}

	public String getRegion() {
		return region;
	}

	public String getVariedad() {
		return variedad;
	}

	public Integer getAlturaMsnm() {
		return alturaMsnm;
	}

	public String getNotasCata() {
		return notasCata;
	}

	public BigDecimal getStockMinimoAlertaGramos() {
		return stockMinimoAlertaGramos;
	}
}
