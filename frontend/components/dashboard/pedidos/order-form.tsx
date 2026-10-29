'use client'

import { useMemo, useState } from 'react'
import { Info } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

const grams = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const kilograms = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 2 })
const inputClass = 'h-11 w-full rounded-lg border border-input bg-card px-3.5 text-sm outline-none focus-visible:border-primary focus-visible:ring-3 focus-visible:ring-primary/15'

export function OrderForm() {
  const { roastedLots, customers, presentations, grindingTypes, isReady } = useRoasteryDemo()
  const [customerId, setCustomerId] = useState('')
  const [lotId, setLotId] = useState('')
  const [presentationId, setPresentationId] = useState('')
  const [grindingId, setGrindingId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const availableLots = useMemo(() => roastedLots.filter((lot) => lot.availableForSale && lot.stockGrams > 0), [roastedLots])
  const selectedLot = availableLots.find((lot) => lot.numericId === Number(lotId)) ?? availableLots[0]
  const selectedPresentation = presentations.find((item) => item.idPresentacion === Number(presentationId)) ?? presentations[0]
  const selectedGrinding = grindingTypes.find((item) => item.idTipoMolienda === Number(grindingId)) ?? grindingTypes[0]
  const calculatedGrams = Number(quantity) > 0 && selectedPresentation ? Number(quantity) * selectedPresentation.pesoGramos : 0

  return <DashboardCard title="Detalle del pedido" description="Seleccioná cliente, lote, presentación y molienda con catálogos reales. Los pedidos todavía no se crean ni descuentan stock desde el backend." action={<span className="rounded-full bg-secondary px-2.5 py-1 text-[11px] font-semibold text-primary">CATÁLOGOS REALES</span>}>
    <form className="flex flex-col gap-6" onSubmit={(event) => event.preventDefault()}>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-2"><label className="text-sm font-medium" htmlFor="order-client">Cliente</label><select id="order-client" className={inputClass} value={customerId} onChange={(event) => setCustomerId(event.target.value)} disabled={!isReady}><option value="">Seleccionar cliente</option>{customers.map((customer) => <option key={customer.idCliente} value={customer.idCliente}>{customer.nombreRazonSocial} · {customer.tipoCliente}</option>)}</select></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium" htmlFor="order-lot">Lote tostado disponible</label><select id="order-lot" className={inputClass} value={selectedLot?.numericId ?? ''} onChange={(event) => setLotId(event.target.value)} disabled={!isReady || !availableLots.length}><option value="">Seleccionar lote</option>{availableLots.map((lot) => <option key={lot.id} value={lot.numericId}>{lot.code} · {lot.origin} · {kilograms.format(lot.stockGrams / 1000)} kg</option>)}</select></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium" htmlFor="order-presentation">Presentación</label><select id="order-presentation" className={inputClass} value={selectedPresentation?.idPresentacion ?? ''} onChange={(event) => setPresentationId(event.target.value)} disabled={!isReady}><option value="">Seleccionar presentación</option>{presentations.map((item) => <option key={item.idPresentacion} value={item.idPresentacion}>{item.nombreEnvase} · {grams.format(item.pesoGramos)} g</option>)}</select></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium" htmlFor="order-grinding">Tipo de molienda</label><select id="order-grinding" className={inputClass} value={selectedGrinding?.idTipoMolienda ?? ''} onChange={(event) => setGrindingId(event.target.value)} disabled={!isReady}><option value="">Seleccionar molienda</option>{grindingTypes.map((item) => <option key={item.idTipoMolienda} value={item.idTipoMolienda}>{item.nombreMolienda}{item.tipoCafetera ? ` · ${item.tipoCafetera}` : ''}</option>)}</select></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium" htmlFor="order-quantity">Cantidad de envases</label><input id="order-quantity" className={inputClass} type="number" min="1" step="1" value={quantity} onChange={(event) => setQuantity(event.target.value)} /></div>
      </div>
      <div role="status" className="flex items-start gap-3 rounded-xl border border-[#e8d3bf] bg-[#fbf3ea] px-4 py-3.5 text-[#6b4c3a]"><Info className="mt-0.5 size-4.5 shrink-0" /><p className="text-sm leading-relaxed">Peso calculado: <strong className="tabular-nums">{grams.format(calculatedGrams)} g de café tostado</strong>.</p></div>
      {availableLots.length === 0 && <p role="alert" className="text-sm text-destructive">No hay lotes tostados disponibles para venta.</p>}
      <button type="button" disabled className="h-11 rounded-lg bg-[#6b4c3a] text-sm font-semibold text-white opacity-50">Confirmar pedido · pendiente del backend</button>
    </form>
  </DashboardCard>
}
