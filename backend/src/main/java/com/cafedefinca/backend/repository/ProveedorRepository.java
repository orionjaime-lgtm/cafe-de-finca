package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a la tabla "proveedor".
 */
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

	// Proveedores ordenados por razón social
	List<Proveedor> findAllByOrderByRazonSocialAsc();
}
