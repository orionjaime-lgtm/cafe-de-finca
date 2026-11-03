package com.cafedefinca.backend.repository;

import com.cafedefinca.backend.entity.Usuario;
import com.cafedefinca.backend.enums.RolUsuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	Optional<Usuario> findByEmail(String email);
	boolean existsByEmail(String email);
	Optional<Usuario> findByIdCliente(Integer idCliente);
	long countByRolAndActivoTrue(RolUsuario rol);
	List<Usuario> findAllByOrderByNombreAsc();
}
