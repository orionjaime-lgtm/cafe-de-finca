package com.cafedefinca.backend.entity;

import com.cafedefinca.backend.exception.StockInsuficienteException;
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
import java.time.LocalDate;

/**
 * Tabla "lote_cafe_verde": cada compra de grano verde (materia prima).
 */
@Entity
@Table(name = "lote_cafe_verde")
public class LoteCafeVerde {

	// ---------------------------------------------------------------------
	// COLUMNAS
	// ---------------------------------------------------------------------
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_lote_verde")
	private Integer idLoteVerde;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "id_origen", nullable = false)
	private OrigenCafe origen;

	// Por ahora solo se guarda el id: ningún endpoint necesita los datos del proveedor
	@Column(name = "id_proveedor", nullable = false)
	private Integer idProveedor;

	@Column(name = "codigo_lote", nullable = false)
	private String codigoLote;

	@Column(name = "fecha_ingreso", nullable = false)
	private LocalDate fechaIngreso;

	@Column(name = "cant_bolsas", nullable = false)
	private Integer cantBolsas;

	@Column(name = "peso_inicial_gramos", nullable = false)
	private BigDecimal pesoInicialGramos;

	@Column(name = "stock_actual_gramos", nullable = false)
	private BigDecimal stockActualGramos;

	// ---------------------------------------------------------------------
	// CONSTRUCTOR (JPA lo necesita vacío)
	// ---------------------------------------------------------------------
	protected LoteCafeVerde() {
	}

	// ---------------------------------------------------------------------
	// CONSTRUCTOR DE NEGOCIO: un lote recién ingresado
	// El stock actual arranca igual al peso inicial (todavía no se tostó nada).
	// ---------------------------------------------------------------------
	public LoteCafeVerde(OrigenCafe origen, Integer idProveedor, String codigoLote,
			LocalDate fechaIngreso, Integer cantBolsas, BigDecimal pesoInicialGramos) {
		this.origen = origen;
		this.idProveedor = idProveedor;
		this.codigoLote = codigoLote;
		this.fechaIngreso = fechaIngreso;
		this.cantBolsas = cantBolsas;
		this.pesoInicialGramos = pesoInicialGramos;
		this.stockActualGramos = pesoInicialGramos;
	}

	// ---------------------------------------------------------------------
	// MÉTODO DE NEGOCIO: DESCONTAR STOCK
	// Si no alcanza el café, lanza StockInsuficienteException (el manejador la convierte en 409).
	// ---------------------------------------------------------------------
	public void descontarStock(BigDecimal gramos) {
		if (gramos.compareTo(stockActualGramos) > 0) {
			throw new StockInsuficienteException(stockActualGramos, gramos);
		}
		this.stockActualGramos = stockActualGramos.subtract(gramos);
	}

	// ---------------------------------------------------------------------
	// GETTERS
	// ---------------------------------------------------------------------
	public Integer getIdLoteVerde() {
		return idLoteVerde;
	}

	public OrigenCafe getOrigen() {
		return origen;
	}

	public Integer getIdProveedor() {
		return idProveedor;
	}

	public String getCodigoLote() {
		return codigoLote;
	}

	public LocalDate getFechaIngreso() {
		return fechaIngreso;
	}

	public Integer getCantBolsas() {
		return cantBolsas;
	}

	public BigDecimal getPesoInicialGramos() {
		return pesoInicialGramos;
	}

	public BigDecimal getStockActualGramos() {
		return stockActualGramos;
	}
}
