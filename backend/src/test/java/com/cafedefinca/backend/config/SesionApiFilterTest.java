package com.cafedefinca.backend.config;

import com.cafedefinca.backend.enums.RolUsuario;
import com.cafedefinca.backend.service.SesionTokenService;
import com.cafedefinca.backend.service.SesionTokenService.DatosSesion;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Prueba qué rutas de la API puede usar cada cargo y que sin sesión válida todo se rechace.
 * Sirve de red de seguridad: el menú del frontend y estos permisos tienen que coincidir.
 * Si una pantalla del menú pide una ruta que el cargo no puede usar, esa pantalla no carga sus datos.
 */
class SesionApiFilterTest {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private SesionTokenService tokens;
	private SesionApiFilter filtro;

	@BeforeEach
	void preparar() {
		tokens = Mockito.mock(SesionTokenService.class);
		filtro = new SesionApiFilter(tokens);
		when(tokens.validar("token-admin")).thenReturn(new DatosSesion(1, RolUsuario.ADMIN, null));
		when(tokens.validar("token-tueste")).thenReturn(new DatosSesion(2, RolUsuario.TUESTE, null));
		when(tokens.validar("token-vendedor")).thenReturn(new DatosSesion(3, RolUsuario.VENDEDOR, null));
		when(tokens.validar("token-cliente")).thenReturn(new DatosSesion(4, RolUsuario.CLIENTE, 6));
	}

	// ---------------------------------------------------------------------
	// SIN SESIÓN
	// ---------------------------------------------------------------------
	@Test
	void sinTokenLaApiResponde401() throws Exception {
		MockHttpServletResponse respuesta = llamar("GET", "/api/alertas", null);

		assertThat(respuesta.getStatus()).isEqualTo(401);
		assertThat(respuesta.getContentAsString()).contains("Iniciá sesión");
	}

	@Test
	void unTokenInvalidoResponde401() throws Exception {
		when(tokens.validar("token-roto")).thenThrow(new SecurityException("Sesión no válida."));

		assertThat(llamar("GET", "/api/alertas", "token-roto").getStatus()).isEqualTo(401);
	}

	@Test
	void elLoginYElRegistroNoPidenSesion() throws Exception {
		assertThat(llamar("POST", "/api/auth/login", null).getStatus()).isEqualTo(200);
		assertThat(llamar("POST", "/api/auth/registro", null).getStatus()).isEqualTo(200);
	}

	// ---------------------------------------------------------------------
	// EL VENDEDOR: LO QUE NECESITAN TODAS SUS PANTALLAS
	// ---------------------------------------------------------------------
	@Test
	void elVendedorPuedeUsarTodasLasRutasQueCargaSuMenu() throws Exception {
		// Su menú: pedidos, cobros, caducidad, clientes, hojas de ruta y configuración
		String[] rutas = { "/api/pedidos", "/api/cobros/b2b", "/api/cobros/b2c", "/api/pagos", "/api/clientes",
				"/api/hojas-ruta", "/api/lotes-tostados", "/api/presentaciones", "/api/tipos-molienda",
				"/api/canales-venta", "/api/medios-pago", "/api/caducidad/resumen" };

		for (String ruta : rutas) {
			assertThat(llamar("GET", ruta, "token-vendedor").getStatus()).as(ruta).isEqualTo(200);
		}
	}

	@Test
	void elVendedorPuedeVerCaducidad() throws Exception {
		assertThat(llamar("GET", "/api/caducidad/resumen", "token-vendedor").getStatus()).isEqualTo(200);
	}

	@Test
	void elVendedorSigueSinPoderTostarNiAdministrarUsuariosNiVerAlertas() throws Exception {
		assertThat(llamar("POST", "/api/ordenes-tueste", "token-vendedor").getStatus()).isEqualTo(403);
		assertThat(llamar("POST", "/api/lotes-verdes", "token-vendedor").getStatus()).isEqualTo(403);
		assertThat(llamar("GET", "/api/usuarios", "token-vendedor").getStatus()).isEqualTo(403);
		assertThat(llamar("PATCH", "/api/usuarios/1", "token-vendedor").getStatus()).isEqualTo(403);
		assertThat(llamar("GET", "/api/alertas", "token-vendedor").getStatus()).isEqualTo(403);
	}

	// ---------------------------------------------------------------------
	// LOS OTROS CARGOS
	// ---------------------------------------------------------------------
	@Test
	void elAdministradorPuedeUsarTodo() throws Exception {
		for (String ruta : new String[] { "/api/usuarios", "/api/ordenes-tueste", "/api/caducidad/resumen", "/api/alertas", "/api/pagos" }) {
			assertThat(llamar("GET", ruta, "token-admin").getStatus()).as(ruta).isEqualTo(200);
		}
	}

	@Test
	void elTostadorUsaProduccionYCaducidadPeroNoVentasNiCobros() throws Exception {
		for (String ruta : new String[] { "/api/lotes-verdes", "/api/ordenes-tueste", "/api/alertas", "/api/caducidad/resumen", "/api/origenes/stock" }) {
			assertThat(llamar("GET", ruta, "token-tueste").getStatus()).as(ruta).isEqualTo(200);
		}
		for (String ruta : new String[] { "/api/pedidos", "/api/cobros/b2b", "/api/usuarios", "/api/hojas-ruta" }) {
			assertThat(llamar("GET", ruta, "token-tueste").getStatus()).as(ruta).isEqualTo(403);
		}
	}

	@Test
	void elClienteNoPuedeVerCaducidadNiCobrosNiUsuarios() throws Exception {
		for (String ruta : new String[] { "/api/caducidad/resumen", "/api/cobros/b2b", "/api/usuarios", "/api/pagos", "/api/ordenes-tueste" }) {
			assertThat(llamar("GET", ruta, "token-cliente").getStatus()).as(ruta).isEqualTo(403);
		}
	}

	@Test
	void unaRutaRechazadaNoLlegaAlControlador() throws Exception {
		FilterChain cadena = Mockito.mock(FilterChain.class);
		MockHttpServletRequest pedido = pedido("GET", "/api/usuarios", "token-vendedor");

		filtro.doFilter(pedido, new MockHttpServletResponse(), cadena);

		verify(cadena, never()).doFilter(any(), any());
	}

	// ---------------------------------------------------------------------
	// AYUDANTES
	// ---------------------------------------------------------------------
	private MockHttpServletRequest pedido(String metodo, String ruta, String token) {
		MockHttpServletRequest pedido = new MockHttpServletRequest(metodo, ruta);
		if (token != null) {
			pedido.addHeader("Authorization", "Bearer " + token);
		}
		return pedido;
	}

	private MockHttpServletResponse llamar(String metodo, String ruta, String token) throws Exception {
		MockHttpServletResponse respuesta = new MockHttpServletResponse();
		filtro.doFilter(pedido(metodo, ruta, token), respuesta, Mockito.mock(FilterChain.class));
		return respuesta;
	}
}
