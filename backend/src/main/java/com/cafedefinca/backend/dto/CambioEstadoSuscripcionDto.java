package com.cafedefinca.backend.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del PATCH de una suscripción: el estado al que se quiere pasar.
 * PAUSADA para pausar, ACTIVA para reactivar y CANCELADA para cancelar.
 */
public record CambioEstadoSuscripcionDto(@NotBlank String estado) {
}
