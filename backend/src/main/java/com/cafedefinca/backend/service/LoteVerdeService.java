package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.LoteVerdeDto;
import com.cafedefinca.backend.dto.NuevoLoteVerdeDto;
import com.cafedefinca.backend.entity.LoteCafeVerde;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.entity.Proveedor;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.LoteCafeVerdeRepository;
import com.cafedefinca.backend.repository.OrigenCafeRepository;
import com.cafedefinca.backend.repository.ProveedorRepository;
import com.cafedefinca.backend.util.ConversorUnidades;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lotes de café verde (materia prima): listado e ingreso de lotes nuevos.
 * El peso llega con su unidad (kg, lb, g) y se guarda siempre en gramos.
 */
@Service
@Transactional(readOnly = true)
public class LoteVerdeService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------

	// Mayor peso que entra en la columna peso_inicial_gramos, que es NUMERIC(12,3)
	public static final BigDecimal MAXIMO_GRAMOS = new BigDecimal("999999999.999");

	private final LoteCafeVerdeRepository loteCafeVerdeRepository;
	private final OrigenCafeRepository origenCafeRepository;
	private final ProveedorRepository proveedorRepository;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories y el reloj)
	// ---------------------------------------------------------------------
	public LoteVerdeService(LoteCafeVerdeRepository loteCafeVerdeRepository,
			OrigenCafeRepository origenCafeRepository,
			ProveedorRepository proveedorRepository,
			Clock reloj) {
		this.loteCafeVerdeRepository = loteCafeVerdeRepository;
		this.origenCafeRepository = origenCafeRepository;
		this.proveedorRepository = proveedorRepository;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// LISTAR LOTES VERDES
	// ---------------------------------------------------------------------

	/**
	 * Devuelve todos los lotes del más antiguo al más nuevo (FIFO).
	 * Los proveedores se traen en una sola consulta para no consultar uno por lote.
	 */
	public List<LoteVerdeDto> listar() {
		List<LoteCafeVerde> lotes = loteCafeVerdeRepository.buscarTodosConOrigen();
		Map<Integer, Proveedor> proveedoresPorId = proveedorRepository.findAll().stream()
				.collect(Collectors.toMap(Proveedor::getIdProveedor, Function.identity()));

		return lotes.stream()
				.map(lote -> aDto(lote, proveedoresPorId.get(lote.getIdProveedor())))
				.toList();
	}

	// ---------------------------------------------------------------------
	// INGRESAR UN LOTE NUEVO
	// ---------------------------------------------------------------------

	/**
	 * Valida los datos, convierte la cantidad a gramos y guarda el lote.
	 * El stock actual arranca igual al peso inicial.
	 * Un código de lote repetido responde 409 con un mensaje claro. Si dos pedidos con el mismo código
	 * llegan a la vez, la restricción UNIQUE de la base deja pasar solo a uno (el otro recibe el 409 genérico).
	 */
	@Transactional
	public LoteVerdeDto crear(NuevoLoteVerdeDto datos) {
		OrigenCafe origen = origenCafeRepository.findById(datos.idOrigen())
				.orElseThrow(() -> new SolicitudInvalidaException("No existe el origen con id " + datos.idOrigen() + "."));
		Proveedor proveedor = proveedorRepository.findById(datos.idProveedor())
				.orElseThrow(() -> new SolicitudInvalidaException("No existe el proveedor con id " + datos.idProveedor() + "."));
		LocalDate hoy = LocalDate.now(reloj);
		LocalDate fechaIngreso = datos.fechaIngreso() != null ? datos.fechaIngreso() : hoy;
		String codigo = datos.codigoLote().trim();
		BigDecimal pesoGramos;
		LoteCafeVerde lote;

		// 0. El código no puede estar repetido
		if (loteCafeVerdeRepository.existsByCodigoLote(codigo)) {
			throw new ConflictoException("Ya existe un lote verde con el código " + codigo + ".");
		}

		// 1. La fecha de ingreso no puede ser futura
		if (fechaIngreso.isAfter(hoy)) {
			throw new SolicitudInvalidaException("La fecha de ingreso no puede ser posterior a hoy.");
		}

		// 2. Convertir la cantidad a gramos (la única conversión de unidades vive en ConversorUnidades)
		pesoGramos = ConversorUnidades.aGramos(datos.cantidad(), datos.unidad());
		if (pesoGramos.signum() <= 0) {
			throw new SolicitudInvalidaException("La cantidad es demasiado pequeña: equivale a 0 gramos.");
		}
		if (pesoGramos.compareTo(MAXIMO_GRAMOS) > 0) {
			throw new SolicitudInvalidaException("La cantidad es demasiado grande: supera el máximo de "
					+ MAXIMO_GRAMOS.toPlainString() + " gramos.");
		}

		// 3. Guardar el lote
		lote = loteCafeVerdeRepository.save(new LoteCafeVerde(
				origen,
				proveedor.getIdProveedor(),
				codigo,
				fechaIngreso,
				datos.cantBolsas(),
				pesoGramos));

		return aDto(lote, proveedor);
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private LoteVerdeDto aDto(LoteCafeVerde lote, Proveedor proveedor) {
		OrigenCafe origen = lote.getOrigen();

		return new LoteVerdeDto(
				lote.getIdLoteVerde(),
				lote.getCodigoLote(),
				origen.getIdOrigen(),
				origen.getPais() + " " + origen.getRegion(),
				lote.getIdProveedor(),
				proveedor != null ? proveedor.getRazonSocial() : null,
				lote.getFechaIngreso(),
				lote.getCantBolsas(),
				lote.getPesoInicialGramos(),
				lote.getStockActualGramos());
	}
}
