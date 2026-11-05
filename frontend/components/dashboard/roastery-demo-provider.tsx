'use client'

import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { apiGet, apiPatch, apiPost } from '@/lib/api'
import { useAuth } from '@/components/auth-provider'

export type GreenCoffeeLot = { id: string; numericId: number; code: string; origin: string; supplier: string; purchaseDate: string; stockGrams: number; initialGrams: number }
export type RoastedCoffeeLot = { id: string; numericId: number; code: string; origin: string; profile: string; roastDate: string; availableDate: string; expirationDate: string; stockGrams: number; availableForSale: boolean; freshness: string }
export type RoastOrder = { id: string; numericId: number; lotId: string; origin: string; profile: string; greenInputGrams: number; roastedOutputGrams: number; lossPercent: number; createdAt: string; operator: string | null }
export type Origin = { idOrigen: number; pais: string; region: string; variedad: string; alturaMsnm: number; notasCata: string; stockMinimoAlertaGramos: number }
export type Supplier = { idProveedor: number; cuit: string; razonSocial: string; paisOrigen: string; telefonoEmail: string }
export type RoastProfile = { idPerfilTueste: number; nombrePerfil: string; tempObjetivoC: number; tiempoTuesteMin: number; mermaEsperadaPct: number; horasReposoMinimas: number }
export type Customer = { idCliente: number; tipoCliente: string; nombreRazonSocial: string; cuitODni: string; condicionIva: string; plazoCtaCteDias: number; limiteCredito: number }
export type Presentation = { idPresentacion: number; nombreEnvase: string; pesoGramos: number }
export type GrindingType = { idTipoMolienda: number; nombreMolienda: string; tipoCafetera: string; nivelGranulometria: string; esGranoEntero: boolean }
export type StockRow = { idOrigen: number; pais: string; region: string; variedad: string; stockActualGramos: number; stockMinimoGramos: number; estado: string }
export type AlertRow = { nivel: string; idOrigen: number; origen: string; mensaje: string; stockActualGramos: number; stockMinimoGramos: number; consumoDiarioGramos: number | null; diasEstimados: number | null }
export type ExpirySummary = { activos: number; porVencer: number; vencidos: number }

type PurchaseInput = { idOrigen: number; idProveedor: number; codigoLote: string; cantidad: number; unidad: string; cantBolsas: number; fechaIngreso?: string }
type RoastInput = { idLoteVerde: number; idPerfilTueste: number; pesoVerdeUsado: number; unidad: string; operadorTostador?: string }
type SaleChannel = { idCanalVenta: number; nombreCanal: string; requiereDatosFiscales: boolean; permiteCtaCte: boolean }
type PaymentMethod = { idMedioPago: number; nombreMedio: string; requiereComprobante: boolean }
type OrderItem = { loteTostadoId: number; presentacionId: number; tipoMoliendaId: number; cantidadEnvases: number; pesoTotalGramos: number; precioUnitario: number; subtotal: number }
type Order = { idPedido: number; clienteId: number; canalVentaId: number; fechaHoraPedido: string; estadoPreparacion: string; estadoCobro: string; montoTotal: number; porcentajeDescuento: number; montoDescuento: number; items: OrderItem[]; factura: null | { idFactura: number; tipoComprobante: string; numeroComprobante: string; fechaEmision: string; fechaVencimiento: string; montoTotal: number } }
type B2bAccount = { pedidoId: number; clienteId: number; cliente: string; deuda: number; factura: null | { idFactura: number; tipoComprobante: string; numeroComprobante: string; fechaEmision: string; fechaVencimiento: string; montoTotal: number }; vencimiento: string | null; estado: string }
type B2cSale = { pagoId: number; pedidoId: number; clienteId: number; cliente: string; fechaHoraPago: string; montoPagado: number; medioPagoId: number; medioPago: string }
type NewOrderInput = { clienteId: number; canalVentaId: number; items: Array<{ loteTostadoId: number; presentacionId: number; tipoMoliendaId: number; cantidadEnvases: number; precioUnitario: number }> }
type NewPaymentInput = { pedidoId: number; medioPagoId: number; monto: number }
type OrderQuote = { montoTotal: number; porcentajeDescuentoAplicado: number; montoDescuentoAplicado: number; ahorroPorPlan: Array<{ planId: number; nombrePlan: string; porcentajeDescuento: number; montoAhorro: number }> }
type InventoryContext = { greenLots: GreenCoffeeLot[]; roastedLots: RoastedCoffeeLot[]; roastOrders: RoastOrder[]; origins: Origin[]; suppliers: Supplier[]; roastProfiles: RoastProfile[]; customers: Customer[]; presentations: Presentation[]; grindingTypes: GrindingType[]; stockRows: StockRow[]; alerts: AlertRow[]; expirySummary: ExpirySummary; saleChannels: SaleChannel[]; paymentMethods: PaymentMethod[]; orders: Order[]; b2bAccounts: B2bAccount[]; b2cSales: B2cSale[]; isReady: boolean; storageError: string | null; refresh: () => Promise<void>; updateStockMinimum: (idOrigen: number, stockMinimoGramos: number) => Promise<StockRow>; recordPurchase: (purchase: PurchaseInput) => Promise<GreenCoffeeLot>; recordRoast: (order: RoastInput) => Promise<RoastOrder>; recordOrder: (order: NewOrderInput) => Promise<Order>; quoteOrder: (order: { clienteId: number; items: NewOrderInput['items'] }) => Promise<OrderQuote>; recordPayment: (payment: NewPaymentInput) => Promise<{ pagoId: number; pedidoId: number; medioPagoId: number; medioPago: string; fechaHoraPago: string; montoPagado: number; estadoCobro: string }>; changeOrderStatus: (pedidoId: number, estadoPreparacion: string) => Promise<Order>; recordCustomerOrder: (order: { clienteId: number; loteTostadoId: number; presentacionId: number; tipoMoliendaId: number; cantidadEnvases: number; precioUnitario: number; canalVentaId: number; medioPagoId?: number; montoPago?: number }) => Promise<{ pedido: Order; pago?: { pagoId: number; pedidoId: number; medioPagoId: number; medioPago: string; fechaHoraPago: string; montoPagado: number; estadoCobro: string } }> }

