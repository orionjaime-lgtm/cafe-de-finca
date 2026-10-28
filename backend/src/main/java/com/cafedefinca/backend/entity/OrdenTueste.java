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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tabla "orden_tueste": cada tueste de grano verde.
 * Se usa para saber de qué origen viene un lote tostado y cuánto verde se consume por día.
 */
@Entity
@Table(name = "orden_tueste")
public class OrdenTueste {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------

	// Estado con el que nace toda orden (el otro valor que acepta la base es ANULADO)
	private static final String ESTADO_COMPLETADO = "COMPLETADO";

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_orden_tueste")
	private Integer idOrdenTueste;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_lote_verde", nullable = false)
	private LoteCafeVerde loteVerde;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_perfil_tueste", nullable = false)
	private PerfilTueste perfilTueste;

	@Column(name = "fecha_hora_tueste", nullable = false)
	private LocalDateTime fechaHoraTueste;

	@Column(name = "peso_verde_usado_g", nullable = false)
	private BigDecimal pesoVerdeUsadoG;

	@Column(name = "peso_tostado_obtenido_g", nullable = false)
	private BigDecimal pesoTostadoObtenidoG;

	// Columna calculada por la base: Java solo la lee, nunca la escribe
	@Column(name = "merma_real_pct", insertable = false, updatable = false)
	private BigDecimal mermaRealPct;

	@Column(name = "operador_tostador")
	private String operadorTostador;

	@Column(name = "estado_orden")
	private String estadoOrden;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected OrdenTueste() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO: un tueste recién hecho
	// Nace COMPLETADO. La merma real no se pasa: la calcula la base.
	// ---------------------------------------------------------------------
	public OrdenTueste(LoteCafeVerde loteVerde, PerfilTueste perfilTueste, LocalDateTime fechaHoraTueste,
			BigDecimal pesoVerdeUsadoG, BigDecimal pesoTostadoObtenidoG, String operadorTostador) {
		this.loteVerde = loteVerde;
		this.perfilTueste = perfilTueste;
		this.fechaHoraTueste = fechaHoraTueste;
		this.pesoVerdeUsadoG = pesoVerdeUsadoG;
		this.pesoTostadoObtenidoG = pesoTostadoObtenidoG;
		this.operadorTostador = operadorTostador;
		this.estadoOrden = ESTADO_COMPLETADO;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdOrdenTueste() {
		return idOrdenTueste;
	}

	public LoteCafeVerde getLoteVerde() {
		return loteVerde;
	}

	public PerfilTueste getPerfilTueste() {
		return perfilTueste;
	}

	public LocalDateTime getFechaHoraTueste() {
		return fechaHoraTueste;
	}

	public BigDecimal getPesoVerdeUsadoG() {
		return pesoVerdeUsadoG;
	}

	public BigDecimal getPesoTostadoObtenidoG() {
		return pesoTostadoObtenidoG;
	}

	public BigDecimal getMermaRealPct() {
		return mermaRealPct;
	}

	public String getOperadorTostador() {
		return operadorTostador;
	}

	public String getEstadoOrden() {
		return estadoOrden;
	}
}
