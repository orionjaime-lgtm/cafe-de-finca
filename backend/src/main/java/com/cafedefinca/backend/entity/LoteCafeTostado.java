package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tabla "lote_cafe_tostado": producto terminado que sale de una orden de tueste.
 * Tiene fecha de caducidad (control de frescura) y stock en gramos.
 */
@Entity
@Table(name = "lote_cafe_tostado")
public class LoteCafeTostado {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------

	// Estado con el que nace todo lote: todavía no terminó el reposo
	private static final String ESTADO_REPOSO = "REPOSO";

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_lote_tostado")
	private Integer idLoteTostado;

	// Relación 1 a 1: cada orden de tueste genera un único lote tostado
	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_orden_tueste", nullable = false, unique = true)
	private OrdenTueste ordenTueste;

	@Column(name = "codigo_lote_tostado", nullable = false)
	private String codigoLoteTostado;

	@Column(name = "fecha_hora_fin_tueste", nullable = false)
	private LocalDateTime fechaHoraFinTueste;

	@Column(name = "fecha_disponible_venta", nullable = false)
	private LocalDateTime fechaDisponibleVenta;

	@Column(name = "fecha_caducidad", nullable = false)
	private LocalDate fechaCaducidad;

	@Column(name = "peso_inicial_gramos", nullable = false)
	private BigDecimal pesoInicialGramos;

	@Column(name = "stock_disponible_gramos", nullable = false)
	private BigDecimal stockDisponibleGramos;

	@Column(name = "stock_reservado_gramos", nullable = false)
	private BigDecimal stockReservadoGramos;

	@Column(name = "estado_disponibilidad")
	private String estadoDisponibilidad;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected LoteCafeTostado() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO: el lote que sale de un tueste
	// Nace en REPOSO, con todo el café disponible y nada reservado.
	// ---------------------------------------------------------------------
	public LoteCafeTostado(OrdenTueste ordenTueste, String codigoLoteTostado, LocalDateTime fechaHoraFinTueste,
			LocalDateTime fechaDisponibleVenta, LocalDate fechaCaducidad, BigDecimal pesoInicialGramos) {
		this.ordenTueste = ordenTueste;
		this.codigoLoteTostado = codigoLoteTostado;
		this.fechaHoraFinTueste = fechaHoraFinTueste;
		this.fechaDisponibleVenta = fechaDisponibleVenta;
		this.fechaCaducidad = fechaCaducidad;
		this.pesoInicialGramos = pesoInicialGramos;
		this.stockDisponibleGramos = pesoInicialGramos;
		this.stockReservadoGramos = BigDecimal.ZERO;
		this.estadoDisponibilidad = ESTADO_REPOSO;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdLoteTostado() {
		return idLoteTostado;
	}

	public OrdenTueste getOrdenTueste() {
		return ordenTueste;
	}

	public String getCodigoLoteTostado() {
		return codigoLoteTostado;
	}

	public LocalDateTime getFechaHoraFinTueste() {
		return fechaHoraFinTueste;
	}

	public LocalDateTime getFechaDisponibleVenta() {
		return fechaDisponibleVenta;
	}

	public LocalDate getFechaCaducidad() {
		return fechaCaducidad;
	}

	public BigDecimal getPesoInicialGramos() {
		return pesoInicialGramos;
	}

	public BigDecimal getStockDisponibleGramos() {
		return stockDisponibleGramos;
	}

	public BigDecimal getStockReservadoGramos() {
		return stockReservadoGramos;
	}

	public String getEstadoDisponibilidad() {
		return estadoDisponibilidad;
	}
}
