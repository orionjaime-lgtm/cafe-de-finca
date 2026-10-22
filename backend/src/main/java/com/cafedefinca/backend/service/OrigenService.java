package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.OrigenStockDto;
import com.cafedefinca.backend.dto.TotalPorOrigen;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.enums.EstadoStock;
import com.cafedefinca.backend.repository.LoteCafeVerdeRepository;
import com.cafedefinca.backend.repository.OrigenCafeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Stock de café verde por origen, comparado con el stock mínimo de cada uno.
 * Alimenta la tabla "Límites de Stock Mínimo" y es la base de las alertas.
 */
@Service
@Transactional(readOnly = true)
public class OrigenService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final OrigenCafeRepository origenCafeRepository;
	private final LoteCafeVerdeRepository loteCafeVerdeRepository;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories)
	// ---------------------------------------------------------------------
	public OrigenService(OrigenCafeRepository origenCafeRepository,
			LoteCafeVerdeRepository loteCafeVerdeRepository) {
		this.origenCafeRepository = origenCafeRepository;
		this.loteCafeVerdeRepository = loteCafeVerdeRepository;
	}

	// ---------------------------------------------------------------------
	// STOCK POR ORIGEN
	// ---------------------------------------------------------------------

	/**
	 * Devuelve cada origen con la suma del stock de sus lotes de café verde.
	 * Un origen sin lotes cuenta como 0 gramos.
	 */
	public List<OrigenStockDto> listarStock() {
		List<OrigenCafe> origenes = origenCafeRepository.findAllByOrderByPaisAscRegionAsc();
		Map<Integer, BigDecimal> stockPorOrigen = loteCafeVerdeRepository.sumarStockPorOrigen().stream()
				.collect(Collectors.toMap(TotalPorOrigen::idOrigen, TotalPorOrigen::totalGramos));

		return origenes.stream()
				.map(origen -> aDto(origen, stockPorOrigen.getOrDefault(origen.getIdOrigen(), BigDecimal.ZERO)))
				.toList();
	}

	// ---------------------------------------------------------------------
	// REGLA: ESTADO DEL STOCK
	// ---------------------------------------------------------------------

	/**
	 * OK si el stock alcanza el mínimo; BAJO si no lo alcanza; CRITICO si además está en 0.
	 * Un origen con mínimo 0 nunca entra en alerta.
	 */
	public EstadoStock calcularEstado(BigDecimal stockGramos, BigDecimal minimoGramos) {
		if (stockGramos.compareTo(minimoGramos) >= 0) {
			return EstadoStock.OK;
		}
		if (stockGramos.compareTo(BigDecimal.ZERO) == 0) {
			return EstadoStock.CRITICO;
		}
		return EstadoStock.BAJO;
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private OrigenStockDto aDto(OrigenCafe origen, BigDecimal stockGramos) {
		BigDecimal minimoGramos = origen.getStockMinimoAlertaGramos() != null
				? origen.getStockMinimoAlertaGramos()
				: BigDecimal.ZERO;

		return new OrigenStockDto(
				origen.getIdOrigen(),
				origen.getPais(),
				origen.getRegion(),
				origen.getVariedad(),
				stockGramos,
				minimoGramos,
				calcularEstado(stockGramos, minimoGramos));
	}
}
