'use client'

import { useEffect, useMemo, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Check, Minus, Plus, ShoppingBag } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'

const precios: Record<number, number> = { 250: 5800, 500: 9800, 1000: 17500 }

export function CatalogoWorkspace() {
  const router = useRouter()
  const { roastedLots, presentations, grindingTypes, isReady } = useRoasteryDemo()
  const disponibles = useMemo(() => roastedLots.filter((lot) => lot.availableForSale && lot.stockGrams > 0), [roastedLots])
  const [lotId, setLotId] = useState(disponibles[0]?.id ?? '')
  const [presentationId, setPresentationId] = useState(presentations[0]?.idPresentacion.toString() ?? '')
  const [grindingId, setGrindingId] = useState(grindingTypes[0]?.idTipoMolienda.toString() ?? '')
  const [quantity, setQuantity] = useState(1)

  useEffect(() => {
    if (!lotId && disponibles.length > 0) setLotId(disponibles[0].id)
  }, [disponibles, lotId])

  useEffect(() => {
    if (!presentationId && presentations.length > 0) setPresentationId(presentations[0].idPresentacion.toString())
  }, [presentations, presentationId])

  useEffect(() => {
    if (!grindingId && grindingTypes.length > 0) setGrindingId(grindingTypes[0].idTipoMolienda.toString())
  }, [grindingTypes, grindingId])

  const lot = disponibles.find((item) => item.id === lotId)
  const presentation = presentations.find((item) => item.idPresentacion.toString() === presentationId)
  const grinding = grindingTypes.find((item) => item.idTipoMolienda.toString() === grindingId)
  const unitPrice = presentation ? (precios[presentation.pesoGramos] ?? Math.max(2500, Math.round((presentation.pesoGramos / 250) * 5800))) : 0
  const subtotal = unitPrice * quantity
  const maxQuantity = lot && presentation ? Math.max(1, Math.floor(lot.stockGrams / presentation.pesoGramos)) : 99
  const currency = (value: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(value)

  function irAlCheckout() {
    if (!lot || !presentation || !grinding || quantity < 1) return
    sessionStorage.setItem('cafe-checkout', JSON.stringify({ lotId: lot.id, presentationId: presentation.idPresentacion, grindingId: grinding.idTipoMolienda, quantity }))
    router.push('/checkout')
  }

  return <div className="mt-8 grid gap-6 xl:grid-cols-[1fr_360px]">
    <div className="space-y-6">
      <DashboardCard title="Cafés disponibles" description="Elegí el origen, tueste y variedad que querés comprar.">
        {!isReady ? <p className="py-8 text-sm text-muted-foreground">Cargando catálogo…</p> : disponibles.length === 0 ? <p className="py-8 text-sm text-muted-foreground">No hay lotes disponibles para compra.</p> : <div className="grid gap-3 md:grid-cols-2">
          {disponibles.map((item) => <button key={item.id} type="button" onClick={() => { setLotId(item.id); setQuantity(1) }} className={cn('rounded-xl border p-4 text-left transition-colors', lotId === item.id ? 'border-primary bg-primary/5 ring-1 ring-primary/20' : 'border-border hover:bg-muted/50')}>
            <span className="flex items-start justify-between gap-3"><span><span className="block text-sm font-semibold">{item.origin}</span><span className="mt-1 block text-xs text-muted-foreground">{item.profile} · {item.code}</span></span>{lotId === item.id && <Check className="size-4 text-primary" />}</span>
            <span className="mt-3 block text-xs text-muted-foreground">Stock disponible: {(item.stockGrams / 1000).toLocaleString('es-AR')} kg</span>
          </button>)}
        </div>}
      </DashboardCard>
      <DashboardCard title="Configurá el café" description="Seleccioná presentación, molienda y cantidad.">
        <div className="grid gap-4 sm:grid-cols-2">
          <label><span className="mb-2 block text-sm font-medium">Presentación</span><select value={presentationId} onChange={(event) => { setPresentationId(event.target.value); setQuantity(1) }} className="h-11 w-full rounded-lg border border-input bg-card px-3 text-sm"><option value="">Seleccionar presentación</option>{presentations.map((item) => <option key={item.idPresentacion} value={item.idPresentacion}>{item.nombreEnvase} · {item.pesoGramos} g</option>)}</select></label>
          <label><span className="mb-2 block text-sm font-medium">Molienda</span><select value={grindingId} onChange={(event) => setGrindingId(event.target.value)} className="h-11 w-full rounded-lg border border-input bg-card px-3 text-sm"><option value="">Seleccionar molienda</option>{grindingTypes.map((item) => <option key={item.idTipoMolienda} value={item.idTipoMolienda}>{item.nombreMolienda}</option>)}</select></label>
        </div>
        <div className="mt-5 flex items-center justify-between rounded-xl border border-border bg-background p-4"><div><p className="text-sm font-medium">Cantidad</p><p className="mt-1 text-xs text-muted-foreground">Paquetes de {presentation?.pesoGramos ?? 0} g</p></div><div className="flex items-center gap-3"><button type="button" onClick={() => setQuantity(Math.max(1, quantity - 1))} className="flex size-9 items-center justify-center rounded-lg border border-border bg-card"><Minus className="size-4" /></button><span className="w-8 text-center text-sm font-semibold tabular-nums">{quantity}</span><button type="button" disabled={quantity >= maxQuantity} onClick={() => setQuantity(Math.min(maxQuantity, quantity + 1))} className="flex size-9 items-center justify-center rounded-lg border border-border bg-card disabled:opacity-40"><Plus className="size-4" /></button></div></div>
      </DashboardCard>
    </div>
    <aside className="h-fit xl:sticky xl:top-24"><DashboardCard title="Tu selección" description="Revisá antes de continuar al pago"><div className="space-y-3 border-b border-border pb-5 text-sm"><p className="flex justify-between gap-3"><span className="text-muted-foreground">Café</span><strong className="max-w-44 text-right">{lot?.origin ?? 'No seleccionado'}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Presentación</span><strong>{presentation ? `${presentation.pesoGramos} g × ${quantity}` : '—'}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Molienda</span><strong>{grinding?.nombreMolienda ?? '—'}</strong></p></div><div className="flex items-end justify-between py-5"><span className="text-sm text-muted-foreground">Subtotal</span><strong className="text-2xl">{currency(subtotal)}</strong></div><button type="button" disabled={!lot || !presentation || !grinding} onClick={irAlCheckout} className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary text-sm font-semibold text-primary-foreground disabled:opacity-50"><ShoppingBag className="size-4" />Comprar</button></DashboardCard></aside>
  </div>
}
