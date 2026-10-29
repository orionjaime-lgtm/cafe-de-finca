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

/**
 * Tabla "factura": comprobante simple asociado a un pedido B2B.
 */
@Entity
@Table(name = "factura")
public class Factura {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_factura")
	private Integer idFactura;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_pedido", nullable = false, unique = true)
	private Pedido pedido;

	@Column(name = "tipo_comprobante", nullable = false)
	private String tipoComprobante;

	@Column(name = "numero_comprobante", nullable = false)
	private String numeroComprobante;

	@Column(name = "fecha_emision", nullable = false)
	private LocalDate fechaEmision;

	@Column(name = "fecha_vencimiento")
	private LocalDate fechaVencimiento;

	@Column(name = "monto_total", nullable = false)
	private BigDecimal montoTotal;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected Factura() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO
	// ---------------------------------------------------------------------
	public Factura(Pedido pedido, String tipoComprobante, String numeroComprobante,
			LocalDate fechaEmision, LocalDate fechaVencimiento, BigDecimal montoTotal) {
		this.pedido = pedido;
		this.tipoComprobante = tipoComprobante;
		this.numeroComprobante = numeroComprobante;
		this.fechaEmision = fechaEmision;
		this.fechaVencimiento = fechaVencimiento;
		this.montoTotal = montoTotal;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdFactura() {
		return idFactura;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public String getTipoComprobante() {
		return tipoComprobante;
	}

	public String getNumeroComprobante() {
		return numeroComprobante;
	}

	public LocalDate getFechaEmision() {
		return fechaEmision;
	}

	public LocalDate getFechaVencimiento() {
		return fechaVencimiento;
	}

	public BigDecimal getMontoTotal() {
		return montoTotal;
	}
}
