package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.HojaRuta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a la tabla "hoja_ruta".
 */
public interface HojaRutaRepository extends JpaRepository<HojaRuta, Integer> {

	// Todas las hojas de ruta, de la más reciente a la más antigua
	List<HojaRuta> findAllByOrderByFechaRepartoDesc();

	// La hoja de un día (hay una sola por fecha)
	Optional<HojaRuta> findByFechaReparto(LocalDate fechaReparto);

	// Crea la hoja de esa fecha solo si todavía no existe (la fecha es UNIQUE en la base).
	// "ON CONFLICT DO NOTHING" evita el error si dos generaciones la crean a la vez; el estado arranca PENDIENTE.
	@Modifying
	@Query(value = "INSERT INTO hoja_ruta (fecha_reparto) VALUES (:fecha) ON CONFLICT (fecha_reparto) DO NOTHING",
			nativeQuery = true)
	void crearSiNoExiste(@Param("fecha") LocalDate fecha);

	// Busca la hoja de una fecha y la bloquea hasta que termine la transacción (SELECT ... FOR UPDATE).
	// Así, dos generaciones simultáneas no le asignan el mismo orden de visita a dos pedidos.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT h FROM HojaRuta h WHERE h.fechaReparto = :fecha")
	Optional<HojaRuta> buscarPorFechaConBloqueo(@Param("fecha") LocalDate fecha);
}
