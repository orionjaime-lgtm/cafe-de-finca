package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Pago;
import com.cafedefinca.backend.dto.PagoAcumuladoDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

	@Query("""
			SELECT new com.cafedefinca.backend.dto.PagoAcumuladoDto(p.pedido.idPedido, SUM(p.montoPagado))
			FROM Pago p
			WHERE p.pedido.idPedido IN :ids
			GROUP BY p.pedido.idPedido
			""")
	List<PagoAcumuladoDto> sumarPorPedidos(@Param("ids") Collection<Integer> ids);

	@Query("""
			SELECT new com.cafedefinca.backend.dto.PagoAcumuladoDto(p.pedido.idPedido, SUM(p.montoPagado))
			FROM Pago p
			WHERE p.pedido.idPedido = :idPedido
			GROUP BY p.pedido.idPedido
			""")
	List<PagoAcumuladoDto> sumarPorPedido(@Param("idPedido") Integer idPedido);

	@Query("""
			SELECT p
			FROM Pago p
			JOIN FETCH p.pedido pedido
			JOIN FETCH pedido.cliente
			JOIN FETCH p.medioPago
			WHERE pedido.cliente.tipoCliente = 'B2C'
			ORDER BY p.fechaHoraPago DESC, p.idPago DESC
			""")
	List<Pago> buscarCobrosB2c();
}
