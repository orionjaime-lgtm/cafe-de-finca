package com.cafedefinca.backend.dto;

import com.cafedefinca.backend.enums.NivelAlerta;

import java.math.BigDecimal;

/**
 * Una alerta del panel "Alertas Activas".
 * consumoDiarioGramos y diasEstimados solo vienen cargados en las alertas de ADVERTENCIA (predictivas).
 */
public record AlertaDto(
		NivelAlerta nivel,
		Integer idOrigen,
		String origen,
		String mensaje,
		BigDecimal stockActualGramos,
		BigDecimal stockMinimoGramos,
		BigDecimal consumoDiarioGramos,
		Long diasEstimados) {
}
