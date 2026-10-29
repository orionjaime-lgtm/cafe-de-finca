package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FacturaRepository extends JpaRepository<Factura, Integer> {

	@Query(value = "SELECT CAST(pg_advisory_xact_lock(:clave) AS text)", nativeQuery = true)
	String bloquearGeneracionDeNumero(@Param("clave") long clave);

	@Query(value = """
			SELECT COALESCE(MAX(CAST(numero_comprobante AS BIGINT)), 0)
			FROM factura
			WHERE tipo_comprobante = :tipo
				AND numero_comprobante ~ '^[0-9]+$'
			""", nativeQuery = true)
	long buscarNumeroMaximo(@Param("tipo") String tipoComprobante);
}
