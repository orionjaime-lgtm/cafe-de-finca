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

/**
 * Tabla "detalle_suscripcion": un renglón del abono (presentación, molienda, cantidad y precio pactado).
 * No guarda el lote: el lote se elige al generar cada pedido, según lo que haya en stock ese día.
 */
@Entity
@Table(name = "detalle_suscripcion")
public class DetalleSuscripcion {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_detalle_suscripcion")
	private Integer idDetalleSuscripcion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_suscripcion", nullable = false)
	private Suscripcion suscripcion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_presentacion", nullable = false)
	private Presentacion presentacion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_tipo_molienda", nullable = false)
	private TipoMolienda tipoMolienda;

	@Column(name = "cantidad_envases", nullable = false)
	private Integer cantidadEnvases;

	@Column(name = "precio_unitario", nullable = false)
	private BigDecimal precioUnitario;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected DetalleSuscripcion() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO (lo usa Suscripcion.agregarRenglon)
	// ---------------------------------------------------------------------
	DetalleSuscripcion(Suscripcion suscripcion, Presentacion presentacion, TipoMolienda tipoMolienda,
			Integer cantidadEnvases, BigDecimal precioUnitario) {
		this.suscripcion = suscripcion;
		this.presentacion = presentacion;
		this.tipoMolienda = tipoMolienda;
		this.cantidadEnvases = cantidadEnvases;
		this.precioUnitario = precioUnitario;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdDetalleSuscripcion() {
		return idDetalleSuscripcion;
	}

	public Suscripcion getSuscripcion() {
		return suscripcion;
	}

	public Presentacion getPresentacion() {
		return presentacion;
	}

	public TipoMolienda getTipoMolienda() {
		return tipoMolienda;
	}

	public Integer getCantidadEnvases() {
		return cantidadEnvases;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}
}
