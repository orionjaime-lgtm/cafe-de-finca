package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.LoteCafeTostado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Acceso a la tabla "lote_cafe_tostado".
 */
public interface LoteCafeTostadoRepository extends JpaRepository<LoteCafeTostado, Integer> {

	// Lotes tostados que todavía tienen café (disponible o reservado), del más antiguo al más nuevo.
	// El orden "más antiguo primero" es la regla FIFO de la propuesta.
	// JOIN FETCH trae en una sola consulta la orden, el lote verde, el origen y el perfil de tueste.
	@Query("""
			SELECT l
			FROM LoteCafeTostado l
			JOIN FETCH l.ordenTueste o
			JOIN FETCH o.loteVerde lv
			JOIN FETCH lv.origen
			JOIN FETCH o.perfilTueste
			WHERE l.stockDisponibleGramos + l.stockReservadoGramos > 0
			ORDER BY l.fechaHoraFinTueste ASC
			""")
	List<LoteCafeTostado> buscarEnInventario();
}
