package com.cafedefinca.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabla "detalle_pedido": presentación, molienda, lote y precio de un renglón vendido.
 */
@Entity
@Table(name = "detalle_pedido")
public class DetallePedido {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_detalle_pedido")
	private Integer idDetallePedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_pedido", nullable = false)
	private Pedido pedido;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_lote_tostado", nullable = false)
	private LoteCafeTostado loteTostado;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_presentacion", nullable = false)
	private Presentacion presentacion;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_tipo_molienda", nullable = false)
	private TipoMolienda tipoMolienda;

	@Column(name = "cantidad_envases", nullable = false)
	private Integer cantidadEnvases;

	@Column(name = "peso_total_gramos", nullable = false)
	private BigDecimal pesoTotalGramos;

	@Column(name = "precio_unitario", nullable = false)
	private BigDecimal precioUnitario;

	@Column(name = "subtotal", nullable = false)
	private BigDecimal subtotal;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected DetallePedido() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO
	// ---------------------------------------------------------------------
	public DetallePedido(Pedido pedido, LoteCafeTostado loteTostado, Presentacion presentacion,
			TipoMolienda tipoMolienda, Integer cantidadEnvases, BigDecimal pesoTotalGramos,
			BigDecimal precioUnitario, BigDecimal subtotal) {
		this.pedido = pedido;
		this.loteTostado = loteTostado;
		this.presentacion = presentacion;
		this.tipoMolienda = tipoMolienda;
		this.cantidadEnvases = cantidadEnvases;
		this.pesoTotalGramos = pesoTotalGramos;
		this.precioUnitario = precioUnitario;
		this.subtotal = subtotal;
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdDetallePedido() {
		return idDetallePedido;
	}

	public Pedido getPedido() {
		return pedido;
	}

	public LoteCafeTostado getLoteTostado() {
		return loteTostado;
	}

	public Presentacion getPresentacion() {
		return presentacion;
	}

	public TipoMolienda getTipoMolienda() {
		return tipoMolienda;
	}

	public Integer getCantidadEnvases() {
		return cantidadEnvases;
	}

	public BigDecimal getPesoTotalGramos() {
		return pesoTotalGramos;
	}

	public BigDecimal getPrecioUnitario() {
		return precioUnitario;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}
}
