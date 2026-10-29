package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla "pedido": venta realizada a un cliente por un canal.
 */
@Entity
@Table(name = "pedido")
public class Pedido {

	// ---------------------------------------------------------------------
	// CONSTANTES
	// ---------------------------------------------------------------------
	private static final String ESTADO_PREPARACION_PENDIENTE = "PENDIENTE";
	private static final String ESTADO_COBRO_PENDIENTE = "PENDIENTE";

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_pedido")
	private Integer idPedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_cliente", nullable = false)
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_canal_venta", nullable = false)
	private CanalVenta canalVenta;

	@Column(name = "id_suscripcion")
	private Integer idSuscripcion;

	@Column(name = "id_hoja_ruta")
	private Integer idHojaRuta;

	@Column(name = "orden_visita")
	private Integer ordenVisita;

	@Column(name = "fecha_hora_pedido", nullable = false)
	private LocalDateTime fechaHoraPedido;

	@Column(name = "fecha_entrega_prevista")
	private LocalDate fechaEntregaPrevista;

	@Column(name = "estado_preparacion")
	private String estadoPreparacion;

	@Column(name = "estado_cobro")
	private String estadoCobro;

	@Column(name = "monto_total", nullable = false)
	private BigDecimal montoTotal;

	@OneToMany(mappedBy = "pedido", fetch = FetchType.LAZY)
	private List<DetallePedido> detalles = new ArrayList<>();

	@OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY)
	private Factura factura;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected Pedido() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO
	// ---------------------------------------------------------------------
	public Pedido(Cliente cliente, CanalVenta canalVenta, LocalDateTime fechaHoraPedido, BigDecimal montoTotal) {
		this.cliente = cliente;
		this.canalVenta = canalVenta;
		this.fechaHoraPedido = fechaHoraPedido;
		this.estadoPreparacion = ESTADO_PREPARACION_PENDIENTE;
		this.estadoCobro = ESTADO_COBRO_PENDIENTE;
		this.montoTotal = montoTotal;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdPedido() {
		return idPedido;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public CanalVenta getCanalVenta() {
		return canalVenta;
	}

	public Integer getIdSuscripcion() {
		return idSuscripcion;
	}

	public Integer getIdHojaRuta() {
		return idHojaRuta;
	}

	public Integer getOrdenVisita() {
		return ordenVisita;
	}

	public LocalDateTime getFechaHoraPedido() {
		return fechaHoraPedido;
	}

	public LocalDate getFechaEntregaPrevista() {
		return fechaEntregaPrevista;
	}

	public String getEstadoPreparacion() {
		return estadoPreparacion;
	}

	public String getEstadoCobro() {
		return estadoCobro;
	}

	public BigDecimal getMontoTotal() {
		return montoTotal;
	}

	public List<DetallePedido> getDetalles() {
		return detalles;
	}

	public Factura getFactura() {
		return factura;
	}
}
