package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.PerfilTueste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a la tabla "perfil_tueste".
 */
public interface PerfilTuesteRepository extends JpaRepository<PerfilTueste, Integer> {

	// Perfiles en el orden en que se cargaron (Claro, Medio, Oscuro)
	List<PerfilTueste> findAllByOrderByIdPerfilTuesteAsc();
}
