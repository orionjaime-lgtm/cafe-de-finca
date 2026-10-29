package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla "medio_pago": medios habilitados para registrar cobros.
 */
@Entity
@Table(name = "medio_pago")
public class MedioPago {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_medio_pago")
	private Integer idMedioPago;

	@Column(name = "nombre_medio", nullable = false)
	private String nombreMedio;

	@Column(name = "requiere_comprobante")
	private Boolean requiereComprobante;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected MedioPago() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdMedioPago() {
		return idMedioPago;
	}

	public String getNombreMedio() {
		return nombreMedio;
	}

	public Boolean getRequiereComprobante() {
		return requiereComprobante;
	}
}
