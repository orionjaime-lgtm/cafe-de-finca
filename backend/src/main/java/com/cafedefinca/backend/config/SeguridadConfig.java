package com.cafedefinca.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SeguridadConfig {
	@Bean
	public PasswordEncoder codificadorDeContrasenas() {
		return new BCryptPasswordEncoder();
	}
}
