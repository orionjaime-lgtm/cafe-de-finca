'use client'

import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { apiGet, apiPost } from '@/lib/api'

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
type RoastInput = { idLoteVerde: number; idPerfilTueste: number; pesoVerdeUsado: number; pesoTostadoObtenido: number; unidad: string; operadorTostador?: string }
type InventoryContext = { greenLots: GreenCoffeeLot[]; roastedLots: RoastedCoffeeLot[]; roastOrders: RoastOrder[]; origins: Origin[]; suppliers: Supplier[]; roastProfiles: RoastProfile[]; customers: Customer[]; presentations: Presentation[]; grindingTypes: GrindingType[]; stockRows: StockRow[]; alerts: AlertRow[]; expirySummary: ExpirySummary; isReady: boolean; storageError: string | null; refresh: () => Promise<void>; recordPurchase: (purchase: PurchaseInput) => Promise<GreenCoffeeLot>; recordRoast: (order: RoastInput) => Promise<RoastOrder>; recordCustomerOrder: (order: { roastedLotId: string; quantityGrams: number }) => boolean }

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
  const [isReady, setIsReady] = useState(false)
  const [storageError, setStorageError] = useState<string | null>(null)

  const refresh = useCallback(async () => {
    setIsReady(false)
    try {
      const [green, roasted, orders, originList, supplierList, profileList, clientList, presentationList, grindingList, stock, alertList, expiry] = await Promise.all([
        apiGet<any[]>('/api/lotes-verdes'), apiGet<any[]>('/api/lotes-tostados'), apiGet<any[]>('/api/ordenes-tueste'), apiGet<any[]>('/api/origenes'), apiGet<any[]>('/api/proveedores'), apiGet<any[]>('/api/perfiles-tueste'), apiGet<any[]>('/api/clientes'), apiGet<any[]>('/api/presentaciones'), apiGet<any[]>('/api/tipos-molienda'), apiGet<any[]>('/api/origenes/stock'), apiGet<any[]>('/api/alertas'), apiGet<any>('/api/caducidad/resumen'),
      ])
      setGreenLots(green.map((row) => ({ id: `VG-${row.idLoteVerde}`, numericId: row.idLoteVerde, code: row.codigoLote, origin: row.origen, supplier: row.proveedor, purchaseDate: row.fechaIngreso, stockGrams: num(row.stockActualGramos), initialGrams: num(row.pesoInicialGramos) })))
      setRoastedLots(roasted.map((row) => ({ id: row.codigoLote ?? `LT-${row.idLoteTostado}`, numericId: row.idLoteTostado, code: row.codigoLote, origin: `${row.pais} ${row.region} · ${row.variedad}`, profile: row.perfilTueste, roastDate: row.fechaTueste?.slice(0, 10) ?? '', availableDate: row.fechaDisponibleVenta?.slice(0, 10) ?? '', expirationDate: row.fechaCaducidad ?? '', stockGrams: num(row.stockDisponibleGramos), availableForSale: row.disponibleParaVenta, freshness: row.estadoFrescura })))
      setRoastOrders(orders.map((row) => ({ id: row.codigoLoteTostado ?? `OT-${row.idOrdenTueste}`, numericId: row.idOrdenTueste, lotId: row.codigoLoteTostado ?? '', origin: row.origen, profile: row.perfilTueste, greenInputGrams: num(row.pesoVerdeUsadoGramos), roastedOutputGrams: num(row.pesoTostadoObtenidoGramos), lossPercent: num(row.mermaRealPct), createdAt: row.fechaHoraTueste, operator: row.operadorTostador })))
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
  }, [])

  useEffect(() => { void refresh() }, [refresh])

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
  function recordCustomerOrder() { return false }
  return <InventoryContext.Provider value={{ greenLots, roastedLots, roastOrders, origins, suppliers, roastProfiles, customers, presentations, grindingTypes, stockRows, alerts, expirySummary, isReady, storageError, refresh, recordPurchase, recordRoast, recordCustomerOrder }}>{children}</InventoryContext.Provider>
}

export function useRoasteryDemo() { const context = useContext(InventoryContext); if (!context) throw new Error('useRoasteryDemo debe usarse dentro de RoasteryDemoProvider.'); return context }
