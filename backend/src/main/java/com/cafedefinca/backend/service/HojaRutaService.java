package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.HojaRutaDto;
import com.cafedefinca.backend.dto.HojaRutaDto.Parada;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.HojaRuta;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.HojaRutaRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hojas de ruta: el reparto de cada día, con los clientes a visitar en orden y su domicilio.
 * Solo consulta; las hojas y sus pedidos los arma el generador de pedidos de los abonos.
 */
@Service
@Transactional(readOnly = true)
public class HojaRutaService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private final HojaRutaRepository hojaRutaRepository;
	private final PedidoRepository pedidoRepository;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta los repositories)
	// ---------------------------------------------------------------------
	public HojaRutaService(HojaRutaRepository hojaRutaRepository, PedidoRepository pedidoRepository) {
		this.hojaRutaRepository = hojaRutaRepository;
		this.pedidoRepository = pedidoRepository;
	}

	// ---------------------------------------------------------------------
	// LISTAR HOJAS DE RUTA
	// ---------------------------------------------------------------------

	/**
	 * Sin fecha devuelve todas, de la más reciente a la más antigua. Con fecha devuelve solo la de ese día
	 * (como mucho una, porque hay una sola hoja por fecha) o una lista vacía si ese día no hay reparto.
	 */
	public List<HojaRutaDto> listar(LocalDate fecha) {
		List<HojaRuta> hojas = fecha == null
				? hojaRutaRepository.findAllByOrderByFechaRepartoDesc()
				: hojaRutaRepository.findByFechaReparto(fecha).map(List::of).orElse(List.of());

		return armarHojas(hojas);
	}

	// ---------------------------------------------------------------------
	// UNA HOJA DE RUTA
	// ---------------------------------------------------------------------
	public HojaRutaDto obtener(Integer idHojaRuta) {
		HojaRuta hoja = hojaRutaRepository.findById(idHojaRuta)
				.orElseThrow(() -> new SolicitudInvalidaException("No existe la hoja de ruta " + idHojaRuta + "."));

		return armarHojas(List.of(hoja)).get(0);
	}

	// ---------------------------------------------------------------------
	// ARMAR LAS HOJAS CON SUS PARADAS
	// Los pedidos de todas las hojas se traen en una sola consulta, ya ordenados por hoja y orden de visita.
	// ---------------------------------------------------------------------
	private List<HojaRutaDto> armarHojas(List<HojaRuta> hojas) {
		List<Integer> idsHojas = hojas.stream().map(HojaRuta::getIdHojaRuta).toList();
		Map<Integer, List<Pedido>> pedidosPorHoja = idsHojas.isEmpty()
				? Map.of()
				: pedidoRepository.buscarPorHojasConCliente(idsHojas).stream()
						.collect(Collectors.groupingBy(Pedido::getIdHojaRuta));
		List<HojaRutaDto> resultado = new ArrayList<>();

		for (HojaRuta hoja : hojas) {
			List<Parada> paradas = pedidosPorHoja.getOrDefault(hoja.getIdHojaRuta(), List.of()).stream()
					.map(this::aParada)
					.toList();
			resultado.add(new HojaRutaDto(hoja.getIdHojaRuta(), hoja.getFechaReparto(), hoja.getEstadoRuta(), paradas));
		}

		return resultado;
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN PEDIDO -> PARADA
	// ---------------------------------------------------------------------
	private Parada aParada(Pedido pedido) {
		Cliente cliente = pedido.getCliente();

		return new Parada(
				pedido.getOrdenVisita(),
				pedido.getIdPedido(),
				cliente.getIdCliente(),
				cliente.getNombreRazonSocial(),
				cliente.getDomicilio(),
				cliente.getLocalidad(),
				pedido.getMontoTotal(),
				pedido.getEstadoPreparacion());
	}
}
