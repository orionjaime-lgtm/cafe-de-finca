package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevaOrdenTuesteDto;
import com.cafedefinca.backend.dto.OrdenTuesteDto;
import com.cafedefinca.backend.entity.LoteCafeTostado;
import com.cafedefinca.backend.entity.LoteCafeVerde;
import com.cafedefinca.backend.entity.OrdenTueste;
import com.cafedefinca.backend.entity.OrigenCafe;
import com.cafedefinca.backend.entity.PerfilTueste;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.LoteCafeTostadoRepository;
import com.cafedefinca.backend.repository.LoteCafeVerdeRepository;
import com.cafedefinca.backend.repository.OrdenTuesteRepository;
import com.cafedefinca.backend.repository.PerfilTuesteRepository;
import com.cafedefinca.backend.util.ConversorUnidades;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Órdenes de tueste: listado y registro de un tueste nuevo.
 * Registrar un tueste hace tres cosas en una sola transacción: descuenta el café verde,
 * guarda la orden y crea el lote tostado que sale de ella. Si algo falla, no queda nada a medias.
 */
@Service
@Transactional(readOnly = true)
public class OrdenTuesteService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------

	// Días de vida del café tostado desde el día del tueste (convención a confirmar con el cliente)
	public static final int DIAS_CADUCIDAD = 30;

	// Los códigos de los lotes tostados son LT-1001, LT-1002, ...
	public static final String PREFIJO_CODIGO = "LT-";

	// El primer código es el siguiente a este número (LT-1001)
	public static final long NUMERO_BASE_CODIGOS = 1000;

	// Número fijo que identifica el candado de PostgreSQL usado para generar códigos de a uno por vez
	private static final long CLAVE_CANDADO_CODIGOS = 7300001L;

	private final OrdenTuesteRepository ordenTuesteRepository;
	private final LoteCafeVerdeRepository loteCafeVerdeRepository;
	private final PerfilTuesteRepository perfilTuesteRepository;
	private final LoteCafeTostadoRepository loteCafeTostadoRepository;
	private final EntityManager entityManager;
	private final Clock reloj;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories, el EntityManager y el reloj)
	// ---------------------------------------------------------------------
	public OrdenTuesteService(OrdenTuesteRepository ordenTuesteRepository,
			LoteCafeVerdeRepository loteCafeVerdeRepository,
			PerfilTuesteRepository perfilTuesteRepository,
			LoteCafeTostadoRepository loteCafeTostadoRepository,
			EntityManager entityManager,
			Clock reloj) {
		this.ordenTuesteRepository = ordenTuesteRepository;
		this.loteCafeVerdeRepository = loteCafeVerdeRepository;
		this.perfilTuesteRepository = perfilTuesteRepository;
		this.loteCafeTostadoRepository = loteCafeTostadoRepository;
		this.entityManager = entityManager;
		this.reloj = reloj;
	}

	// ---------------------------------------------------------------------
	// LISTAR ÓRDENES
	// ---------------------------------------------------------------------

	/**
	 * Devuelve las órdenes de la más reciente a la más antigua, cada una con su lote tostado.
	 * Los lotes tostados se traen en una sola consulta para no consultar uno por orden.
	 */
	public List<OrdenTuesteDto> listar() {
		List<OrdenTueste> ordenes = ordenTuesteRepository.buscarTodasConDetalle();
		List<Integer> idsOrdenes = ordenes.stream().map(OrdenTueste::getIdOrdenTueste).toList();
		Map<Integer, LoteCafeTostado> tostadoPorOrden = idsOrdenes.isEmpty()
				? Map.of()
				: loteCafeTostadoRepository.buscarPorOrdenes(idsOrdenes).stream()
						.collect(Collectors.toMap(lote -> lote.getOrdenTueste().getIdOrdenTueste(), Function.identity()));

		return ordenes.stream()
				.map(orden -> aDto(orden, tostadoPorOrden.get(orden.getIdOrdenTueste())))
				.toList();
	}

	// ---------------------------------------------------------------------
	// REGISTRAR UN TUESTE
	// ---------------------------------------------------------------------

	/**
	 * Valida los datos y, en una sola transacción:
	 * descuenta el café verde, guarda la orden y crea el lote tostado en estado REPOSO.
	 * Si el verde no alcanza responde 409 "Stock insuficiente" y no se escribe nada.
	 */
	@Transactional
	public OrdenTuesteDto crear(NuevaOrdenTuesteDto datos) {
		// Bloquea el lote verde: dos tuestes simultáneos sobre el mismo lote se hacen de a uno
		LoteCafeVerde loteVerde = loteCafeVerdeRepository.buscarPorIdConBloqueo(datos.idLoteVerde())
				.orElseThrow(() -> new SolicitudInvalidaException("No existe el lote verde con id " + datos.idLoteVerde() + "."));
		PerfilTueste perfil = perfilTuesteRepository.findById(datos.idPerfilTueste())
				.orElseThrow(() -> new SolicitudInvalidaException("No existe el perfil de tueste con id " + datos.idPerfilTueste() + "."));
		BigDecimal verdeGramos = ConversorUnidades.aGramos(datos.pesoVerdeUsado(), datos.unidad());
		BigDecimal tostadoGramos = ConversorUnidades.aGramos(datos.pesoTostadoObtenido(), datos.unidad());
		LocalDateTime fechaTueste = LocalDateTime.now(reloj);
		LocalDateTime finTueste;
		LocalDateTime disponibleVenta;
		LocalDate fechaCaducidad;
		OrdenTueste orden;
		LoteCafeTostado loteTostado;

		// 1. El tostado nunca pesa más que el verde (la base también lo exige)
		if (tostadoGramos.compareTo(verdeGramos) > 0) {
			throw new SolicitudInvalidaException("El café tostado no puede pesar más que el verde usado.");
		}

		// 2. Descontar el verde (lanza 409 si no alcanza)
		loteVerde.descontarStock(verdeGramos);

		// 3. Calcular las fechas del lote tostado
		finTueste = fechaTueste.plusMinutes(perfil.getTiempoTuesteMin());
		disponibleVenta = finTueste.plusHours(horasDeReposo(perfil));
		fechaCaducidad = fechaTueste.toLocalDate().plusDays(DIAS_CADUCIDAD);

		// 4. Guardar la orden. El refresh vuelve a leerla para traer la merma que calculó la base.
		orden = ordenTuesteRepository.saveAndFlush(new OrdenTueste(
				loteVerde, perfil, fechaTueste, verdeGramos, tostadoGramos, limpiarOperador(datos.operadorTostador())));
		entityManager.refresh(orden);

		// 5. Guardar el lote tostado, en REPOSO y con todo el café disponible
		loteTostado = loteCafeTostadoRepository.save(new LoteCafeTostado(
				orden, generarCodigoLoteTostado(), finTueste, disponibleVenta, fechaCaducidad, tostadoGramos));

		return aDto(orden, loteTostado);
	}

	// ---------------------------------------------------------------------
	// CÓDIGO DEL LOTE TOSTADO (LT-xxxx)
	// ---------------------------------------------------------------------

	/**
	 * Siguiente código libre: el mayor número usado más uno.
	 * El candado hace que dos tuestes simultáneos no calculen el mismo número.
	 * Se suelta solo al terminar la transacción.
	 */
	private String generarCodigoLoteTostado() {
		long ultimo;

		loteCafeTostadoRepository.bloquearGeneracionDeCodigo(CLAVE_CANDADO_CODIGOS);
		ultimo = Math.max(loteCafeTostadoRepository.buscarNumeroMaximoDeCodigo(), NUMERO_BASE_CODIGOS);
		return PREFIJO_CODIGO + (ultimo + 1);
	}

	// ---------------------------------------------------------------------
	// AYUDANTES
	// ---------------------------------------------------------------------

	// Un perfil sin horas de reposo configuradas no tiene reposo
	private int horasDeReposo(PerfilTueste perfil) {
		return perfil.getHorasReposoMinimas() != null ? perfil.getHorasReposoMinimas() : 0;
	}

	// El operador es opcional: vacío o solo espacios se guarda como null
	private String limpiarOperador(String operador) {
		return operador == null || operador.isBlank() ? null : operador.trim();
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private OrdenTuesteDto aDto(OrdenTueste orden, LoteCafeTostado loteTostado) {
		LoteCafeVerde loteVerde = orden.getLoteVerde();
		OrigenCafe origen = loteVerde.getOrigen();
		PerfilTueste perfil = orden.getPerfilTueste();

		return new OrdenTuesteDto(
				orden.getIdOrdenTueste(),
				loteVerde.getIdLoteVerde(),
				loteVerde.getCodigoLote(),
				origen.getPais() + " " + origen.getRegion(),
				perfil.getIdPerfilTueste(),
				perfil.getNombrePerfil(),
				orden.getFechaHoraTueste(),
				orden.getPesoVerdeUsadoG(),
				orden.getPesoTostadoObtenidoG(),
				orden.getMermaRealPct(),
				orden.getOperadorTostador(),
				orden.getEstadoOrden(),
				loteTostado != null ? loteTostado.getCodigoLoteTostado() : null,
				loteTostado != null ? loteTostado.getFechaDisponibleVenta() : null,
				loteTostado != null ? loteTostado.getFechaCaducidad() : null);
	}
}
