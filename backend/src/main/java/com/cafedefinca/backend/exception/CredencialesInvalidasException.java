package com.cafedefinca.backend.exception;

/**
 * Se lanza cuando el email o la contraseña del inicio de sesión no son correctos.
 * Es siempre el mismo mensaje, para no revelar si el email existe.
 * El ManejadorGlobalErrores la convierte en una respuesta 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

	public CredencialesInvalidasException() {
		super("Email o contraseña incorrectos.");
	}
}
