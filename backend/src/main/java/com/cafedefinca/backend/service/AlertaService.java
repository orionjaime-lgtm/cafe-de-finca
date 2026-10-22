package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.AlertaDto;
import com.cafedefinca.backend.dto.OrigenStockDto;
import com.cafedefinca.backend.dto.TotalPorOrigen;
import com.cafedefinca.backend.enums.EstadoStock;
import com.cafedefinca.backend.enums.NivelAlerta;
import com.cafedefinca.backend.repository.OrdenTuesteRepository;
import com.cafedefinca.backend.util.ConversorUnidades;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel de alertas: avisa qué orígenes de café verde están bajo el mínimo
 * y cuáles van a llegar al mínimo pronto según su ritmo de consumo (alerta predictiva).
 * Las alertas no se guardan en la base: se calculan cada vez que se consultan.
 */
@Service
@Transactional(readOnly = true)
public class AlertaService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------

	// Días hacia atrás que se miran para calcular el consumo promedio diario
	public static final int DIAS_VENTANA_CONSUMO = 30;

	// Si al ritmo actual se llega al mínimo en esta cantidad de días o menos, se avisa
	public static final int DIAS_AVISO_REABASTECIMIENTO = 7;

	// Decimales para mostrar los kilos en los mensajes (ej: "2.3 kg")
	private static final int DECIMALES_MENSAJE = 1;

	// Decimales internos del consumo diario en gramos
	private static final int DECIMALES_CONSUMO = 3;

	private final OrigenService origenService;
	private final OrdenTuesteRepository ordenTuesteRepository;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta las dependencias)
	// ---------------------------------------------------------------------
	public AlertaService(OrigenService origenService,
			OrdenTuesteRepository ordenTuesteRepository,
			Clock reloj) {
		this.origenService = origenService;
		this.ordenTuesteRepository = ordenTuesteRepository;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// LISTAR ALERTAS ACTIVAS
	// ---------------------------------------------------------------------

	/**
	 * Revisa cada origen y devuelve las alertas, primero las CRÍTICAS y después las ADVERTENCIAS.
	 */
	public List<AlertaDto> listarAlertas() {
		LocalDateTime desde = LocalDateTime.now(reloj).minusDays(DIAS_VENTANA_CONSUMO);
		List<OrigenStockDto> origenes = origenService.listarStock();
		Map<Integer, BigDecimal> tostadoPorOrigen = ordenTuesteRepository.sumarVerdeTostadoPorOrigenDesde(desde).stream()
				.collect(Collectors.toMap(TotalPorOrigen::idOrigen, TotalPorOrigen::totalGramos));
		List<AlertaDto> alertas = new ArrayList<>();

		for (OrigenStockDto origen : origenes) {
			BigDecimal gramosTostados = tostadoPorOrigen.getOrDefault(origen.idOrigen(), BigDecimal.ZERO);
			AlertaDto alerta = evaluarOrigen(origen, gramosTostados);
			if (alerta != null) {
				alertas.add(alerta);
			}
		}

		alertas.sort(Comparator.comparing(AlertaDto::nivel));
		return alertas;
	}

	// ---------------------------------------------------------------------
	// REGLA: ¿ESTE ORIGEN GENERA UNA ALERTA?
	// ---------------------------------------------------------------------

	/**
	 * Devuelve la alerta del origen, o null si no hace falta avisar nada.
	 */
	private AlertaDto evaluarOrigen(OrigenStockDto origen, BigDecimal gramosTostadosEnVentana) {
		String nombreOrigen = origen.pais() + " " + origen.region();
		String stockKg = aKilosTexto(origen.stockActualGramos());
		String minimoKg = aKilosTexto(origen.stockMinimoGramos());

		// 1. Sin stock: crítico
		if (origen.estado() == EstadoStock.CRITICO) {
			String mensaje = "Stock agotado totalmente. El mínimo configurado es " + minimoKg + " kg.";
			return alertaCritica(origen, nombreOrigen, mensaje);
		}

		// 2. Por debajo del mínimo: crítico
		if (origen.estado() == EstadoStock.BAJO) {
			String mensaje = "Stock crítico: " + stockKg + " kg restantes. El mínimo configurado es " + minimoKg + " kg.";
			return alertaCritica(origen, nombreOrigen, mensaje);
		}

		// 3. Stock OK: alerta predictiva si al ritmo actual llega pronto al mínimo
		return alertaPredictiva(origen, nombreOrigen, gramosTostadosEnVentana);
	}

	/**
	 * Consumo diario = gramos de verde tostados en los últimos 30 días / 30.
	 * Días hasta el mínimo = (stock actual - mínimo) / consumo diario, redondeado hacia arriba.
	 */
	private AlertaDto alertaPredictiva(OrigenStockDto origen, String nombreOrigen, BigDecimal gramosTostadosEnVentana) {
		BigDecimal consumoDiarioGramos;
		BigDecimal margenGramos;
		long diasHastaMinimo;
		String mensaje;

		// Sin consumo reciente no se puede predecir nada
		if (gramosTostadosEnVentana.compareTo(BigDecimal.ZERO) <= 0) {
			return null;
		}

		consumoDiarioGramos = gramosTostadosEnVentana.divide(
				BigDecimal.valueOf(DIAS_VENTANA_CONSUMO), DECIMALES_CONSUMO, RoundingMode.HALF_UP);
		margenGramos = origen.stockActualGramos().subtract(origen.stockMinimoGramos());
		diasHastaMinimo = margenGramos.divide(consumoDiarioGramos, 0, RoundingMode.CEILING).longValue();

		if (diasHastaMinimo > DIAS_AVISO_REABASTECIMIENTO) {
			return null;
		}

		mensaje = "Reabastecer en " + diasHastaMinimo + " días estimados según tu tasa de consumo promedio de "
				+ aKilosTexto(consumoDiarioGramos) + " kg/día.";

		return new AlertaDto(
				NivelAlerta.ADVERTENCIA,
				origen.idOrigen(),
				nombreOrigen,
				mensaje,
				origen.stockActualGramos(),
				origen.stockMinimoGramos(),
				consumoDiarioGramos,
				diasHastaMinimo);
	}

	// ---------------------------------------------------------------------
	// AYUDANTES
	// ---------------------------------------------------------------------
	private AlertaDto alertaCritica(OrigenStockDto origen, String nombreOrigen, String mensaje) {
		return new AlertaDto(
				NivelAlerta.CRITICO,
				origen.idOrigen(),
				nombreOrigen,
				mensaje,
				origen.stockActualGramos(),
				origen.stockMinimoGramos(),
				null,
				null);
	}

	// Gramos -> texto en kilos con 1 decimal. Ejemplo: 2300 -> "2.3"
	private String aKilosTexto(BigDecimal gramos) {
		return ConversorUnidades.gramosAKilos(gramos)
				.setScale(DECIMALES_MENSAJE, RoundingMode.HALF_UP)
				.toPlainString();
	}
}
