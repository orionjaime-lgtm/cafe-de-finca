package com.cafedefinca.backend.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;

@Configuration
public class DeserializacionConfig {
	@Bean
	JsonMapperBuilderCustomizer rechazarDecimalesEnEnteros() {
		return builder -> builder.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
	}
}
