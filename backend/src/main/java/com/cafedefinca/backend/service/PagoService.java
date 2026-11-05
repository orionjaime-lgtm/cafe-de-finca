package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.NuevoPagoDto;
import com.cafedefinca.backend.dto.PagoDto;
import com.cafedefinca.backend.entity.MedioPago;
import com.cafedefinca.backend.entity.Pago;
import com.cafedefinca.backend.entity.Pedido;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.MedioPagoRepository;
import com.cafedefinca.backend.repository.PagoRepository;
import com.cafedefinca.backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class PagoService {

	private final PagoRepository pagoRepository;
	private final PedidoRepository pedidoRepository;
	private final MedioPagoRepository medioPagoRepository;
	private final Clock reloj;

	public PagoService(PagoRepository pagoRepository, PedidoRepository pedidoRepository,
			MedioPagoRepository medioPagoRepository, Clock reloj) {
		this.pagoRepository = pagoRepository;
		this.pedidoRepository = pedidoRepository;
		this.medioPagoRepository = medioPagoRepository;
		this.reloj = reloj;
	}

	@Transactional
	public PagoDto registrar(NuevoPagoDto datos) {
		return registrar(datos, null);
	}

	@Transactional
	public PagoDto registrar(NuevoPagoDto datos, Integer clienteId) {
		Pedido pedido = pedidoRepository.buscarPorIdConBloqueo(datos.pedidoId())
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el pedido con id " + datos.pedidoId() + "."));
		if (clienteId != null && !pedido.getCliente().getIdCliente().equals(clienteId)) {
			throw new SolicitudInvalidaException("Solo podés pagar pedidos asociados a tu cuenta.");
		}
		if ("PAGADO".equals(pedido.getEstadoCobro()) || "REEMBOLSADO".equals(pedido.getEstadoCobro())) {
			throw new ConflictoException("El pedido no tiene saldo pendiente.");
		}
		MedioPago medioPago = medioPagoRepository.findById(datos.medioPagoId())
				.orElseThrow(() -> new SolicitudInvalidaException(
						"No existe el medio de pago con id " + datos.medioPagoId() + "."));
		BigDecimal totalPagado = pagoRepository.sumarPorPedido(pedido.getIdPedido()).stream()
				.map(total -> total.montoPagado())
				.findFirst()
				.orElse(BigDecimal.ZERO);
		BigDecimal saldo = pedido.getMontoTotal().subtract(totalPagado);
		if (datos.monto().compareTo(saldo) > 0) {
			throw new ConflictoException("El pago supera el saldo pendiente de " + saldo + ".");
		}

		Pago pago = pagoRepository.save(new Pago(pedido, medioPago, LocalDateTime.now(reloj), datos.monto()));
		pedido.actualizarEstadoCobro(totalPagado.add(datos.monto()));
		return new PagoDto(
				pago.getIdPago(),
				pedido.getIdPedido(),
				medioPago.getIdMedioPago(),
				medioPago.getNombreMedio(),
				pago.getFechaHoraPago(),
				pago.getMontoPagado(),
				pedido.getEstadoCobro());
	}
}
