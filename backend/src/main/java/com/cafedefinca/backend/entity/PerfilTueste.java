package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabla "perfil_tueste": Tueste Claro, Medio u Oscuro, con su merma esperada y horas de reposo.
 */
@Entity
@Table(name = "perfil_tueste")
public class PerfilTueste {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_perfil_tueste")
	private Integer idPerfilTueste;

	@Column(name = "nombre_perfil", nullable = false)
	private String nombrePerfil;

	@Column(name = "temp_objetivo_c", nullable = false)
	private BigDecimal tempObjetivoC;

	@Column(name = "tiempo_tueste_min", nullable = false)
	private Integer tiempoTuesteMin;

	@Column(name = "merma_esperada_pct")
	private BigDecimal mermaEsperadaPct;

	@Column(name = "horas_reposo_minimas")
	private Integer horasReposoMinimas;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected PerfilTueste() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdPerfilTueste() {
		return idPerfilTueste;
	}

	public String getNombrePerfil() {
		return nombrePerfil;
	}

	public BigDecimal getTempObjetivoC() {
		return tempObjetivoC;
	}

	public Integer getTiempoTuesteMin() {
		return tiempoTuesteMin;
	}

	public BigDecimal getMermaEsperadaPct() {
		return mermaEsperadaPct;
	}

	public Integer getHorasReposoMinimas() {
		return horasReposoMinimas;
	}
}
