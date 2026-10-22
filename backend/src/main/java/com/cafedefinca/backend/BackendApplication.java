package com.cafedefinca.backend;

import com.cafedefinca.backend.config.TiempoConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		// Windows con región Argentina le da a Java la zona vieja "America/Buenos_Aires",
		// que PostgreSQL 15 rechaza al conectarse. Se fija el nombre actual antes de arrancar.
		TimeZone.setDefault(TimeZone.getTimeZone(TiempoConfig.ZONA_HORARIA));
		SpringApplication.run(BackendApplication.class, args);
	}

}
