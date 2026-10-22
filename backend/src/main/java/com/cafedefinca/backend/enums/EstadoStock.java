package com.cafedefinca.backend.enums;

/**
 * Estado del stock de café verde de un origen, comparado con su stock mínimo configurado.
 */
public enum EstadoStock {

	// El stock está en el mínimo o por encima
	OK,

	// El stock está por debajo del mínimo, pero todavía queda café
	BAJO,

	// El stock está por debajo del mínimo y no queda nada (0 gramos)
	CRITICO
}
