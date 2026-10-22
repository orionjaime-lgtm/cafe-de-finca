package com.cafedefinca.backend.exception;

import com.cafedefinca.backend.dto.ErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Atrapa los errores de todos los controllers y los devuelve como JSON claro,
 * en lugar de un error 500 genérico (regla técnica 6 del README).
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

	// ---------------------------------------------------------------------
	// DATO INVÁLIDO ENVIADO POR EL FRONTEND -> 400
	// ---------------------------------------------------------------------
	@ExceptionHandler(SolicitudInvalidaException.class)
	public ResponseEntity<ErrorDto> manejarSolicitudInvalida(SolicitudInvalidaException ex) {
		return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	// ---------------------------------------------------------------------
	// PARÁMETRO CON TIPO EQUIVOCADO (ej: ?soloDisponibles=hola) -> 400
	// ---------------------------------------------------------------------
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorDto> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
		String mensaje = "El parámetro '" + ex.getName() + "' tiene un valor inválido: " + ex.getValue();
		return responder(HttpStatus.BAD_REQUEST, mensaje);
	}

	// ---------------------------------------------------------------------
	// ARMADO DE LA RESPUESTA
	// ---------------------------------------------------------------------
	private ResponseEntity<ErrorDto> responder(HttpStatus estado, String mensaje) {
		ErrorDto cuerpo = new ErrorDto(estado.value(), mensaje);
		return ResponseEntity.status(estado).body(cuerpo);
	}
}
