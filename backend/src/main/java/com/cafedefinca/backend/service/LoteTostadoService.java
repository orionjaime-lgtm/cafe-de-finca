package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.LoteTostadoDto;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.entity.OrdenTueste;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.enums.EstadoFrescura;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Lotes de café tostado: alimenta el panel de caducidad y el desplegable "Café disponible".
 */
@Service
@Transactional(readOnly = true)
public class LoteTostadoService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------

	// Estado guardado en la base que impide vender el lote aunque le quede stock
	private static final String ESTADO_AGOTADO_EN_BASE = "AGOTADO";

	private final LoteCafeTostadoRepository loteCafeTostadoRepository;
	private final CaducidadService caducidadService;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta las dependencias)
	// ---------------------------------------------------------------------
	public LoteTostadoService(LoteCafeTostadoRepository loteCafeTostadoRepository,
			CaducidadService caducidadService,
			Clock reloj) {
		this.loteCafeTostadoRepository = loteCafeTostadoRepository;
		this.caducidadService = caducidadService;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// LISTAR LOTES
	// ---------------------------------------------------------------------

	/**
	 * Devuelve los lotes con stock, del más antiguo al más nuevo (FIFO).
	 * Si soloDisponibles es true, deja solo los que hoy se pueden vender.
	 */
	public List<LoteTostadoDto> listar(boolean soloDisponibles) {
		LocalDateTime ahora = LocalDateTime.now(reloj);
		List<LoteCafeTostado> lotes = loteCafeTostadoRepository.buscarEnInventario();

		return lotes.stream()
				.map(lote -> aDto(lote, ahora))
				.filter(lote -> !soloDisponibles || lote.disponibleParaVenta())
				.toList();
	}

	// ---------------------------------------------------------------------
	// REGLA: ¿SE PUEDE VENDER HOY?
	// ---------------------------------------------------------------------

	/**
	 * Un lote se puede vender si se cumplen las 4 condiciones:
	 * 1. Ya terminó el reposo (llegó la fecha de disponible para venta).
	 * 2. Le queda stock disponible (no reservado).
	 * 3. No está vencido (por fecha o porque se marcó VENCIDO en la base).
	 * 4. En la base no fue marcado como AGOTADO.
	 * Se calcula con fechas porque todavía no hay un proceso que actualice "estado_disponibilidad" solo.
	 */
	private boolean esVendible(LoteCafeTostado lote, EstadoFrescura frescura, LocalDateTime ahora) {
		boolean terminoReposo = !ahora.isBefore(lote.getFechaDisponibleVenta());
		boolean tieneStock = lote.getStockDisponibleGramos().compareTo(BigDecimal.ZERO) > 0;
		boolean noVencido = frescura != EstadoFrescura.VENCIDO;
		boolean estadoPermiteVenta = !ESTADO_AGOTADO_EN_BASE.equals(lote.getEstadoDisponibilidad());

		return terminoReposo && tieneStock && noVencido && estadoPermiteVenta;
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private LoteTostadoDto aDto(LoteCafeTostado lote, LocalDateTime ahora) {
		LocalDate hoy = ahora.toLocalDate();
		OrdenTueste orden = lote.getOrdenTueste();
		OrigenCafe origen = orden.getLoteVerde().getOrigen();
		LocalDate diaTueste = lote.getFechaHoraFinTueste().toLocalDate();

		long vidaUtilDias = ChronoUnit.DAYS.between(diaTueste, lote.getFechaCaducidad());
		long diasRestantes = caducidadService.calcularDiasRestantes(lote.getFechaCaducidad(), hoy);
		EstadoFrescura frescura = caducidadService.calcularEstadoFrescura(lote, hoy);
		boolean disponibleParaVenta = esVendible(lote, frescura, ahora);

		return new LoteTostadoDto(
				lote.getIdLoteTostado(),
				lote.getCodigoLoteTostado(),
				origen.getPais(),
				origen.getRegion(),
				origen.getVariedad(),
				orden.getPerfilTueste().getNombrePerfil(),
				lote.getFechaHoraFinTueste(),
				lote.getFechaDisponibleVenta(),
				lote.getFechaCaducidad(),
				vidaUtilDias,
				diasRestantes,
				frescura,
				lote.getStockDisponibleGramos(),
				lote.getStockReservadoGramos(),
				lote.getEstadoDisponibilidad(),
				disponibleParaVenta);
	}
}
