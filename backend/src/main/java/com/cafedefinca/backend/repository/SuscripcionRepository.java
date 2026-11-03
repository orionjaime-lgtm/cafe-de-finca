package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Suscripcion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla "suscripcion".
 */
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer> {

	// Todas las suscripciones con su cliente y sus renglones, en una sola consulta (sin una consulta por abono)
	@Query("""
			SELECT DISTINCT s
			FROM Suscripcion s
			JOIN FETCH s.cliente
			LEFT JOIN FETCH s.detalles d
			LEFT JOIN FETCH d.presentacion
			LEFT JOIN FETCH d.tipoMolienda
			ORDER BY s.idSuscripcion
			""")
	List<Suscripcion> buscarTodasConDetalle();

	// Busca una suscripción y la bloquea hasta que termine la transacción (SELECT ... FOR UPDATE).
	// Así, un cambio de estado y la generación de pedidos no pueden pisarse sobre la misma suscripción.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Suscripcion s JOIN FETCH s.cliente WHERE s.idSuscripcion = :id")
	Optional<Suscripcion> buscarPorIdConBloqueo(@Param("id") Integer id);
}
