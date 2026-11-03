package com.cafedefinca.backend.dto;

import com.cafedefinca.backend.entity.Usuario;
import com.cafedefinca.backend.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UsuarioDto {
	private UsuarioDto() {
	}

	public record Nuevo(
			@NotBlank(message = "El email es obligatorio.")
			@Email(message = "El email no es válido.")
			@Size(max = 150, message = "El email es demasiado largo.") String email,
			@NotBlank(message = "La contraseña es obligatoria.") String password,
			@NotBlank(message = "El nombre es obligatorio.")
			@Size(max = 150, message = "El nombre es demasiado largo.") String nombre,
			@NotNull(message = "El cargo es obligatorio.") RolUsuario rol,
			Integer idCliente) {
	}

	public record Cambio(RolUsuario rol, Integer idCliente, Boolean activo) {
	}

	public record Resumen(Integer idUsuario, String nombre, String email, RolUsuario rol, Integer idCliente, boolean activo) {
		public static Resumen de(Usuario usuario) {
			return new Resumen(usuario.getIdUsuario(), usuario.getNombre(), usuario.getEmail(), usuario.getRol(), usuario.getIdCliente(), usuario.isActivo());
		}
	}
}
