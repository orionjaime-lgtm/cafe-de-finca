package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.UsuarioDto;
import com.cafedefinca.backend.entity.Usuario;
import com.cafedefinca.backend.enums.RolUsuario;
import com.cafedefinca.backend.exception.ConflictoException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {
	private final PasswordEncoder codificador = new BCryptPasswordEncoder();
	private UsuarioRepository usuarioRepository;
	private ClienteRepository clienteRepository;
	private UsuarioService servicio;

	@BeforeEach
	void preparar() {
		usuarioRepository = Mockito.mock(UsuarioRepository.class);
		clienteRepository = Mockito.mock(ClienteRepository.class);
		servicio = new UsuarioService(usuarioRepository, clienteRepository, codificador);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
	}

	private Usuario usuarioConId(int id, String email, RolUsuario rol, Integer idCliente) {
		Usuario usuario = new Usuario(email, codificador.encode("clave1234"), "Persona " + id, rol, idCliente);
		ReflectionTestUtils.setField(usuario, "idUsuario", id);
		return usuario;
	}

	private UsuarioDto.Nuevo nuevo(RolUsuario rol, Integer idCliente) {
		return new UsuarioDto.Nuevo("Nueva@CafeDeFinca.com", "clave1234", "Persona nueva", rol, idCliente);
	}

	@Test
	void unaCuentaDePersonalSeCreaConLaContrasenaCifrada() {
		UsuarioDto.Resumen creada = servicio.crear(nuevo(RolUsuario.TUESTE, null));
		assertThat(creada.rol()).isEqualTo(RolUsuario.TUESTE);
		assertThat(creada.email()).isEqualTo("nueva@cafedefinca.com");
		assertThat(creada.idCliente()).isNull();
		assertThat(creada.activo()).isTrue();
		ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
		verify(usuarioRepository).save(guardado.capture());
		assertThat(guardado.getValue().getPasswordHash()).isNotEqualTo("clave1234");
	}

	@Test
	void unEmailRepetidoSeRechaza() {
		when(usuarioRepository.existsByEmail("nueva@cafedefinca.com")).thenReturn(true);
		assertThatThrownBy(() -> servicio.crear(nuevo(RolUsuario.TUESTE, null)))
				.isInstanceOf(ConflictoException.class);
	}

	@Test
	void unaCuentaDeClienteSinClienteSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(nuevo(RolUsuario.CLIENTE, null)))
				.isInstanceOf(SolicitudInvalidaException.class);
	}

	@Test
	void unaCuentaDePersonalConClienteSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(nuevo(RolUsuario.TUESTE, 3)))
				.isInstanceOf(SolicitudInvalidaException.class);
	}

	@Test
	void unClienteInexistenteSeRechaza() {
		assertThatThrownBy(() -> servicio.crear(nuevo(RolUsuario.CLIENTE, 3)))
				.isInstanceOf(SolicitudInvalidaException.class)
				.hasMessageContaining("no existe");
	}

	@Test
	void unClienteQueYaTieneCuentaSeRechaza() {
		when(clienteRepository.existsById(3)).thenReturn(true);
		when(usuarioRepository.findByIdCliente(3))
				.thenReturn(Optional.of(usuarioConId(9, "otra@cafedefinca.com", RolUsuario.CLIENTE, 3)));
		assertThatThrownBy(() -> servicio.crear(nuevo(RolUsuario.CLIENTE, 3)))
				.isInstanceOf(ConflictoException.class);
	}

	@Test
	void unaCuentaDeClienteSeCreaVinculadaAlCliente() {
		when(clienteRepository.existsById(3)).thenReturn(true);
		when(usuarioRepository.findByIdCliente(3)).thenReturn(Optional.empty());
		UsuarioDto.Resumen creada = servicio.crear(nuevo(RolUsuario.CLIENTE, 3));
		assertThat(creada.rol()).isEqualTo(RolUsuario.CLIENTE);
		assertThat(creada.idCliente()).isEqualTo(3);
	}

	@Test
	void laUltimaCuentaDeAdministradorNoSePuedeDesactivar() {
		when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioConId(1, "admin@cafedefinca.com", RolUsuario.ADMIN, null)));
		when(usuarioRepository.countByRolAndActivoTrue(RolUsuario.ADMIN)).thenReturn(1L);
		assertThatThrownBy(() -> servicio.modificar(1, new UsuarioDto.Cambio(null, null, false)))
				.isInstanceOf(ConflictoException.class)
				.hasMessageContaining("administrador");
	}

	@Test
	void conOtraCuentaDeAdministradorActivaSePuedeDesactivar() {
		when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuarioConId(1, "admin@cafedefinca.com", RolUsuario.ADMIN, null)));
		when(usuarioRepository.countByRolAndActivoTrue(RolUsuario.ADMIN)).thenReturn(2L);
		UsuarioDto.Resumen resultado = servicio.modificar(1, new UsuarioDto.Cambio(null, null, false));
		assertThat(resultado.activo()).isFalse();
	}

	@Test
	void alPasarDeClienteAPersonalSeLimpiaElClienteVinculado() {
		when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuarioConId(5, "cli@cafedefinca.com", RolUsuario.CLIENTE, 3)));
		UsuarioDto.Resumen resultado = servicio.modificar(5, new UsuarioDto.Cambio(RolUsuario.TUESTE, null, null));
		assertThat(resultado.rol()).isEqualTo(RolUsuario.TUESTE);
		assertThat(resultado.idCliente()).isNull();
	}

	@Test
	void unUsuarioInexistenteSeRechaza() {
		assertThatThrownBy(() -> servicio.modificar(99, new UsuarioDto.Cambio(null, null, true)))
				.isInstanceOf(SolicitudInvalidaException.class);
	}

	@Test
	void unaPeticionSinCambiosSeRechaza() {
		when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuarioConId(5, "cli@cafedefinca.com", RolUsuario.CLIENTE, 3)));
		assertThatThrownBy(() -> servicio.modificar(5, new UsuarioDto.Cambio(null, null, null)))
				.isInstanceOf(SolicitudInvalidaException.class);
	}
}
