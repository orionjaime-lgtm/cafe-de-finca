'use client'

import { createContext, useContext, useEffect, useState } from 'react'

export type GreenCoffeeLot = {
  id: string
  origin: string
  region: string
  variety: string
  supplier: string
  purchaseDate: string
  stockGrams: number
  minimumGrams: number
}

export type RoastedCoffeeLot = {
  id: string
  origin: string
  profile: string
  roastDate: string
  availableDate: string
  expirationDate: string
  stockGrams: number
}

export type RoastOrder = {
  id: string
  origin: string
  profile: string
  greenInputGrams: number
  roastedOutputGrams: number
  lossPercent: number
  restingDays: number
  createdAt: string
}

type RoastInput = {
  greenLotId: string
  profile: string
  inputGrams: number
  lossPercent: number
  restingDays: number
}

type PurchaseInput = {
  origin: string
  region: string
  variety: string
  supplier: string
  purchaseDate: string
  amountGrams: number
}

type CustomerOrderInput = {
  roastedLotId: string
  quantityGrams: number
}

type DemoInventory = {
  greenLots: GreenCoffeeLot[]
  roastedLots: RoastedCoffeeLot[]
  roastOrders: RoastOrder[]
  isReady: boolean
  storageError: string | null
  recordPurchase: (purchase: PurchaseInput) => boolean
  recordRoast: (order: RoastInput) => boolean
  recordCustomerOrder: (order: CustomerOrderInput) => boolean
  resetDemo: () => void
}

const STORAGE_KEY = 'cafe-de-finca:roastery-demo:v1'

const initialGreenLots: GreenCoffeeLot[] = [
  {
    id: 'VG-2041',
    origin: 'Colombia',
    region: 'Huila',
    variety: 'Caturra lavado',
    supplier: 'Finca El Paraíso',
    purchaseDate: '2026-10-08',
    stockGrams: 33_000,
    minimumGrams: 12_000,
  },
  {
    id: 'VG-2038',
    origin: 'Brasil',
    region: 'Cerrado Mineiro',
    variety: 'Bourbon natural',
    supplier: 'Cooperativa Sul de Minas',
    purchaseDate: '2026-10-03',
    stockGrams: 21_500,
    minimumGrams: 10_000,
  },
  {
    id: 'VG-2032',
    origin: 'Etiopía',
    region: 'Yirgacheffe',
    variety: 'Heirloom lavado',
    supplier: 'Origen Selecto',
    purchaseDate: '2026-09-27',
    stockGrams: 3_400,
    minimumGrams: 10_000,
  },
  {
    id: 'VG-2028',
    origin: 'Perú',
    region: 'Cajamarca',
    variety: 'Typica lavado',
    supplier: 'Andes Coffee Co.',
    purchaseDate: '2026-09-21',
    stockGrams: 0,
    minimumGrams: 8_000,
  },
]

const initialRoastedLots: RoastedCoffeeLot[] = [
  {
    id: 'LT-1082',
    origin: 'Colombia Huila · Caturra',
    profile: 'Tueste medio',
    roastDate: '2026-10-20',
    availableDate: '2026-10-22',
    expirationDate: '2026-11-19',
    stockGrams: 12_600,
  },
  {
    id: 'LT-1079',
    origin: 'Brasil Cerrado · Bourbon',
    profile: 'Tueste oscuro',
    roastDate: '2026-10-16',
    availableDate: '2026-10-18',
    expirationDate: '2026-11-15',
    stockGrams: 8_250,
  },
  {
    id: 'LT-1074',
    origin: 'Etiopía Yirgacheffe · Heirloom',
    profile: 'Tueste claro',
    roastDate: '2026-10-02',
    availableDate: '2026-10-04',
    expirationDate: '2026-11-01',
    stockGrams: 4_100,
  },
  {
    id: 'LT-1068',
    origin: 'Colombia Huila · Caturra',
    profile: 'Tueste medio',
    roastDate: '2026-09-18',
    availableDate: '2026-09-20',
    expirationDate: '2026-10-18',
    stockGrams: 1_650,
  },
]

