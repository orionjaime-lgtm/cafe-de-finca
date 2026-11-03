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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {
	private final PasswordEncoder codificador = new BCryptPasswordEncoder();
	private UsuarioRepository usuarioRepository;
	private ClienteRepository clienteRepository;
	private AuthService servicio;

	@BeforeEach
	void preparar() {
		usuarioRepository = Mockito.mock(UsuarioRepository.class);
		clienteRepository = Mockito.mock(ClienteRepository.class);
		servicio = new AuthService(usuarioRepository, clienteRepository, codificador);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
		when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> {
			Cliente cliente = invocacion.getArgument(0);
			ReflectionTestUtils.setField(cliente, "idCliente", 7);
			return cliente;
		});
	}

	private Usuario cuenta(String email, String contrasena, RolUsuario rol, Integer idCliente) {
		return new Usuario(email, codificador.encode(contrasena), "Persona de prueba", rol, idCliente);
	}

	private AuthDto.Registro registro(String email, String password, String confirmacion) {
		return new AuthDto.Registro("Ana Pérez", "30111999", email, password, confirmacion, null);
	}

	@Test
	void unaCuentaValidaIniciaSesionAunqueElEmailTengaMayusculasYEspacios() {
		when(usuarioRepository.findByEmail("admin@cafedefinca.com"))
				.thenReturn(Optional.of(cuenta("admin@cafedefinca.com", "cafe1234", RolUsuario.ADMIN, null)));
		AuthDto.Sesion sesion = servicio.iniciarSesion(new AuthDto.Login("  Admin@CafeDeFinca.com ", "cafe1234"));
		assertThat(sesion.rol()).isEqualTo(RolUsuario.ADMIN);
		assertThat(sesion.secciones()).contains("usuarios", "pedidos");
	}

	@Test
	void unaContrasenaIncorrectaSeRechaza() {
		when(usuarioRepository.findByEmail("admin@cafedefinca.com"))
				.thenReturn(Optional.of(cuenta("admin@cafedefinca.com", "cafe1234", RolUsuario.ADMIN, null)));
		assertThatThrownBy(() -> servicio.iniciarSesion(new AuthDto.Login("admin@cafedefinca.com", "otra-clave")))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	@Test
	void unEmailInexistenteDaElMismoError() {
		when(usuarioRepository.findByEmail("nadie@cafedefinca.com")).thenReturn(Optional.empty());
		assertThatThrownBy(() -> servicio.iniciarSesion(new AuthDto.Login("nadie@cafedefinca.com", "cafe1234")))
				.isInstanceOf(CredencialesInvalidasException.class)
				.hasMessage("Email o contraseña incorrectos.");
	}

	@Test
	void unaCuentaInactivaNoPuedeEntrar() {
		Usuario inactiva = cuenta("vendedor@cafedefinca.com", "cafe1234", RolUsuario.VENDEDOR, null);
		inactiva.desactivar();
		when(usuarioRepository.findByEmail("vendedor@cafedefinca.com")).thenReturn(Optional.of(inactiva));
		assertThatThrownBy(() -> servicio.iniciarSesion(new AuthDto.Login("vendedor@cafedefinca.com", "cafe1234")))
				.isInstanceOf(CredencialesInvalidasException.class);
	}

	@Test
	void unVisitanteSeRegistraComoClienteYQuedaConLaSesionIniciada() {
		AuthDto.Sesion sesion = servicio.registrar(registro("Ana@Mail.com", "clave1234", "clave1234"));
		assertThat(sesion.rol()).isEqualTo(RolUsuario.CLIENTE);
		assertThat(sesion.idCliente()).isEqualTo(7);
		assertThat(sesion.email()).isEqualTo("ana@mail.com");
		ArgumentCaptor<Cliente> clienteGuardado = ArgumentCaptor.forClass(Cliente.class);
		verify(clienteRepository).save(clienteGuardado.capture());
		assertThat(clienteGuardado.getValue().getTipoCliente()).isEqualTo("B2C");
		ArgumentCaptor<Usuario> usuarioGuardado = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(usuarioGuardado.capture());
		assertThat(usuarioGuardado.getValue().getPasswordHash()).isNotEqualTo("clave1234");
		assertThat(codificador.matches("clave1234", usuarioGuardado.getValue().getPasswordHash())).isTrue();
	}

	@Test
	void unEmailYaRegistradoSeRechazaYNoSeCreaNada() {
		when(usuarioRepository.existsByEmail("ana@mail.com")).thenReturn(true);
		assertThatThrownBy(() -> servicio.registrar(registro("ana@mail.com", "clave1234", "clave1234")))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("email");
		verify(clienteRepository, never()).save(any(Cliente.class));
	}

	@Test
	void unDocumentoQueYaPerteneceAUnClienteSeRechazaSinVincularLaCuenta() {
		when(clienteRepository.existsByCuitODni("30111999")).thenReturn(true);
		assertThatThrownBy(() -> servicio.registrar(registro("ana@mail.com", "clave1234", "clave1234")))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("documento");
		verify(usuarioRepository, never()).save(any(Usuario.class));
	}

	@Test
	void siLaConfirmacionNoCoincideSeRechaza() {
		assertThatThrownBy(() -> servicio.registrar(registro("ana@mail.com", "clave1234", "otra-clave")))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("no coinciden");
	}

	@Test
	void unaContrasenaCortaSeRechaza() {
		assertThatThrownBy(() -> servicio.registrar(registro("ana@mail.com", "corta", "corta")))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("8 caracteres");
	}
}
