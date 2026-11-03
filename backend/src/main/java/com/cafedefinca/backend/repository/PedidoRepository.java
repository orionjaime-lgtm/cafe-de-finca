package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

	// Mayor orden de visita ya asignado en una hoja de ruta (0 si todavía no tiene pedidos)
	@Query("SELECT COALESCE(MAX(p.ordenVisita), 0) FROM Pedido p WHERE p.idHojaRuta = :idHojaRuta")
	Integer buscarUltimoOrdenVisita(@Param("idHojaRuta") Integer idHojaRuta);
}
