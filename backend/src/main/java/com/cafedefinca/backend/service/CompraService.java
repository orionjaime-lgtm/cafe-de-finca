package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.CompraDto;
import com.cafedefinca.backend.dto.NuevaCompraDto;
import com.cafedefinca.backend.dto.NuevoPagoDto;
import com.cafedefinca.backend.dto.PedidoDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompraService {
	private final PedidoService pedidoService;
	private final PagoService pagoService;

	public CompraService(PedidoService pedidoService, PagoService pagoService) {
		this.pedidoService = pedidoService;
		this.pagoService = pagoService;
	}

	@Transactional
	public CompraDto procesar(NuevaCompraDto datos, Integer clienteId) {
		PedidoDto pedido = pedidoService.crearCompra(datos.pedido(), clienteId);
		var pago = pagoService.registrar(new NuevoPagoDto(
				pedido.idPedido(), datos.medioPagoId(), pedido.montoTotal()), clienteId);
		return new CompraDto(pedido, pago);
	}
}
