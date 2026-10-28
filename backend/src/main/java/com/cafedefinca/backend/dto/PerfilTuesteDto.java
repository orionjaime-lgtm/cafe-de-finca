package com.cafedefinca.backend.dto;

import java.math.BigDecimal;

/**
 * Perfil de tueste (Claro, Medio, Oscuro) tal como lo recibe el frontend.
 */
public record PerfilTuesteDto(
		Integer idPerfilTueste,
		String nombrePerfil,
		BigDecimal tempObjetivoC,
		Integer tiempoTuesteMin,
		BigDecimal mermaEsperadaPct,
		Integer horasReposoMinimas) {
}