const initialRoastOrders: RoastOrder[] = [
  {
    id: 'OT-058',
    origin: 'Colombia Huila · Caturra',
    profile: 'Tueste medio',
    greenInputGrams: 15_000,
    roastedOutputGrams: 12_600,
    lossPercent: 16,
    restingDays: 2,
    createdAt: '2026-10-20T09:30:00',
  },
  {
    id: 'OT-057',
    origin: 'Brasil Cerrado · Bourbon',
    profile: 'Tueste oscuro',
    greenInputGrams: 10_000,
    roastedOutputGrams: 8_250,
    lossPercent: 17.5,
    restingDays: 2,
    createdAt: '2026-10-16T14:15:00',
  },
  {
    id: 'OT-056',
    origin: 'Etiopía Yirgacheffe · Heirloom',
    profile: 'Tueste claro',
    greenInputGrams: 5_000,
    roastedOutputGrams: 4_100,
    lossPercent: 18,
    restingDays: 2,
    createdAt: '2026-10-02T11:00:00',
  },
]

const DemoInventoryContext = createContext<DemoInventory | null>(null)

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isGreenLot(value: unknown): value is GreenCoffeeLot {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    typeof value.origin === 'string' &&
    typeof value.region === 'string' &&
    typeof value.variety === 'string' &&
    typeof value.supplier === 'string' &&
    typeof value.purchaseDate === 'string' &&
    typeof value.stockGrams === 'number' &&
    Number.isFinite(value.stockGrams) &&
    value.stockGrams >= 0 &&
    typeof value.minimumGrams === 'number' &&
    Number.isFinite(value.minimumGrams) &&
    value.minimumGrams >= 0
  )
}

function isRoastedLot(value: unknown): value is RoastedCoffeeLot {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    typeof value.origin === 'string' &&
    typeof value.profile === 'string' &&
    typeof value.roastDate === 'string' &&
    typeof value.availableDate === 'string' &&
    typeof value.expirationDate === 'string' &&
    typeof value.stockGrams === 'number' &&
    Number.isFinite(value.stockGrams) &&
    value.stockGrams >= 0
  )
}

function isRoastOrder(value: unknown): value is RoastOrder {
  return (
    isRecord(value) &&
    typeof value.id === 'string' &&
    typeof value.origin === 'string' &&
    typeof value.profile === 'string' &&
    typeof value.greenInputGrams === 'number' &&
    Number.isFinite(value.greenInputGrams) &&
    typeof value.roastedOutputGrams === 'number' &&
    Number.isFinite(value.roastedOutputGrams) &&
    typeof value.lossPercent === 'number' &&
    Number.isFinite(value.lossPercent) &&
    typeof value.restingDays === 'number' &&
    Number.isFinite(value.restingDays) &&
    typeof value.createdAt === 'string'
  )
}

function isStoredInventory(
  value: unknown,
): value is { greenLots: GreenCoffeeLot[]; roastedLots: RoastedCoffeeLot[]; roastOrders: RoastOrder[] } {
  return (
    isRecord(value) &&
    Array.isArray(value.greenLots) &&
    value.greenLots.every(isGreenLot) &&
    Array.isArray(value.roastedLots) &&
    value.roastedLots.every(isRoastedLot) &&
    Array.isArray(value.roastOrders) &&
    value.roastOrders.every(isRoastOrder)
  )
}

function dateAfterDays(date: Date, days: number) {
  const result = new Date(date)
  result.setDate(result.getDate() + days)
  return result.toISOString().slice(0, 10)
}

