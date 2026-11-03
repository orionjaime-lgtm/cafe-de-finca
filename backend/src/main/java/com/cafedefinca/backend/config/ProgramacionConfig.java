package com.cafedefinca.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita las tareas programadas (@Scheduled) del backend.
 * Hoy hay una sola: la generación diaria de los pedidos de los abonos (TareaGeneracionPedidos).
 */
@Configuration
@EnableScheduling
public class ProgramacionConfig {
}
