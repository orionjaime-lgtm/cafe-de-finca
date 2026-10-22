package com.cafedefinca.backend.enums;

/**
 * Gravedad de una alerta del panel de alertas predictivas.
 * El orden importa: las alertas se listan de la más grave a la menos grave.
 */
public enum NivelAlerta {

	// El stock ya está por debajo del mínimo o agotado: requiere atención inmediata
	CRITICO,

	// El stock todavía está bien, pero al ritmo de consumo actual llegará al mínimo en pocos días
	ADVERTENCIA
}
