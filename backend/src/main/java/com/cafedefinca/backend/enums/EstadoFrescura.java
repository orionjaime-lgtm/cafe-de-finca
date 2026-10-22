package com.cafedefinca.backend.enums;

/**
 * Estado de frescura de un lote tostado, según los días que faltan para su fecha de caducidad.
 */
public enum EstadoFrescura {

	// Faltan más de 7 días para que caduque
	OPTIMO,

	// Caduca dentro de los próximos 7 días (incluye el día de hoy): priorizar su salida
	POR_VENCER,

	// La fecha de caducidad ya pasó
	VENCIDO
}
