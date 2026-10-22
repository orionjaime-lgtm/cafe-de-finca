package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.dto.TotalPorOrigen;
import com.cafedefinca.backend.entity.LoteCafeVerde;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Acceso a la tabla "lote_cafe_verde".
 */
public interface LoteCafeVerdeRepository extends JpaRepository<LoteCafeVerde, Integer> {

	// Suma el stock actual de todos los lotes de café verde, agrupado por origen.
	// Los orígenes sin lotes no aparecen en el resultado (el service los toma como 0 g).
	@Query("""
			SELECT new com.cafedefinca.backend.dto.TotalPorOrigen(lv.origen.idOrigen, SUM(lv.stockActualGramos))
			FROM LoteCafeVerde lv
			GROUP BY lv.origen.idOrigen
			""")
	List<TotalPorOrigen> sumarStockPorOrigen();
}
