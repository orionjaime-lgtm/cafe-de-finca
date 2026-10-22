package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.OrigenCafe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a la tabla "origen_cafe".
 */
public interface OrigenCafeRepository extends JpaRepository<OrigenCafe, Integer> {

	// Orígenes ordenados por país y región
	List<OrigenCafe> findAllByOrderByPaisAscRegionAsc();
}
