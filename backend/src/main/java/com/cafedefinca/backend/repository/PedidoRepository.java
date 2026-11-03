package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Pedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

	// Los pedidos de las hojas de ruta indicadas, con su cliente (nombre y domicilio), ordenados por hoja y
	// por orden de visita. Una sola consulta para todas las hojas, en lugar de una por pedido.
	// La factura se trae en la misma consulta a propósito: Pedido.factura es el lado inverso de un
	// @OneToOne y Hibernate no lo puede cargar "perezoso", así que sin esto haría una consulta por pedido.
	@Query("""
			SELECT p
			FROM Pedido p
			JOIN FETCH p.cliente
			LEFT JOIN FETCH p.factura
			WHERE p.idHojaRuta IN :ids
			ORDER BY p.idHojaRuta, p.ordenVisita, p.idPedido
			""")
	List<Pedido> buscarPorHojasConCliente(@Param("ids") Collection<Integer> ids);

	@Query("""
			SELECT DISTINCT p
			FROM Pedido p
			JOIN FETCH p.cliente
			JOIN FETCH p.canalVenta
			LEFT JOIN FETCH p.factura
			LEFT JOIN FETCH p.detalles d
			LEFT JOIN FETCH d.loteTostado
			LEFT JOIN FETCH d.presentacion
			LEFT JOIN FETCH d.tipoMolienda
			ORDER BY p.fechaHoraPedido DESC, p.idPedido DESC
			""")
	List<Pedido> buscarTodosConDetalle();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT p FROM Pedido p WHERE p.idPedido = :id")
	Optional<Pedido> buscarPorIdConBloqueo(@Param("id") Integer idPedido);

	@Query("""
			SELECT DISTINCT p
			FROM Pedido p
			JOIN FETCH p.cliente
			JOIN FETCH p.factura
			WHERE p.cliente.tipoCliente = 'B2B'
				AND p.estadoCobro IN ('PENDIENTE', 'PARCIAL')
			ORDER BY p.factura.fechaVencimiento, p.idPedido
			""")
	List<Pedido> buscarPendientesB2b();

	// Mayor orden de visita ya asignado en una hoja de ruta (0 si todavía no tiene pedidos)
	@Query("SELECT COALESCE(MAX(p.ordenVisita), 0) FROM Pedido p WHERE p.idHojaRuta = :idHojaRuta")
	Integer buscarUltimoOrdenVisita(@Param("idHojaRuta") Integer idHojaRuta);
}
