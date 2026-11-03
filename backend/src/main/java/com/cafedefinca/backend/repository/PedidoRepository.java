package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

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

	// Mayor orden de visita ya asignado en una hoja de ruta (0 si todavía no tiene pedidos)
	@Query("SELECT COALESCE(MAX(p.ordenVisita), 0) FROM Pedido p WHERE p.idHojaRuta = :idHojaRuta")
	Integer buscarUltimoOrdenVisita(@Param("idHojaRuta") Integer idHojaRuta);
}
