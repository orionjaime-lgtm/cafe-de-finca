package com.cafedefinca.backend.enums;

import java.util.List;

public enum RolUsuario {
	ADMIN("alertas", "inventario", "tueste", "pedidos", "cobros", "caducidad", "clientes", "compras", "abonos", "hojas-ruta", "usuarios", "configuracion"),
	TUESTE("alertas", "inventario", "tueste", "caducidad", "configuracion"),
	VENDEDOR("pedidos", "cobros", "caducidad", "clientes", "hojas-ruta", "configuracion"),
	CLIENTE("catalogo", "compras", "configuracion");

	private final List<String> secciones;

	RolUsuario(String... secciones) {
		this.secciones = List.of(secciones);
	}

	public List<String> getSecciones() {
		return secciones;
	}
}
