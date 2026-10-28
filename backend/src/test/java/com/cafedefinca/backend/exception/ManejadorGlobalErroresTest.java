package com.cafedefinca.backend.exception;

import com.cafedefinca.backend.dto.ErrorDto;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponseException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba que cada error se traduzca al código HTTP y al JSON correctos (sin Spring, llamando al manejador directo).
 */
class ManejadorGlobalErroresTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final ManejadorGlobalErrores manejador = new ManejadorGlobalErrores();

	// ---------------------------------------------------------------------
	// 400
	// ---------------------------------------------------------------------
	@Test
	void solicitudInvalidaDevuelve400() {
		ResponseEntity<ErrorDto> respuesta = manejador.manejarSolicitudInvalida(new SolicitudInvalidaException("Dato mal"));

		assertThat(respuesta.getStatusCode().value()).isEqualTo(400);
		assertThat(respuesta.getBody().estado()).isEqualTo(400);
		assertThat(respuesta.getBody().mensaje()).isEqualTo("Dato mal");
	}

	// ---------------------------------------------------------------------
	// 401
	// ---------------------------------------------------------------------
	@Test
	void credencialesInvalidasDevuelve401() {
		ResponseEntity<ErrorDto> respuesta = manejador.manejarCredenciales(new CredencialesInvalidasException());

		assertThat(respuesta.getStatusCode().value()).isEqualTo(401);
		assertThat(respuesta.getBody().mensaje()).isEqualTo("Email o contraseña incorrectos.");
	}

	// ---------------------------------------------------------------------
	// 409
	// ---------------------------------------------------------------------
	@Test
	void stockInsuficienteDevuelve409ConLosGramos() {
		ResponseEntity<ErrorDto> respuesta = manejador.manejarStockInsuficiente(
				new StockInsuficienteException(new BigDecimal("2300.000"), new BigDecimal("5000.000")));

		assertThat(respuesta.getStatusCode().value()).isEqualTo(409);
		assertThat(respuesta.getBody().mensaje())
				.isEqualTo("Stock insuficiente: hay 2300 g disponibles y se pidieron 5000 g.");
	}

	@Test
	void errorDeLaBaseDevuelve409SinMostrarElSql() {
		ResponseEntity<ErrorDto> respuesta = manejador.manejarIntegridad(
				new DataIntegrityViolationException("violates check constraint secreto_interno"));

		assertThat(respuesta.getStatusCode().value()).isEqualTo(409);
		assertThat(respuesta.getBody().mensaje()).doesNotContain("secreto_interno");
	}

	// ---------------------------------------------------------------------
	// 500 y errores propios de Spring
	// ---------------------------------------------------------------------
	@Test
	void errorInesperadoDevuelve500SinDetallesInternos() {
		ResponseEntity<ErrorDto> respuesta = manejador.manejarInesperado(new RuntimeException("clave_secreta_123"));

		assertThat(respuesta.getStatusCode().value()).isEqualTo(500);
		assertThat(respuesta.getBody().mensaje()).doesNotContain("clave_secreta_123");
	}

	@Test
	void unErrorHttpDeSpringConservaSuCodigo() {
		ResponseEntity<ErrorDto> respuesta = manejador.manejarInesperado(new ErrorResponseException(HttpStatus.NOT_FOUND));

		assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
		assertThat(respuesta.getBody().mensaje()).isEqualTo("No se encontró el recurso solicitado.");
	}
}
