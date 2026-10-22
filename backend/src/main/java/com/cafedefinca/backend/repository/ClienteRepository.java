package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Acceso a la tabla "cliente".
 * Spring genera el SQL a partir del nombre de cada método.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

	// Todos los clientes, ordenados alfabéticamente
	List<Cliente> findAllByOrderByNombreRazonSocialAsc();

	// Solo los clientes de un tipo (B2B o B2C), ordenados alfabéticamente
	List<Cliente> findByTipoClienteOrderByNombreRazonSocialAsc(String tipoCliente);
}
