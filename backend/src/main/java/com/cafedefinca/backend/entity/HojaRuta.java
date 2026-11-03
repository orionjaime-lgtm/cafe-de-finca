package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * Tabla "hoja_ruta": el reparto de un día. Hay una sola por fecha y agrupa los pedidos a entregar ese día.
 * El orden en que se visita a cada cliente se guarda en el pedido (orden_visita).
 */
@Entity
@Table(name = "hoja_ruta")
public class HojaRuta {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_hoja_ruta")
	private Integer idHojaRuta;

	@Column(name = "fecha_reparto", nullable = false)
	private LocalDate fechaReparto;

	@Column(name = "estado_ruta")
	private String estadoRuta;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected HojaRuta() {
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdHojaRuta() {
		return idHojaRuta;
	}

	public LocalDate getFechaReparto() {
		return fechaReparto;
	}

	public String getEstadoRuta() {
		return estadoRuta;
	}
}
