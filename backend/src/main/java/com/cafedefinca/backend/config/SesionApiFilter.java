package com.cafedefinca.backend.config;

import com.cafedefinca.backend.enums.RolUsuario;
import com.cafedefinca.backend.service.SesionTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@ConditionalOnProperty(name = "app.sesion.habilitada", havingValue = "true", matchIfMissing = true)
public class SesionApiFilter extends OncePerRequestFilter {
	private final SesionTokenService sesionTokenService;

	public SesionApiFilter(SesionTokenService sesionTokenService) {
		this.sesionTokenService = sesionTokenService;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String ruta = request.getRequestURI();
		return !ruta.startsWith("/api/") || ruta.startsWith("/api/auth/") || "OPTIONS".equalsIgnoreCase(request.getMethod());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String origen = request.getHeader("Origin");
		if ("http://localhost:3000".equals(origen) || "http://localhost:5173".equals(origen)) {
			response.setHeader("Access-Control-Allow-Origin", origen);
			response.setHeader("Vary", "Origin");
		}
		String autorizacion = request.getHeader("Authorization");
		if (autorizacion == null || !autorizacion.startsWith("Bearer ")) {
			escribirError(response, HttpServletResponse.SC_UNAUTHORIZED, "Iniciá sesión para usar la API.");
			return;
		}
		try {
			SesionTokenService.DatosSesion sesion = sesionTokenService.validar(autorizacion.substring(7));
			if (!puedeAcceder(request, sesion.rol())) {
				escribirError(response, HttpServletResponse.SC_FORBIDDEN, "Tu rol no tiene permiso para realizar esta operación.");
				return;
			}
			request.setAttribute("sesionUsuarioId", sesion.idUsuario());
			request.setAttribute("sesionClienteId", sesion.idCliente());
			request.setAttribute("sesionRol", sesion.rol());
			chain.doFilter(request, response);
		} catch (SecurityException e) {
			escribirError(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
		}
	}

	private boolean puedeAcceder(HttpServletRequest request, RolUsuario rol) {
		if (rol == RolUsuario.ADMIN) return true;
		String ruta = request.getRequestURI();
		String metodo = request.getMethod();
		if (rol == RolUsuario.TUESTE) {
			return ruta.equals("/api/alertas") || ruta.startsWith("/api/lotes-verdes") || ruta.startsWith("/api/lotes-tostados")
				|| ruta.startsWith("/api/ordenes-tueste") || ruta.startsWith("/api/origenes") || ruta.startsWith("/api/proveedores")
				|| ruta.startsWith("/api/perfiles-tueste") || ruta.startsWith("/api/caducidad") || ruta.startsWith("/api/presentaciones")
				|| ruta.startsWith("/api/tipos-molienda") || ruta.startsWith("/api/canales-venta") || ruta.startsWith("/api/medios-pago");
		}
		if (rol == RolUsuario.VENDEDOR) {
			return ruta.startsWith("/api/pedidos") || ruta.startsWith("/api/cobros") || ruta.startsWith("/api/pagos")
				|| ruta.startsWith("/api/clientes") || ruta.startsWith("/api/hojas-ruta") || ruta.startsWith("/api/suscripciones")
				|| ruta.startsWith("/api/caducidad")
				|| ruta.startsWith("/api/membresias") || ruta.startsWith("/api/planes-suscripcion")
				|| ruta.startsWith("/api/presentaciones") || ruta.startsWith("/api/tipos-molienda") || ruta.startsWith("/api/lotes-tostados")
				|| ruta.startsWith("/api/canales-venta") || ruta.startsWith("/api/medios-pago") || ruta.equals("/api/usuarios/listar");
		}
		if (rol == RolUsuario.CLIENTE) {
			return ruta.startsWith("/api/pedidos/cotizar") || ruta.startsWith("/api/pedidos") || ruta.startsWith("/api/pagos") || ruta.startsWith("/api/clientes")
				|| ruta.startsWith("/api/membresias")
				|| ruta.startsWith("/api/presentaciones") || ruta.startsWith("/api/tipos-molienda") || ruta.startsWith("/api/lotes-tostados")
				|| ruta.startsWith("/api/canales-venta") || ruta.startsWith("/api/medios-pago");
		}
		return false;
	}

	private void escribirError(HttpServletResponse response, int estado, String mensaje) throws IOException {
		response.setStatus(estado);
		response.setContentType("application/json;charset=UTF-8");
		String json = "{\"estado\":" + estado + ",\"mensaje\":" + componerCadenaJson(mensaje) + "}";
		response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
	}

	private String componerCadenaJson(String valor) {
		return "\"" + valor.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
}
