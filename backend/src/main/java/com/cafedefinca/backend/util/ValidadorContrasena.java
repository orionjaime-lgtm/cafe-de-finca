package com.cafedefinca.backend.util;

import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import java.nio.charset.StandardCharsets;

public final class ValidadorContrasena {
	public static final int LARGO_MINIMO = 8;
	private static final int BYTES_MAXIMOS = 72;

	private ValidadorContrasena() {
	}

	public static void validar(String contrasena) {
		if (contrasena == null || contrasena.length() < LARGO_MINIMO) {
			throw new SolicitudInvalidaException("La contraseña debe tener al menos " + LARGO_MINIMO + " caracteres.");
		}
		if (contrasena.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS) {
			throw new SolicitudInvalidaException("La contraseña es demasiado larga (máximo 72 bytes).");
		}
	}
}
