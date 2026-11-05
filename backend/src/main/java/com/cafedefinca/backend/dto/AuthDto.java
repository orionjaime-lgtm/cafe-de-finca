package com.cafedefinca.backend.dto;

import com.cafedefinca.backend.entity.Usuario;
import com.cafedefinca.backend.enums.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class AuthDto {
	private AuthDto() {
	}

	public record Login(
			@NotBlank(message = "El email es obligatorio.") String email,
			@NotBlank(message = "La contraseña es obligatoria.") String password) {
	}

	public record Registro(
			@NotBlank(message = "El nombre es obligatorio.")
			@Size(max = 150, message = "El nombre es demasiado largo.") String nombre,
			@NotBlank(message = "El documento es obligatorio.")
			@Size(max = 20, message = "El documento es demasiado largo.") String documento,
			@NotBlank(message = "El email es obligatorio.")
			@Email(message = "El email no es válido.")
			@Size(max = 150, message = "El email es demasiado largo.") String email,
			@NotBlank(message = "La contraseña es obligatoria.") String password,
			@NotBlank(message = "Falta repetir la contraseña.") String confirmacion,
			@Size(max = 100, message = "El contacto es demasiado largo.") String contacto) {
	}

	public record Sesion(Integer idUsuario, String nombre, String email, RolUsuario rol, Integer idCliente, List<String> secciones, String token) {
		public static Sesion de(Usuario usuario) {
			return new Sesion(usuario.getIdUsuario(), usuario.getNombre(), usuario.getEmail(), usuario.getRol(), usuario.getIdCliente(), usuario.getRol().getSecciones(), null);
		}
	}
}
