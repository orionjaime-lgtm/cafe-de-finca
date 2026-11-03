package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.CanalVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CanalVentaRepository extends JpaRepository<CanalVenta, Integer> {

	List<CanalVenta> findAllByOrderByIdCanalVentaAsc();

	// Busca un canal por su nombre (por ejemplo "Mayorista (B2B)", el canal de los pedidos de abono)
	Optional<CanalVenta> findByNombreCanal(String nombreCanal);
}
