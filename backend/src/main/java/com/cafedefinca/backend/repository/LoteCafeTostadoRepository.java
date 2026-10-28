package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.LoteCafeTostado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

	// Lotes tostados que nacieron de las órdenes indicadas (para mostrar el código en el listado de órdenes)
	@Query("SELECT l FROM LoteCafeTostado l WHERE l.ordenTueste.idOrdenTueste IN :ids")
	List<LoteCafeTostado> buscarPorOrdenes(@Param("ids") Collection<Integer> ids);

	// Pide el candado de PostgreSQL que sirve para generar códigos LT-xxxx: lo toma una sola transacción
	// a la vez y se suelta solo al terminar. Devuelve texto vacío (la función no devuelve nada útil).
	@Query(value = "SELECT CAST(pg_advisory_xact_lock(:clave) AS text)", nativeQuery = true)
	String bloquearGeneracionDeCodigo(@Param("clave") long clave);

	// Mayor número usado en los códigos LT-xxxx (0 si todavía no hay ninguno).
	// Ignora cualquier código que no tenga la forma LT- seguido de números.
	@Query(value = """
			SELECT COALESCE(MAX(CAST(SUBSTRING(codigo_lote_tostado FROM 4) AS BIGINT)), 0)
			FROM lote_cafe_tostado
			WHERE codigo_lote_tostado ~ '^LT-[0-9]+$'
			""", nativeQuery = true)
	long buscarNumeroMaximoDeCodigo();
}