const InventoryContext = createContext<InventoryContext | null>(null)
const num = (value: unknown) => { const parsed = Number(value ?? 0); return Number.isFinite(parsed) ? parsed : 0 }

export function RoasteryDemoProvider({ children }: { children: React.ReactNode }) {
  const [greenLots, setGreenLots] = useState<GreenCoffeeLot[]>([])
  const [roastedLots, setRoastedLots] = useState<RoastedCoffeeLot[]>([])
  const [roastOrders, setRoastOrders] = useState<RoastOrder[]>([])
  const [origins, setOrigins] = useState<Origin[]>([])
  const [suppliers, setSuppliers] = useState<Supplier[]>([])
  const [roastProfiles, setRoastProfiles] = useState<RoastProfile[]>([])
  const [customers, setCustomers] = useState<Customer[]>([])
  const [presentations, setPresentations] = useState<Presentation[]>([])
  const [grindingTypes, setGrindingTypes] = useState<GrindingType[]>([])
  const [stockRows, setStockRows] = useState<StockRow[]>([])
  const [alerts, setAlerts] = useState<AlertRow[]>([])
  const [expirySummary, setExpirySummary] = useState<ExpirySummary>({ activos: 0, porVencer: 0, vencidos: 0 })
  const [saleChannels, setSaleChannels] = useState<SaleChannel[]>([])
  const [paymentMethods, setPaymentMethods] = useState<PaymentMethod[]>([])
  const [orders, setOrders] = useState<Order[]>([])
  const [b2bAccounts, setB2bAccounts] = useState<B2bAccount[]>([])
  const [b2cSales, setB2cSales] = useState<B2cSale[]>([])
  const [isReady, setIsReady] = useState(false)
  const [storageError, setStorageError] = useState<string | null>(null)
  const { user } = useAuth()

  const refresh = useCallback(async () => {
    setIsReady(false)
    try {
      const puede = (seccion: string) => user?.secciones.includes(seccion) ?? false
      const [green, roasted, roastOrderList, originList, supplierList, profileList, clientList, presentationList, grindingList, stock, alertList, expiry, channelList, methodList, orderList, b2bList, b2cList] = await Promise.all([
        puede('inventario') ? apiGet<any[]>('/api/lotes-verdes') : Promise.resolve([]),
        (puede('inventario') || puede('tueste') || puede('pedidos') || puede('cobros') || puede('compras')) ? apiGet<any[]>('/api/lotes-tostados') : Promise.resolve([]),
        puede('tueste') ? apiGet<any[]>('/api/ordenes-tueste') : Promise.resolve([]),
        (puede('inventario') || puede('tueste')) ? apiGet<any[]>('/api/origenes') : Promise.resolve([]),
        (puede('inventario') || puede('tueste')) ? apiGet<any[]>('/api/proveedores') : Promise.resolve([]),
        (puede('inventario') || puede('tueste')) ? apiGet<any[]>('/api/perfiles-tueste') : Promise.resolve([]),
        (puede('clientes') || puede('pedidos') || puede('compras')) ? apiGet<any[]>('/api/clientes') : Promise.resolve([]),
        (puede('inventario') || puede('pedidos') || puede('compras') || puede('cobros')) ? apiGet<any[]>('/api/presentaciones') : Promise.resolve([]),
        (puede('inventario') || puede('pedidos') || puede('compras') || puede('cobros')) ? apiGet<any[]>('/api/tipos-molienda') : Promise.resolve([]),
        puede('inventario') || puede('tueste') || puede('alertas') ? apiGet<any[]>('/api/origenes/stock') : Promise.resolve([]),
        puede('alertas') || puede('tueste') ? apiGet<any[]>('/api/alertas') : Promise.resolve([]),
        puede('caducidad') ? apiGet<any>('/api/caducidad/resumen') : Promise.resolve({ activos: 0, porVencer: 0, vencidos: 0 }),
        (puede('pedidos') || puede('cobros') || puede('compras')) ? apiGet<SaleChannel[]>('/api/canales-venta') : Promise.resolve([]),
        (puede('pedidos') || puede('cobros') || puede('compras')) ? apiGet<PaymentMethod[]>('/api/medios-pago') : Promise.resolve([]),
        (puede('pedidos') || puede('cobros')) ? apiGet<Order[]>('/api/pedidos') : Promise.resolve([]),
        puede('cobros') ? apiGet<B2bAccount[]>('/api/cobros/b2b') : Promise.resolve([]),
        puede('cobros') ? apiGet<B2cSale[]>('/api/cobros/b2c') : Promise.resolve([]),
      ])
      setSaleChannels(channelList)
      setPaymentMethods(methodList)
      setOrders(orderList.map((row) => ({ ...row, montoTotal: num(row.montoTotal), porcentajeDescuento: num(row.porcentajeDescuento), montoDescuento: num(row.montoDescuento), items: (row.items ?? []).map((item) => ({ ...item, pesoTotalGramos: num(item.pesoTotalGramos), precioUnitario: num(item.precioUnitario), subtotal: num(item.subtotal) })) })))
      setB2bAccounts(b2bList.map((row) => ({ ...row, deuda: num(row.deuda) })))
      setB2cSales(b2cList.map((row) => ({ ...row, montoPagado: num(row.montoPagado) })))
      setGreenLots(green.map((row) => ({ id: `VG-${row.idLoteVerde}`, numericId: row.idLoteVerde, code: row.codigoLote, origin: row.origen, supplier: row.proveedor, purchaseDate: row.fechaIngreso, stockGrams: num(row.stockActualGramos), initialGrams: num(row.pesoInicialGramos) })))
      setRoastedLots(roasted.map((row) => ({ id: row.codigoLote ?? `LT-${row.idLoteTostado}`, numericId: row.idLoteTostado, code: row.codigoLote, origin: `${row.pais} ${row.region} · ${row.variedad}`, profile: row.perfilTueste, roastDate: row.fechaTueste?.slice(0, 10) ?? '', availableDate: row.fechaDisponibleVenta?.slice(0, 10) ?? '', expirationDate: row.fechaCaducidad ?? '', stockGrams: num(row.stockDisponibleGramos), availableForSale: row.disponibleParaVenta, freshness: row.estadoFrescura })))
      setRoastOrders(roastOrderList.map((row) => ({ id: row.codigoLoteTostado ?? `OT-${row.idOrdenTueste}`, numericId: row.idOrdenTueste, lotId: row.codigoLoteTostado ?? '', origin: row.origen, profile: row.perfilTueste, greenInputGrams: num(row.pesoVerdeUsadoGramos), roastedOutputGrams: num(row.pesoTostadoObtenidoGramos), lossPercent: num(row.mermaRealPct), createdAt: row.fechaHoraTueste, operator: row.operadorTostador })))
      setOrigins(originList.map((row) => ({ ...row, stockMinimoAlertaGramos: num(row.stockMinimoAlertaGramos) })))
      setSuppliers(supplierList)
      setRoastProfiles(profileList.map((row) => ({ ...row, tempObjetivoC: num(row.tempObjetivoC), tiempoTuesteMin: Number(row.tiempoTuesteMin ?? 0), mermaEsperadaPct: num(row.mermaEsperadaPct), horasReposoMinimas: Number(row.horasReposoMinimas ?? 0) })))
      setCustomers(clientList.map((row) => ({ ...row, limiteCredito: num(row.limiteCredito), plazoCtaCteDias: Number(row.plazoCtaCteDias ?? 0) })))
      setPresentations(presentationList.map((row) => ({ ...row, pesoGramos: num(row.pesoGramos) })))
      setGrindingTypes(grindingList)
      setStockRows(stock.map((row) => ({ ...row, stockActualGramos: num(row.stockActualGramos), stockMinimoGramos: num(row.stockMinimoGramos) })))
      setAlerts(alertList.map((row) => ({ ...row, stockActualGramos: num(row.stockActualGramos), stockMinimoGramos: num(row.stockMinimoGramos), consumoDiarioGramos: row.consumoDiarioGramos == null ? null : num(row.consumoDiarioGramos), diasEstimados: row.diasEstimados == null ? null : Number(row.diasEstimados) })))
      setExpirySummary(expiry)
      setStorageError(null)
    } catch (error) { setStorageError(error instanceof Error ? error.message : 'No se pudo conectar con el backend.') }
    finally { setIsReady(true) }
  }, [user?.role, user?.secciones])

  useEffect(() => { void refresh() }, [refresh])

  async function updateStockMinimum(idOrigen: number, stockMinimoGramos: number) {
    const row = await apiPatch<any>(`/api/origenes/${idOrigen}/stock-minimo`, { stockMinimoGramos })
    await refresh()
    return { ...row, stockActualGramos: num(row.stockActualGramos), stockMinimoGramos: num(row.stockMinimoGramos) } as StockRow
  }

  async function recordPurchase(purchase: PurchaseInput) {
    const row = await apiPost<any>('/api/lotes-verdes', purchase)
    await refresh()
    return { id: `VG-${row.idLoteVerde}`, numericId: row.idLoteVerde, code: row.codigoLote, origin: row.origen, supplier: row.proveedor, purchaseDate: row.fechaIngreso, stockGrams: num(row.stockActualGramos), initialGrams: num(row.pesoInicialGramos) }
  }
  async function recordRoast(order: RoastInput) {
    const row = await apiPost<any>('/api/ordenes-tueste', order)
    await refresh()
    return { id: row.codigoLoteTostado ?? `OT-${row.idOrdenTueste}`, numericId: row.idOrdenTueste, lotId: row.codigoLoteTostado ?? '', origin: row.origen, profile: row.perfilTueste, greenInputGrams: num(row.pesoVerdeUsadoGramos), roastedOutputGrams: num(row.pesoTostadoObtenidoGramos), lossPercent: num(row.mermaRealPct), createdAt: row.fechaHoraTueste, operator: row.operadorTostador }
  }
  async function recordOrder(order: NewOrderInput) {
    const row = await apiPost<Order>('/api/pedidos', order)
    await refresh()
    return row
  }
  async function quoteOrder(order: { clienteId: number; items: NewOrderInput['items'] }) {
    const row = await apiPost<any>('/api/pedidos/cotizar', order)
    return {
      montoTotal: num(row.montoTotal),
      porcentajeDescuentoAplicado: num(row.porcentajeDescuentoAplicado),
      montoDescuentoAplicado: num(row.montoDescuentoAplicado),
      ahorroPorPlan: (row.ahorroPorPlan ?? []).map((plan: any) => ({ planId: plan.planId, nombrePlan: plan.nombrePlan, porcentajeDescuento: num(plan.porcentajeDescuento), montoAhorro: num(plan.montoAhorro) })),
    }
  }
  async function recordPayment(payment: NewPaymentInput) {
    const row = await apiPost<any>('/api/pagos', payment)
    await refresh()
    return { ...row, montoPagado: num(row.montoPagado) }
  }
  async function changeOrderStatus(pedidoId: number, estadoPreparacion: string) {
    const row = await apiPatch<Order>(`/api/pedidos/${pedidoId}`, { estadoPreparacion })
    await refresh()
    return row
  }
  async function recordCustomerOrder(order: { clienteId: number; loteTostadoId: number; presentacionId: number; tipoMoliendaId: number; cantidadEnvases: number; precioUnitario: number; canalVentaId: number; medioPagoId?: number; montoPago?: number }) {
    const pedido = await recordOrder({
      clienteId: order.clienteId,
      canalVentaId: order.canalVentaId,
      items: [{ loteTostadoId: order.loteTostadoId, presentacionId: order.presentacionId, tipoMoliendaId: order.tipoMoliendaId, cantidadEnvases: order.cantidadEnvases, precioUnitario: order.precioUnitario }],
    })
    let pago
    if (order.medioPagoId && order.montoPago && pedido.estadoCobro !== 'PAGADO') {
      pago = await recordPayment({ pedidoId: pedido.idPedido, medioPagoId: order.medioPagoId, monto: order.montoPago })
    }
    await refresh()
    return { pedido, pago }
  }
  return <InventoryContext.Provider value={{ greenLots, roastedLots, roastOrders, origins, suppliers, roastProfiles, customers, presentations, grindingTypes, stockRows, alerts, expirySummary, saleChannels, paymentMethods, orders, b2bAccounts, b2cSales, isReady, storageError, refresh, updateStockMinimum, recordPurchase, recordRoast, recordOrder, quoteOrder, recordPayment, changeOrderStatus, recordCustomerOrder }}>{children}</InventoryContext.Provider>
}

export function useRoasteryDemo() { const context = useContext(InventoryContext); if (!context) throw new Error('useRoasteryDemo debe usarse dentro de RoasteryDemoProvider.'); return context }
