package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.AuthDto;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.entity.Usuario;
import com.cafedefinca.backend.enums.RolUsuario;
import com.cafedefinca.backend.exception.ConflictoException;
import com.cafedefinca.backend.exception.CredencialesInvalidasException;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.ClienteRepository;
import com.cafedefinca.backend.repository.UsuarioRepository;
import com.cafedefinca.backend.util.ValidadorContrasena;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {
	private final UsuarioRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder codificador;

	public AuthService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository, PasswordEncoder codificador) {
		this.usuarioRepository = usuarioRepository;
		this.clienteRepository = clienteRepository;
		this.codificador = codificador;
	}

	public AuthDto.Sesion iniciarSesion(AuthDto.Login datos) {
		Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(datos.email())).orElseThrow(CredencialesInvalidasException::new);
		boolean contrasenaCorrecta = codificador.matches(datos.password(), usuario.getPasswordHash());
		if (!usuario.isActivo() || !contrasenaCorrecta) {
			throw new CredencialesInvalidasException();
		}
		return AuthDto.Sesion.de(usuario);
	}

	@Transactional
	public AuthDto.Sesion registrar(AuthDto.Registro datos) {
		if (!datos.password().equals(datos.confirmacion())) {
			throw new SolicitudInvalidaException("La contraseña y su confirmación no coinciden.");
		}
		ValidadorContrasena.validar(datos.password());
		String email = normalizarEmail(datos.email());
		String documento = datos.documento().trim();
		String nombre = datos.nombre().trim();
		if (usuarioRepository.existsByEmail(email)) {
			throw new ConflictoException("Ya existe una cuenta con ese email.");
		}
		if (clienteRepository.existsByCuitODni(documento)) {
			throw new ConflictoException("Ya existe un cliente con ese documento. Pedile a la tostaduría que vincule tu cuenta.");
		}
		String contacto = (datos.contacto() == null || datos.contacto().isBlank()) ? null : datos.contacto().trim();
		Cliente cliente = clienteRepository.save(Cliente.nuevoParticular(nombre, documento, contacto));
		Usuario usuario = usuarioRepository.save(new Usuario(email, codificador.encode(datos.password()), nombre, RolUsuario.CLIENTE, cliente.getIdCliente()));
		return AuthDto.Sesion.de(usuario);
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
