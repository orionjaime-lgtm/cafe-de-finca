package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Presentacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a la tabla "presentacion".
 */
public interface PresentacionRepository extends JpaRepository<Presentacion, Integer> {

	// Envases ordenados del más chico al más grande
	List<Presentacion> findAllByOrderByPesoGramosAsc();
}
