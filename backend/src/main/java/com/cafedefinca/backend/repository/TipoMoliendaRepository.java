package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.TipoMolienda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a la tabla "tipo_molienda".
 */
public interface TipoMoliendaRepository extends JpaRepository<TipoMolienda, Integer> {

	// Moliendas en el orden en que se cargaron (Grano Entero primero)
	List<TipoMolienda> findAllByOrderByIdTipoMoliendaAsc();
}
