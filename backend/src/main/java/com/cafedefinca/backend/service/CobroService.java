package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.CobroB2bDto;
import com.cafedefinca.backend.dto.CobroB2cDto;
import com.cafedefinca.backend.dto.FacturaDto;
import com.cafedefinca.backend.dto.PagoAcumuladoDto;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.entity.Pago;
import com.cafedefinca.backend.repository.PagoRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CobroService {

	private static final int DIAS_POR_VENCER = 7;

	private final PedidoRepository pedidoRepository;
	private final PagoRepository pagoRepository;
	private final Clock reloj;

	public CobroService(PedidoRepository pedidoRepository, PagoRepository pagoRepository, Clock reloj) {
		this.pedidoRepository = pedidoRepository;
		this.pagoRepository = pagoRepository;
		this.reloj = reloj;
	}

	public List<CobroB2bDto> listarB2b() {
		List<Pedido> pedidos = pedidoRepository.buscarPendientesB2b();
		if (pedidos.isEmpty()) {
			return List.of();
		}
		List<Integer> ids = pedidos.stream().map(Pedido::getIdPedido).toList();
		Map<Integer, BigDecimal> pagosPorPedido = new HashMap<>();
		for (PagoAcumuladoDto total : pagoRepository.sumarPorPedidos(ids)) {
			pagosPorPedido.put(total.pedidoId(), total.montoPagado());
		}

		LocalDate hoy = LocalDate.now(reloj);
		return pedidos.stream()
				.map(pedido -> aDtoB2b(pedido, pagosPorPedido.getOrDefault(pedido.getIdPedido(), BigDecimal.ZERO), hoy))
				.toList();
	}

	public List<CobroB2cDto> listarB2c() {
		return pagoRepository.buscarCobrosB2c().stream().map(this::aDtoB2c).toList();
	}

	private CobroB2bDto aDtoB2b(Pedido pedido, BigDecimal totalPagado, LocalDate hoy) {
		var factura = pedido.getFactura();
		LocalDate vencimiento = factura.getFechaVencimiento();
		String estado = vencimiento == null || vencimiento.isAfter(hoy.plusDays(DIAS_POR_VENCER))
				? "AL_DIA"
				: vencimiento.isBefore(hoy) ? "VENCIDO" : "POR_VENCER";
		FacturaDto facturaDto = new FacturaDto(
				factura.getIdFactura(),
				factura.getTipoComprobante(),
				factura.getNumeroComprobante(),
				factura.getFechaEmision(),
				vencimiento,
				factura.getMontoTotal());
		return new CobroB2bDto(
				pedido.getIdPedido(),
				pedido.getCliente().getIdCliente(),
				pedido.getCliente().getNombreRazonSocial(),
				pedido.getMontoTotal().subtract(totalPagado),
				facturaDto,
				vencimiento,
				estado);
	}

	private CobroB2cDto aDtoB2c(Pago pago) {
		var pedido = pago.getPedido();
		return new CobroB2cDto(
				pago.getIdPago(),
				pedido.getIdPedido(),
				pedido.getCliente().getIdCliente(),
				pedido.getCliente().getNombreRazonSocial(),
				pago.getFechaHoraPago(),
				pago.getMontoPagado(),
				pago.getMedioPago().getIdMedioPago(),
				pago.getMedioPago().getNombreMedio());
	}
}
