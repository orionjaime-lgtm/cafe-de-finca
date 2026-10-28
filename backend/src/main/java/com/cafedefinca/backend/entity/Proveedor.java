package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Tabla "proveedor": quien vende el café verde (exportadoras e importadoras).
 */
@Entity
@Table(name = "proveedor")
public class Proveedor {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_proveedor")
	private Integer idProveedor;

	@Column(name = "cuit", nullable = false)
	private String cuit;

	@Column(name = "razon_social", nullable = false)
	private String razonSocial;

	@Column(name = "pais_origen")
	private String paisOrigen;

	@Column(name = "telefono_email")
	private String telefonoEmail;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected Proveedor() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdProveedor() {
		return idProveedor;
	}

	public String getCuit() {
		return cuit;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public String getPaisOrigen() {
		return paisOrigen;
	}

	public String getTelefonoEmail() {
		return telefonoEmail;
	}
}
