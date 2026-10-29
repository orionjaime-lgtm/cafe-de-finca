package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.PresentacionDto;
import com.cafedefinca.backend.dto.TipoMoliendaDto;
import com.cafedefinca.backend.dto.CanalVentaDto;
import com.cafedefinca.backend.dto.MedioPagoDto;
import com.cafedefinca.backend.entity.CanalVenta;
import com.cafedefinca.backend.entity.MedioPago;
import com.cafedefinca.backend.entity.Presentacion;
import com.cafedefinca.backend.entity.TipoMolienda;
import com.cafedefinca.backend.repository.CanalVentaRepository;
import com.cafedefinca.backend.repository.MedioPagoRepository;
import com.cafedefinca.backend.repository.PresentacionRepository;
import com.cafedefinca.backend.repository.TipoMoliendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Catálogo de venta: presentaciones (envases) y tipos de molienda.
 * Son datos fijos que alimentan los desplegables del formulario de pedidos.
 */
@Service
@Transactional(readOnly = true)
public class CatalogoService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final PresentacionRepository presentacionRepository;
	private final TipoMoliendaRepository tipoMoliendaRepository;
	private final CanalVentaRepository canalVentaRepository;
	private final MedioPagoRepository medioPagoRepository;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories)
	// ---------------------------------------------------------------------
	public CatalogoService(PresentacionRepository presentacionRepository,
			TipoMoliendaRepository tipoMoliendaRepository,
			CanalVentaRepository canalVentaRepository,
			MedioPagoRepository medioPagoRepository) {
		this.presentacionRepository = presentacionRepository;
		this.tipoMoliendaRepository = tipoMoliendaRepository;
		this.canalVentaRepository = canalVentaRepository;
		this.medioPagoRepository = medioPagoRepository;
	}

	// ---------------------------------------------------------------------
	// PRESENTACIONES
	// ---------------------------------------------------------------------
	public List<PresentacionDto> listarPresentaciones() {
		List<Presentacion> presentaciones = presentacionRepository.findAllByOrderByPesoGramosAsc();
		return presentaciones.stream().map(this::aDto).toList();
	}

	// ---------------------------------------------------------------------
	// TIPOS DE MOLIENDA
	// ---------------------------------------------------------------------
	public List<TipoMoliendaDto> listarTiposMolienda() {
		List<TipoMolienda> moliendas = tipoMoliendaRepository.findAllByOrderByIdTipoMoliendaAsc();
		return moliendas.stream().map(this::aDto).toList();
	}

	// ---------------------------------------------------------------------
	// CANALES DE VENTA
	// ---------------------------------------------------------------------
	public List<CanalVentaDto> listarCanalesVenta() {
		return canalVentaRepository.findAllByOrderByIdCanalVentaAsc().stream()
				.map(this::aDto)
				.toList();
	}

	// ---------------------------------------------------------------------
	// MEDIOS DE PAGO
	// ---------------------------------------------------------------------
	public List<MedioPagoDto> listarMediosPago() {
		return medioPagoRepository.findAllByOrderByIdMedioPagoAsc().stream()
				.map(this::aDto)
				.toList();
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private PresentacionDto aDto(Presentacion presentacion) {
		return new PresentacionDto(
				presentacion.getIdPresentacion(),
				presentacion.getNombreEnvase(),
				presentacion.getPesoGramos());
	}

	private TipoMoliendaDto aDto(TipoMolienda molienda) {
		return new TipoMoliendaDto(
				molienda.getIdTipoMolienda(),
				molienda.getNombreMolienda(),
				molienda.getTipoCafetera(),
				molienda.getNivelGranulometria(),
				molienda.getEsGranoEntero());
	}

	private CanalVentaDto aDto(CanalVenta canal) {
		return new CanalVentaDto(
				canal.getIdCanalVenta(),
				canal.getNombreCanal(),
				canal.getRequiereDatosFiscales(),
				canal.getPermiteCtaCte());
	}

	private MedioPagoDto aDto(MedioPago medioPago) {
		return new MedioPagoDto(
				medioPago.getIdMedioPago(),
				medioPago.getNombreMedio(),
				medioPago.getRequiereComprobante());
	}
}
