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
 * Tabla "pago": cobro individual asociado a un pedido.
 */
@Entity
@Table(name = "pago")
public class Pago {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_pago")
	private Integer idPago;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_pedido", nullable = false)
	private Pedido pedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_medio_pago", nullable = false)
	private MedioPago medioPago;

	@Column(name = "fecha_hora_pago", nullable = false)
	private LocalDateTime fechaHoraPago;

	@Column(name = "monto_pagado", nullable = false)
	private BigDecimal montoPagado;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected Pago() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO
	// ---------------------------------------------------------------------
	public Pago(Pedido pedido, MedioPago medioPago, LocalDateTime fechaHoraPago, BigDecimal montoPagado) {
		this.pedido = pedido;
		this.medioPago = medioPago;
		this.fechaHoraPago = fechaHoraPago;
		this.montoPagado = montoPagado;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdPago() {
		return idPago;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public MedioPago getMedioPago() {
		return medioPago;
	}

	public LocalDateTime getFechaHoraPago() {
		return fechaHoraPago;
	}

	public BigDecimal getMontoPagado() {
		return montoPagado;
	}
}
