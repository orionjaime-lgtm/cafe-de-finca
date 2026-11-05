package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.MembresiaCliente;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface MembresiaClienteRepository extends JpaRepository<MembresiaCliente, Integer> {

	@Query("""
			SELECT m
			FROM MembresiaCliente m
			JOIN FETCH m.plan
			WHERE m.cliente.idCliente = :idCliente
				AND m.estado = 'ACTIVA'
				AND m.fechaInicio <= :fecha
				AND (m.fechaFin IS NULL OR m.fechaFin >= :fecha)
			""")
	Optional<MembresiaCliente> buscarActivaPorCliente(@Param("idCliente") Integer idCliente,
			@Param("fecha") LocalDate fecha);

	@Query("""
			SELECT m
			FROM MembresiaCliente m
			JOIN FETCH m.cliente
			JOIN FETCH m.plan
			ORDER BY m.fechaInicio DESC, m.idMembresia DESC
			""")
	List<MembresiaCliente> buscarTodasConClienteYPlan();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT m FROM MembresiaCliente m WHERE m.idMembresia = :id")
	Optional<MembresiaCliente> buscarPorIdConBloqueo(@Param("id") Integer idMembresia);

	boolean existsByCliente_IdClienteAndEstado(Integer idCliente, String estado);
}
