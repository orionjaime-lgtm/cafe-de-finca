package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ResultadoGeneracionDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tarea programada: todos los días genera los pedidos de los abonos a los que les toca.
 * Por defecto corre a las 06:00, hora de Buenos Aires. Se puede cambiar el horario con la propiedad
 * cafe.suscripciones.cron, o apagar con cafe.suscripciones.tarea-activa=false
 * (también como variables de entorno CAFE_SUSCRIPCIONES_CRON y CAFE_SUSCRIPCIONES_TAREA_ACTIVA).
 * El mismo trabajo se puede disparar a mano con POST /api/suscripciones/generar.
 */
@Component
@ConditionalOnProperty(name = "cafe.suscripciones.tarea-activa", havingValue = "true", matchIfMissing = true)
public class TareaGeneracionPedidos {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private static final Logger LOG = LoggerFactory.getLogger(TareaGeneracionPedidos.class);

	private final GeneradorPedidosService generador;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el generador)
	// ---------------------------------------------------------------------
	public TareaGeneracionPedidos(GeneradorPedidosService generador) {
		this.generador = generador;
	}

	// ---------------------------------------------------------------------
	// LA TAREA: un error no debe cortar las corridas de los días siguientes
	// ---------------------------------------------------------------------
	@Scheduled(cron = "${cafe.suscripciones.cron:0 0 6 * * *}", zone = "America/Argentina/Buenos_Aires")
	public void ejecutar() {
		try {
			ResultadoGeneracionDto resultado = generador.generar();
			LOG.info("Tarea de abonos terminada: {} pedidos generados y {} abonos omitidos",
					resultado.generados().size(), resultado.omitidas().size());
		} catch (RuntimeException ex) {
			LOG.error("La tarea de generación de pedidos de abonos falló", ex);
		}
	}
}
