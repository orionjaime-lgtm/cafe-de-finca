package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ResumenCaducidadDto;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.enums.EstadoFrescura;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Reglas de caducidad y frescura del café tostado.
 * Es el único lugar donde se decide si un lote está ÓPTIMO, POR VENCER o VENCIDO.
 */
@Service
@Transactional(readOnly = true)
public class CaducidadService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------

	// Un lote pasa a "por vencer" cuando le quedan esta cantidad de días o menos
	public static final int DIAS_AVISO_VENCIMIENTO = 7;

	// Valor de "estado_disponibilidad" con el que un lote se marca vencido a mano en la base
	private static final String ESTADO_VENCIDO_EN_BASE = "VENCIDO";

	private final LoteCafeTostadoRepository loteCafeTostadoRepository;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el repository y el reloj)
	// ---------------------------------------------------------------------
	public CaducidadService(LoteCafeTostadoRepository loteCafeTostadoRepository, Clock reloj) {
		this.loteCafeTostadoRepository = loteCafeTostadoRepository;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// RESUMEN: LOS 3 NÚMEROS DEL PANEL
	// ---------------------------------------------------------------------

	/**
	 * Cuenta los lotes en inventario según su frescura.
	 * Los tres números son excluyentes: cada lote cuenta en uno solo.
	 * "activos" son los lotes ÓPTIMOS; los "por vencer" se cuentan aparte.
	 */
	public ResumenCaducidadDto obtenerResumen() {
		LocalDate hoy = LocalDate.now(reloj);
		long activos = 0;
		long porVencer = 0;
		long vencidos = 0;
		List<LoteCafeTostado> lotes = loteCafeTostadoRepository.buscarEnInventario();

		for (LoteCafeTostado lote : lotes) {
			EstadoFrescura estado = calcularEstadoFrescura(lote, hoy);

			switch (estado) {
				case OPTIMO -> activos++;
				case POR_VENCER -> porVencer++;
				case VENCIDO -> vencidos++;
			}
		}

		return new ResumenCaducidadDto(activos, porVencer, vencidos);
	}

	// ---------------------------------------------------------------------
	// CÁLCULOS DE FRESCURA (los reutiliza LoteTostadoService)
	// ---------------------------------------------------------------------

	/**
	 * Días que faltan hasta la fecha de caducidad. Negativo si ya pasó.
	 */
	public long calcularDiasRestantes(LocalDate fechaCaducidad, LocalDate hoy) {
		return ChronoUnit.DAYS.between(hoy, fechaCaducidad);
	}

	/**
	 * Estado de frescura de un lote. Si en la base fue marcado VENCIDO (por ejemplo, al descartarlo),
	 * cuenta como vencido aunque su fecha todavía no haya llegado.
	 */
	public EstadoFrescura calcularEstadoFrescura(LoteCafeTostado lote, LocalDate hoy) {
		if (ESTADO_VENCIDO_EN_BASE.equals(lote.getEstadoDisponibilidad())) {
			return EstadoFrescura.VENCIDO;
		}
		return calcularEstadoFrescura(calcularDiasRestantes(lote.getFechaCaducidad(), hoy));
	}

	/**
	 * Traduce los días restantes a un estado de frescura.
	 * El día de la caducidad (0 días restantes) el lote todavía es vendible: cuenta como POR_VENCER.
	 */
	public EstadoFrescura calcularEstadoFrescura(long diasRestantes) {
		if (diasRestantes < 0) {
			return EstadoFrescura.VENCIDO;
		}
		if (diasRestantes <= DIAS_AVISO_VENCIMIENTO) {
			return EstadoFrescura.POR_VENCER;
		}
		return EstadoFrescura.OPTIMO;
	}
}
