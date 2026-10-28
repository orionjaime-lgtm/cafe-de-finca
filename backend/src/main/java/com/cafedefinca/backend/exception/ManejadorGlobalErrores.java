package com.cafedefinca.backend.exception;

import com.cafedefinca.backend.dto.ErrorDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * Atrapa los errores de todos los controllers y los devuelve como JSON claro,
 * en lugar de un error 500 genérico (regla técnica 6 del README).
 * Formato de la respuesta: { "estado": 409, "mensaje": "..." }
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private static final Logger LOG = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

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
	// CUERPO CON CAMPOS INVÁLIDOS (falló un @NotBlank, @Positive, etc.) -> 400
	// ---------------------------------------------------------------------
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDto> manejarValidacion(MethodArgumentNotValidException ex) {
		String detalle = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining("; "));
		return responder(HttpStatus.BAD_REQUEST, detalle.isBlank() ? "Los datos enviados no son válidos." : detalle);
	}

	// ---------------------------------------------------------------------
	// CUERPO QUE NO ES UN JSON VÁLIDO -> 400
	// ---------------------------------------------------------------------
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorDto> manejarJsonIlegible(HttpMessageNotReadableException ex) {
		return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido.");
	}

	// ---------------------------------------------------------------------
	// EMAIL O CONTRASEÑA INCORRECTOS -> 401
	// ---------------------------------------------------------------------
	@ExceptionHandler(CredencialesInvalidasException.class)
	public ResponseEntity<ErrorDto> manejarCredenciales(CredencialesInvalidasException ex) {
		return responder(HttpStatus.UNAUTHORIZED, ex.getMessage());
	}

	// ---------------------------------------------------------------------
	// NO HAY SUFICIENTE CAFÉ -> 409
	// ---------------------------------------------------------------------
	@ExceptionHandler(StockInsuficienteException.class)
	public ResponseEntity<ErrorDto> manejarStockInsuficiente(StockInsuficienteException ex) {
		return responder(HttpStatus.CONFLICT, ex.getMessage());
	}

	// ---------------------------------------------------------------------
	// LA BASE RECHAZÓ EL DATO (CHECK, UNIQUE, clave foránea) -> 409
	// No se devuelve el detalle del SQL al frontend; queda en el registro del servidor.
	// ---------------------------------------------------------------------
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorDto> manejarIntegridad(DataIntegrityViolationException ex) {
		LOG.warn("La base de datos rechazó la operación: {}", ex.getMostSpecificCause().getMessage());
		return responder(HttpStatus.CONFLICT, "La operación no se pudo guardar: el dato está duplicado o no cumple una regla de la base.");
	}

	// ---------------------------------------------------------------------
	// CUALQUIER OTRO ERROR
	// - Los errores propios de Spring (404, 405...) conservan su código.
	// - Los inesperados devuelven 500 con un mensaje genérico, sin detalles internos.
	// ---------------------------------------------------------------------
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDto> manejarInesperado(Exception ex) {
		if (ex instanceof ErrorResponse errorHttp) {
			HttpStatusCode codigo = errorHttp.getStatusCode();
			return responder(codigo, mensajeParaCodigo(codigo.value()));
		}
		LOG.error("Error inesperado", ex);
		return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor. Intentá de nuevo más tarde.");
	}

	// ---------------------------------------------------------------------
	// MENSAJES PARA LOS ERRORES PROPIOS DE SPRING
	// ---------------------------------------------------------------------
	private String mensajeParaCodigo(int codigo) {
		return switch (codigo) {
			case 400 -> "La petición no es válida.";
			case 404 -> "No se encontró el recurso solicitado.";
			case 405 -> "Método HTTP no permitido para este recurso.";
			case 415 -> "Tipo de contenido no soportado.";
			default -> "No se pudo procesar la petición.";
		};
	}

	// ---------------------------------------------------------------------
	// ARMADO DE LA RESPUESTA
	// ---------------------------------------------------------------------
	private ResponseEntity<ErrorDto> responder(HttpStatusCode estado, String mensaje) {
		ErrorDto cuerpo = new ErrorDto(estado.value(), mensaje);
		return ResponseEntity.status(estado).body(cuerpo);
	}
}
