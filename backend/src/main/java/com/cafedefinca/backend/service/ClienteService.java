package com.cafedefinca.backend.service;

import com.cafedefinca.backend.dto.ClienteDto;
import com.cafedefinca.backend.entity.Cliente;
import com.cafedefinca.backend.exception.SolicitudInvalidaException;
import com.cafedefinca.backend.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lógica de clientes: listado completo o filtrado por tipo (B2B / B2C).
 */
@Service
@Transactional(readOnly = true)
public class ClienteService {

	// ---------------------------------------------------------------------
	// VARIABLES
	// ---------------------------------------------------------------------
	private static final Set<String> TIPOS_VALIDOS = Set.of("B2B", "B2C");

	private final ClienteRepository clienteRepository;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (Spring inyecta el repository)
	// ---------------------------------------------------------------------
	public ClienteService(ClienteRepository clienteRepository) {
		this.clienteRepository = clienteRepository;
	}

	// ---------------------------------------------------------------------
	// LISTAR CLIENTES
	// ---------------------------------------------------------------------

	/**
	 * Devuelve los clientes ordenados por nombre.
	 * Si "tipo" viene vacío devuelve todos; si viene B2B o B2C, solo los de ese tipo.
	 */
	public List<ClienteDto> listar(String tipo) {
		List<Cliente> clientes;
		String tipoNormalizado;

		// 1. Sin filtro: todos los clientes
		if (tipo == null || tipo.isBlank()) {
			clientes = clienteRepository.findAllByOrderByNombreRazonSocialAsc();
			return clientes.stream().map(this::aDto).toList();
		}

		// 2. Con filtro: validar que el tipo exista
		tipoNormalizado = tipo.trim().toUpperCase(Locale.ROOT);
		if (!TIPOS_VALIDOS.contains(tipoNormalizado)) {
			throw new SolicitudInvalidaException("El tipo de cliente debe ser B2B o B2C, y llegó: " + tipo);
		}

		// 3. Buscar solo los de ese tipo
		clientes = clienteRepository.findByTipoClienteOrderByNombreRazonSocialAsc(tipoNormalizado);
		return clientes.stream().map(this::aDto).toList();
	}

	// ---------------------------------------------------------------------
	// CONVERSIÓN ENTIDAD -> DTO
	// ---------------------------------------------------------------------
	private ClienteDto aDto(Cliente cliente) {
		return new ClienteDto(
				cliente.getIdCliente(),
				cliente.getTipoCliente(),
				cliente.getNombreRazonSocial(),
				cliente.getCuitODni(),
				cliente.getCondicionIva(),
				cliente.getPlazoCtaCteDias(),
				cliente.getLimiteCredito());
	}
}
