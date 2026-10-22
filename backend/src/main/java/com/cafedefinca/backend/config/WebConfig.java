package com.cafedefinca.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Permite que el frontend React (que corre en otro puerto) pueda llamar a la API.
 * Sin esto, el navegador bloquea las peticiones por CORS.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	// ---------------------------------------------------------------------
	// ORÍGENES PERMITIDOS (puertos de desarrollo de React: Vite y Create React App)
	// ---------------------------------------------------------------------
	private static final String[] ORIGENES_FRONTEND = {
			"http://localhost:5173",
			"http://localhost:3000"
	};

	private static final String[] METODOS_PERMITIDOS = { "GET", "POST", "PUT", "PATCH", "DELETE" };

	@Override
	public void addCorsMappings(CorsRegistry registro) {
		registro.addMapping("/api/**")
				.allowedOrigins(ORIGENES_FRONTEND)
				.allowedMethods(METODOS_PERMITIDOS);
	}
}
