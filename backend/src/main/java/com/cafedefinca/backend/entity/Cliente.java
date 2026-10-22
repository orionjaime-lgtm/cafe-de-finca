package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabla "cliente": clientes mayoristas (B2B) y consumidores finales (B2C).
 */
@Entity
@Table(name = "cliente")
public class Cliente {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_cliente")
	private Integer idCliente;

	@Column(name = "tipo_cliente", nullable = false)
	private String tipoCliente;

	@Column(name = "nombre_razon_social", nullable = false)
	private String nombreRazonSocial;

	@Column(name = "cuit_o_dni", nullable = false)
	private String cuitODni;

	@Column(name = "condicion_iva")
	private String condicionIva;

	@Column(name = "plazo_cta_cte_dias")
	private Integer plazoCtaCteDias;

	@Column(name = "limite_credito")
	private BigDecimal limiteCredito;

	@Column(name = "contacto_wp_ig")
	private String contactoWpIg;

	@Column(name = "domicilio")
	private String domicilio;

	@Column(name = "localidad")
	private String localidad;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected Cliente() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdCliente() {
		return idCliente;
	}

	public String getTipoCliente() {
		return tipoCliente;
	}

	public String getNombreRazonSocial() {
		return nombreRazonSocial;
	}

	public String getCuitODni() {
		return cuitODni;
	}

	public String getCondicionIva() {
		return condicionIva;
	}

	public Integer getPlazoCtaCteDias() {
		return plazoCtaCteDias;
	}

	public BigDecimal getLimiteCredito() {
		return limiteCredito;
	}

	public String getContactoWpIg() {
		return contactoWpIg;
	}

	public String getDomicilio() {
		return domicilio;
	}

	public String getLocalidad() {
		return localidad;
	}
}
