package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.UsuarioDto;
import com.cafedefinca.backend.entity.Usuario;
import com.cafedefinca.backend.enums.RolUsuario;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.UsuarioRepository;
import com.cafedefinca.backend.util.ValidadorContrasena;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
	private final UsuarioRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder codificador;

	public UsuarioService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, PasswordEncoder codificador) {
		this.usuarioRepository = usuarioRepository;
		this.clienteRepository = clienteRepository;
		this.codificador = codificador;
	}

	public List<UsuarioDto.Resumen> listar() {
		return usuarioRepository.findAllByOrderByNombreAsc().stream().map(UsuarioDto.Resumen::de).toList();
	}

	@Transactional
	public UsuarioDto.Resumen crear(UsuarioDto.Nuevo datos) {
		String email = datos.email().trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existsByEmail(email)) {
			throw new ConflictoException("Ya existe una cuenta con ese email.");
		}
		ValidadorContrasena.validar(datos.password());
		validarVinculoConCliente(datos.rol(), datos.idCliente(), null);
		Usuario usuario = usuarioRepository.save(new Usuario(email, codificador.encode(datos.password()), datos.nombre().trim(), datos.rol(), datos.idCliente()));
		return UsuarioDto.Resumen.de(usuario);
	}

	@Transactional
	public UsuarioDto.Resumen modificar(Integer idUsuario, UsuarioDto.Cambio cambios) {
		Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new SolicitudInvalidaException("No existe el usuario " + idUsuario + "."));
		if (cambios.rol() == null && cambios.activo() == null) {
			throw new SolicitudInvalidaException("No se indicó ningún cambio.");
		}
		if (cambios.rol() == null && cambios.idCliente() != null) {
			throw new SolicitudInvalidaException("Para cambiar el cliente vinculado hay que indicar también el cargo.");
		}
		boolean dejaDeSerAdminActivo = usuario.getRol() == RolUsuario.ADMIN && usuario.isActivo() && ((cambios.rol() != null && cambios.rol() != RolUsuario.ADMIN) || Boolean.FALSE.equals(cambios.activo()));
		if (dejaDeSerAdminActivo && usuarioRepository.countByRolAndActivoTrue(RolUsuario.ADMIN) <= 1) {
			throw new ConflictoException("No se puede dejar al sistema sin una cuenta de administrador activa.");
		}
		if (cambios.rol() != null) {
			Integer idCliente = cambios.rol() == RolUsuario.CLIENTE ? cambios.idCliente() : null;
			validarVinculoConCliente(cambios.rol(), idCliente, usuario.getIdUsuario());
			usuario.cambiarCargo(cambios.rol(), idCliente);
		}
		if (cambios.activo() != null) {
			if (cambios.activo()) {
				usuario.activar();
			} else {
				usuario.desactivar();
			}
		}
		return UsuarioDto.Resumen.de(usuarioRepository.save(usuario));
	}

	private void validarVinculoConCliente(RolUsuario rol, Integer idCliente, Integer idUsuarioActual) {
		if (rol != RolUsuario.CLIENTE) {
			if (idCliente != null) {
				throw new SolicitudInvalidaException("Solo las cuentas con cargo cliente se vinculan a un cliente.");
			}
			return;
		}
		if (idCliente == null) {
			throw new SolicitudInvalidaException("Una cuenta de cliente tiene que estar vinculada a un cliente.");
		}
		if (!clienteRepository.existsById(idCliente)) {
			throw new SolicitudInvalidaException("El cliente indicado no existe.");
		}
		boolean yaTieneOtraCuenta = usuarioRepository.findByIdCliente(idCliente).filter(otra -> !Objects.equals(otra.getIdUsuario(), idUsuarioActual)).isPresent();
		if (yaTieneOtraCuenta) {
			throw new ConflictoException("Ese cliente ya tiene una cuenta.");
		}
	}
}
