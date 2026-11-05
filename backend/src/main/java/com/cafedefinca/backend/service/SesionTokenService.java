package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.AuthDto;
import com.cafedefinca.backend.enums.RolUsuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

@Service
public class SesionTokenService {
	private static final long DURACION_MILLIS = 8L * 60 * 60 * 1000;
	private final byte[] secreto;

	public SesionTokenService(@Value("${app.sesion.secreto:cafe-de-finca-desarrollo-cambiar-en-produccion}") String secreto) {
		this.secreto = secreto.getBytes(StandardCharsets.UTF_8);
	}

	public String crear(AuthDto.Sesion sesion) {
		long expira = System.currentTimeMillis() + DURACION_MILLIS;
		String contenido = sesion.idUsuario() + ":" + sesion.rol().name() + ":" + (sesion.idCliente() == null ? "-" : sesion.idCliente()) + ":" + expira;
		String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(contenido.getBytes(StandardCharsets.UTF_8));
		return payload + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(firmar(payload));
	}

	public DatosSesion validar(String token) {
		if (token == null || !token.contains(".")) throw new SecurityException("Sesión no válida.");
		String[] partes = token.split("\\.");
		if (partes.length != 2) throw new SecurityException("Sesión no válida.");
		byte[] firmaEsperada = firmar(partes[0]);
		byte[] firmaRecibida;
		try {
			firmaRecibida = Base64.getUrlDecoder().decode(partes[1]);
		} catch (IllegalArgumentException e) {
			throw new SecurityException("Sesión no válida.");
		}
		int diferencia = firmaEsperada.length ^ firmaRecibida.length;
		for (int i = 0; i < Math.min(firmaEsperada.length, firmaRecibida.length); i++) diferencia |= firmaEsperada[i] ^ firmaRecibida[i];
		if (diferencia != 0) throw new SecurityException("Sesión no válida.");
		try {
			String contenido = new String(Base64.getUrlDecoder().decode(partes[0]), StandardCharsets.UTF_8);
			String[] datos = contenido.split(":");
			if (datos.length != 4) throw new SecurityException("Sesión no válida.");
			long expira = Long.parseLong(datos[3]);
			if (expira < System.currentTimeMillis()) throw new SecurityException("La sesión expiró.");
			Integer idCliente = "-".equals(datos[2]) ? null : Integer.valueOf(datos[2]);
			return new DatosSesion(Integer.valueOf(datos[0]), RolUsuario.valueOf(datos[1].toUpperCase(Locale.ROOT)), idCliente);
		} catch (IllegalArgumentException | SecurityException e) {
			throw new SecurityException("Sesión no válida.");
		}
	}

	private byte[] firmar(String contenido) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secreto, "HmacSHA256"));
			return mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			throw new IllegalStateException("No se pudo firmar la sesión.", e);
		}
	}

	public record DatosSesion(Integer idUsuario, RolUsuario rol, Integer idCliente) {
	}
}
