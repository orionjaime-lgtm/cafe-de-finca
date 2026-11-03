package com.cafedefinca.backend.entity;

import com.cafedefinca.backend.enums.EstadoSuscripcion;
import com.cafedefinca.backend.enums.FrecuenciaSuscripcion;
import com.cafedefinca.backend.exception.ConflictoException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla "suscripcion": abono recurrente de un cliente B2B.
 * Genera un pedido cada semana o cada mes con los renglones y precios pactados.
 * "proxima_fecha_generacion" es la fecha de entrega del próximo pedido.
 */
@Entity
@Table(name = "suscripcion")
public class Suscripcion {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_suscripcion")
	private Integer idSuscripcion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	@Enumerated(EnumType.STRING)
	@Column(name = "frecuencia", nullable = false)
	private FrecuenciaSuscripcion frecuencia;

	@Column(name = "dia_entrega", nullable = false)
	private Integer diaEntrega;

	@Column(name = "fecha_inicio", nullable = false)
	private LocalDate fechaInicio;

	@Column(name = "fecha_fin")
	private LocalDate fechaFin;

	@Column(name = "proxima_fecha_generacion", nullable = false)
	private LocalDate proximaFechaGeneracion;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado")
	private EstadoSuscripcion estado;

	@OneToMany(mappedBy = "suscripcion", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
	private List<DetalleSuscripcion> detalles = new ArrayList<>();

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected Suscripcion() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO: un abono nuevo, siempre ACTIVO
	// ---------------------------------------------------------------------
	public Suscripcion(Cliente cliente, FrecuenciaSuscripcion frecuencia, Integer diaEntrega,
			LocalDate fechaInicio, LocalDate fechaFin, LocalDate proximaFechaGeneracion) {
		this.cliente = cliente;
		this.frecuencia = frecuencia;
		this.diaEntrega = diaEntrega;
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.proximaFechaGeneracion = proximaFechaGeneracion;
		this.estado = EstadoSuscripcion.ACTIVA;
	}

	// ---------------------------------------------------------------------
	// MÉTODO DE NEGOCIO: AGREGAR UN RENGLÓN (presentación + molienda + cantidad + precio pactado)
	// ---------------------------------------------------------------------
	public void agregarRenglon(Presentacion presentacion, TipoMolienda tipoMolienda,
			Integer cantidadEnvases, BigDecimal precioUnitario) {
		detalles.add(new DetalleSuscripcion(this, presentacion, tipoMolienda, cantidadEnvases, precioUnitario));
	}

	// ---------------------------------------------------------------------
	// REGLA: ¿LE TOCA GENERAR UN PEDIDO HOY?
	// Tiene que estar ACTIVA, con la fecha de entrega ya vencida (hoy o antes)
	// y sin haber pasado su fecha de fin.
	// ---------------------------------------------------------------------
	public boolean debeGenerarPedido(LocalDate hoy) {
		boolean vencida = !proximaFechaGeneracion.isAfter(hoy);
		boolean dentroDeVigencia = fechaFin == null || !proximaFechaGeneracion.isAfter(fechaFin);
		return estado == EstadoSuscripcion.ACTIVA && vencida && dentroDeVigencia;
	}

	// ---------------------------------------------------------------------
	// MÉTODO DE NEGOCIO: AVANZAR LA PRÓXIMA ENTREGA (después de generar un pedido)
	// ---------------------------------------------------------------------
	public void avanzarProximaFecha() {
		this.proximaFechaGeneracion = frecuencia.siguienteFecha(proximaFechaGeneracion, diaEntrega);
	}

	// ---------------------------------------------------------------------
	// CAMBIOS DE ESTADO: solo se permiten los que tienen sentido
	// ---------------------------------------------------------------------

	// ACTIVA -> PAUSADA
	public void pausar() {
		if (estado != EstadoSuscripcion.ACTIVA) {
			throw new ConflictoException("Solo se puede pausar una suscripción activa, y esta está " + estado + ".");
		}
		this.estado = EstadoSuscripcion.PAUSADA;
	}

	// PAUSADA -> ACTIVA. Si la próxima entrega quedó en el pasado se recalcula,
	// para no generar de golpe los pedidos de todo el tiempo que estuvo pausada.
	public void reactivar(LocalDate hoy) {
		if (estado != EstadoSuscripcion.PAUSADA) {
			throw new ConflictoException("Solo se puede reactivar una suscripción pausada, y esta está " + estado + ".");
		}
		this.estado = EstadoSuscripcion.ACTIVA;
		if (proximaFechaGeneracion.isBefore(hoy)) {
			this.proximaFechaGeneracion = frecuencia.primeraFecha(diaEntrega, hoy);
		}
	}

	// ACTIVA o PAUSADA -> CANCELADA (no tiene vuelta atrás)
	public void cancelar() {
		if (estado == EstadoSuscripcion.CANCELADA) {
			throw new ConflictoException("La suscripción ya está cancelada.");
		}
		this.estado = EstadoSuscripcion.CANCELADA;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdSuscripcion() {
		return idSuscripcion;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public FrecuenciaSuscripcion getFrecuencia() {
		return frecuencia;
	}

	public Integer getDiaEntrega() {
		return diaEntrega;
	}

	public LocalDate getFechaInicio() {
		return fechaInicio;
	}

	public LocalDate getFechaFin() {
		return fechaFin;
	}

	public LocalDate getProximaFechaGeneracion() {
		return proximaFechaGeneracion;
	}

	public EstadoSuscripcion getEstado() {
		return estado;
	}

	public List<DetalleSuscripcion> getDetalles() {
		return detalles;
	}
}
