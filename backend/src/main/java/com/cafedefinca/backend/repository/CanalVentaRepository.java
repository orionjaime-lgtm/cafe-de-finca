package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.CanalVenta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CanalVentaRepository extends JpaRepository<CanalVenta, Integer> {

	List<CanalVenta> findAllByOrderByIdCanalVentaAsc();
}
