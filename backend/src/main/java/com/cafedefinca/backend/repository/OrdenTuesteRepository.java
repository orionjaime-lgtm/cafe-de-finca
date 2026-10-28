package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.dto.TotalPorOrigen;
import com.cafedefinca.backend.entity.OrdenTueste;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a la tabla "orden_tueste".
 */
public interface OrdenTuesteRepository extends JpaRepository<OrdenTueste, Integer> {

	// Gramos de café verde usados en tuestes completados desde una fecha, agrupados por origen.
	// Sirve para calcular la tasa de consumo diario de cada origen (alertas predictivas).
	@Query("""
			SELECT new com.cafedefinca.backend.dto.TotalPorOrigen(lv.origen.idOrigen, SUM(o.pesoVerdeUsadoG))
			FROM OrdenTueste o
			JOIN o.loteVerde lv
			WHERE o.fechaHoraTueste >= :desde
			  AND o.estadoOrden = 'COMPLETADO'
			GROUP BY lv.origen.idOrigen
			""")
	List<TotalPorOrigen> sumarVerdeTostadoPorOrigenDesde(@Param("desde") LocalDateTime desde);

	// Todas las órdenes, de la más reciente a la más antigua.
	// JOIN FETCH trae en una sola consulta el lote verde, su origen y el perfil de tueste.
	@Query("""
			SELECT o
			FROM OrdenTueste o
			JOIN FETCH o.loteVerde lv
			JOIN FETCH lv.origen
			JOIN FETCH o.perfilTueste
			ORDER BY o.fechaHoraTueste DESC, o.idOrdenTueste DESC
			""")
	List<OrdenTueste> buscarTodasConDetalle();
}