export function RoasteryDemoProvider({ children }: { children: React.ReactNode }) {
  const [greenLots, setGreenLots] = useState(initialGreenLots)
  const [roastedLots, setRoastedLots] = useState(initialRoastedLots)
  const [roastOrders, setRoastOrders] = useState(initialRoastOrders)
  const [isReady, setIsReady] = useState(false)
  const [storageError, setStorageError] = useState<string | null>(null)

  useEffect(() => {
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY)
      if (stored) {
        const parsed: unknown = JSON.parse(stored)
        if (isStoredInventory(parsed)) {
          setGreenLots(parsed.greenLots)
          setRoastedLots(parsed.roastedLots)
          setRoastOrders(parsed.roastOrders)
        } else {
          setStorageError('Los datos de la demo no son válidos. Reiniciá la información para continuar.')
        }
      }
    } catch {
      setStorageError('No se pudo leer la demo guardada en este navegador.')
    } finally {
      setIsReady(true)
    }
  }, [])

  useEffect(() => {
    if (!isReady) return

    try {
      window.localStorage.setItem(
        STORAGE_KEY,
        JSON.stringify({ greenLots, roastedLots, roastOrders }),
      )
    } catch {
      setStorageError('No se pudo guardar la demo. Los cambios podrían perderse al cerrar el navegador.')
    }
  }, [greenLots, isReady, roastOrders, roastedLots])

  function recordPurchase(purchase: PurchaseInput) {
    if (
      !isReady ||
      !purchase.origin.trim() ||
      !purchase.region.trim() ||
      !purchase.variety.trim() ||
      !purchase.supplier.trim() ||
      !purchase.purchaseDate ||
      !Number.isFinite(purchase.amountGrams) ||
      purchase.amountGrams <= 0
    ) {
      return false
    }

    const id = `VG-${Date.now().toString().slice(-6)}`
    setGreenLots((current) => [
      {
        id,
        ...purchase,
        origin: purchase.origin.trim(),
        region: purchase.region.trim(),
        variety: purchase.variety.trim(),
        supplier: purchase.supplier.trim(),
        stockGrams: purchase.amountGrams,
        minimumGrams: Math.min(purchase.amountGrams, 10_000),
      },
      ...current,
    ])
    return true
  }

  function recordRoast(order: RoastInput) {
    const greenLot = greenLots.find((lot) => lot.id === order.greenLotId)
    if (
      !isReady ||
      !greenLot ||
      !order.profile.trim() ||
      !Number.isFinite(order.inputGrams) ||
      order.inputGrams <= 0 ||
      order.inputGrams > greenLot.stockGrams ||
      !Number.isFinite(order.lossPercent) ||
      order.lossPercent < 0 ||
      order.lossPercent >= 100 ||
      !Number.isInteger(order.restingDays) ||
      order.restingDays < 0
    ) {
      return false
    }

    const now = new Date()
    const roastDate = now.toISOString().slice(0, 10)
    const originLabel = `${greenLot.origin} ${greenLot.region} · ${greenLot.variety}`
    const roastedOutputGrams = Math.round(order.inputGrams * (1 - order.lossPercent / 100))
    const id = `OT-${Date.now().toString().slice(-6)}`

    setGreenLots((current) =>
      current.map((lot) =>
        lot.id === greenLot.id ? { ...lot, stockGrams: lot.stockGrams - order.inputGrams } : lot,
      ),
    )
    setRoastedLots((current) => [
      {
        id: `LT-${Date.now().toString().slice(-6)}`,
        origin: originLabel,
        profile: order.profile,
        roastDate,
        availableDate: dateAfterDays(now, order.restingDays),
        expirationDate: dateAfterDays(now, 30),
        stockGrams: roastedOutputGrams,
      },
      ...current,
    ])
    setRoastOrders((current) => [
      {
        id,
        origin: originLabel,
        profile: order.profile,
        greenInputGrams: order.inputGrams,
        roastedOutputGrams,
        lossPercent: order.lossPercent,
        restingDays: order.restingDays,
        createdAt: now.toISOString(),
      },
      ...current,
    ])
    return true
  }

  function recordCustomerOrder(order: CustomerOrderInput) {
    const lot = roastedLots.find((current) => current.id === order.roastedLotId)
    if (
      !isReady ||
      !lot ||
      !Number.isFinite(order.quantityGrams) ||
      order.quantityGrams <= 0 ||
      order.quantityGrams > lot.stockGrams
    ) {
      return false
    }

    setRoastedLots((current) =>
      current.map((currentLot) =>
        currentLot.id === lot.id
          ? { ...currentLot, stockGrams: currentLot.stockGrams - order.quantityGrams }
          : currentLot,
      ),
    )
    return true
  }

  function resetDemo() {
    setGreenLots(initialGreenLots)
    setRoastedLots(initialRoastedLots)
    setRoastOrders(initialRoastOrders)
    setStorageError(null)
  }

  return (
    <DemoInventoryContext.Provider
      value={{
        greenLots,
        roastedLots,
        roastOrders,
        isReady,
        storageError,
        recordPurchase,
        recordRoast,
        recordCustomerOrder,
        resetDemo,
      }}
    >
      {children}
    </DemoInventoryContext.Provider>
  )
}

export function useRoasteryDemo() {
  const context = useContext(DemoInventoryContext)
  if (!context) {
    throw new Error('useRoasteryDemo debe usarse dentro de RoasteryDemoProvider.')
  }
  return context
}
