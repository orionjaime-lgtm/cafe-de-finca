package com.cafedefinca.backend.entity;

import com.cafedefinca.backend.enums.RolUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_usuario")
	private Integer idUsuario;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Enumerated(EnumType.STRING)
	@Column(name = "rol", nullable = false)
	private RolUsuario rol;

	@Column(name = "id_cliente")
	private Integer idCliente;

	@Column(name = "activo", nullable = false)
	private boolean activo;

	@Column(name = "fecha_alta", insertable = false, updatable = false)
	private LocalDateTime fechaAlta;

	protected Usuario() {
	}

	public Usuario(String email, String passwordHash, String nombre, RolUsuario rol, Integer idCliente) {
		validarVinculo(rol, idCliente);
		this.email = email;
		this.passwordHash = passwordHash;
		this.nombre = nombre;
		this.rol = rol;
		this.idCliente = idCliente;
		this.activo = true;
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getNombre() {
		return nombre;
	}

	public RolUsuario getRol() {
		return rol;
	}

	public Integer getIdCliente() {
		return idCliente;
	}

	public boolean isActivo() {
		return activo;
	}

	public LocalDateTime getFechaAlta() {
		return fechaAlta;
	}

	public void activar() {
		this.activo = true;
	}

	public void desactivar() {
		this.activo = false;
	}

	public void cambiarCargo(RolUsuario nuevoRol, Integer nuevoIdCliente) {
		validarVinculo(nuevoRol, nuevoIdCliente);
		this.rol = nuevoRol;
		this.idCliente = nuevoIdCliente;
	}

	private static void validarVinculo(RolUsuario rol, Integer idCliente) {
		boolean esCliente = rol == RolUsuario.CLIENTE;
		if (esCliente != (idCliente != null)) {
			throw new IllegalArgumentException("Solo el cargo CLIENTE se vincula a un cliente, y es obligatorio.");
		}
	}
}
