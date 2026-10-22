package com.cafedefinca.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Define el "reloj" que usa el backend completo para saber qué día y hora es.
 * Los services piden la fecha a este reloj en lugar de llamar a LocalDate.now() directo:
 * así los tests pueden usar un reloj fijo y probar las fechas sin depender del día real.
 */
@Configuration
public class TiempoConfig {

	// ---------------------------------------------------------------------
	// ZONA HORARIA DEL NEGOCIO (la tostaduría está en Buenos Aires)
	// ---------------------------------------------------------------------
	public static final ZoneId ZONA_HORARIA = ZoneId.of("America/Argentina/Buenos_Aires");

	@Bean
	Clock reloj() {
		return Clock.system(ZONA_HORARIA);
	}
}
