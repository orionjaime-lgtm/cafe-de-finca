'use client'

import { useEffect, useMemo, useState } from 'react'
import { Banknote, Building2, Check, CreditCard, Gift, Minus, Plus, Percent, ShoppingBag, Smartphone, Truck } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useAuth } from '@/components/auth-provider'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'

const preciosPresentacion: Record<number, number> = { 250: 5800, 500: 9800, 1000: 17500 }
type PurchaseRecord = { fecha: string; paquetes: number }
type WholesaleBenefit = { envioHasta: string; descuentoHasta: string; descuentoUsado: boolean }
function precioPresentacion(peso: number) { return preciosPresentacion[peso] ?? Math.max(2500, Math.round((peso / 250) * 5800)) }

export function ComprasWorkspace() {
  const { user } = useAuth()
  const { roastedLots, presentations, grindingTypes, isReady } = useRoasteryDemo()
  const disponibles = useMemo(() => roastedLots.filter((lot) => lot.availableForSale && lot.stockGrams > 0), [roastedLots])
  const [lotId, setLotId] = useState('')
  const [presentationId, setPresentationId] = useState(presentations[0]?.idPresentacion.toString() ?? '')
  const [grindingId, setGrindingId] = useState(grindingTypes[0]?.idTipoMolienda.toString() ?? '')
  const [quantity, setQuantity] = useState(1)
  const [payment, setPayment] = useState<'efectivo' | 'mercadopago' | 'transferencia' | 'tarjeta'>('mercadopago')
  const [confirmed, setConfirmed] = useState(false)
  const [purchaseHistory, setPurchaseHistory] = useState<PurchaseRecord[]>([])
  const [wholesaleBenefit, setWholesaleBenefit] = useState<WholesaleBenefit | null>(null)

  useEffect(() => {
    if (!user?.email) return
    try {
      const history = JSON.parse(localStorage.getItem(`cafe-purchases-${user.email}`) ?? '[]') as PurchaseRecord[]
      const benefit = JSON.parse(localStorage.getItem(`cafe-wholesale-benefit-${user.email}`) ?? 'null') as WholesaleBenefit | null
      setPurchaseHistory(history)
      setWholesaleBenefit(benefit)
    } catch {
      setPurchaseHistory([])
      setWholesaleBenefit(null)
    }
  }, [user?.email])

  const lot = disponibles.find((item) => item.id === lotId)
  const presentation = presentations.find((item) => item.idPresentacion.toString() === presentationId)
  const grinding = grindingTypes.find((item) => item.idTipoMolienda.toString() === grindingId)
  const unitPrice = presentation ? precioPresentacion(presentation.pesoGramos) : 0
  const subtotal = unitPrice * quantity
  const totalWeightGrams = presentation ? presentation.pesoGramos * quantity : 0
  const now = Date.now()
  const recentWholesalePurchases = purchaseHistory.filter((item) => item.paquetes >= 10 && now - new Date(item.fecha).getTime() <= 30 * 24 * 60 * 60 * 1000)
  const wholesaleAlreadyActive = wholesaleBenefit && new Date(wholesaleBenefit.envioHasta).getTime() > now
  const wholesaleDiscountActive = wholesaleBenefit && !wholesaleBenefit.descuentoUsado && new Date(wholesaleBenefit.descuentoHasta).getTime() > now
  const particularFreeShipping = user?.tipoCliente === 'particular' && totalWeightGrams >= 1000
  const freeShipping = particularFreeShipping || Boolean(wholesaleAlreadyActive)
  const discount = wholesaleDiscountActive ? Math.round(subtotal * 0.05) : 0
  const shipping = freeShipping ? 0 : 3500
  const total = subtotal - discount + shipping
  const maxQuantity = lot && presentation
    ? Math.max(1, Math.floor(lot.stockGrams / presentation.pesoGramos))
    : 99
  useEffect(() => {
    if (!presentationId && presentations[0]) setPresentationId(presentations[0].idPresentacion.toString())
    if (!grindingId && grindingTypes[0]) setGrindingId(grindingTypes[0].idTipoMolienda.toString())
  }, [presentations, grindingTypes, presentationId, grindingId])

  const paymentOptions = [
    { value: 'efectivo' as const, label: 'Efectivo', detail: 'Pago al retirar', icon: Banknote },
    { value: 'mercadopago' as const, label: 'Mercado Pago', detail: 'Billetera o QR', icon: Smartphone },
    { value: 'transferencia' as const, label: 'Transferencia', detail: 'Desde tu cuenta bancaria', icon: Building2 },
    { value: 'tarjeta' as const, label: 'Tarjeta', detail: 'Crédito o débito', icon: CreditCard },
  ]

  function confirmar() {
    if (!lot || !presentation || !grinding || quantity < 1) return
    if (quantity * presentation.pesoGramos > lot.stockGrams || !user?.email) return

    const nextHistory = [...purchaseHistory, { fecha: new Date().toISOString(), paquetes: quantity }]
    setPurchaseHistory(nextHistory)
    localStorage.setItem(`cafe-purchases-${user.email}`, JSON.stringify(nextHistory))

    let nextBenefit = wholesaleBenefit
    if (user.tipoCliente === 'mayorista') {
      const qualifyingPurchases = [...recentWholesalePurchases, { fecha: new Date().toISOString(), paquetes: quantity }].filter((item) => item.paquetes >= 10)
      if (qualifyingPurchases.length >= 2) {
        const oneMonth = new Date()
        oneMonth.setMonth(oneMonth.getMonth() + 1)
        nextBenefit = {
          envioHasta: oneMonth.toISOString(),
          descuentoHasta: oneMonth.toISOString(),
          descuentoUsado: Boolean(wholesaleDiscountActive),
        }
      }
      if (nextBenefit && wholesaleDiscountActive) nextBenefit = { ...nextBenefit, descuentoUsado: true }
      setWholesaleBenefit(nextBenefit)
      localStorage.setItem(`cafe-wholesale-benefit-${user.email}`, JSON.stringify(nextBenefit))
    }
    setConfirmed(true)
  }
  function currency(value: number) { return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(value) }

  return <div className="mt-8 grid gap-6 xl:grid-cols-[1fr_360px]">
    <div className="flex flex-col gap-6">
      <DashboardCard title="Elegí tu café" description="Catálogo real de lotes tostados disponibles y presentaciones configuradas en el sistema.">
        {!isReady && <p className="py-8 text-sm text-muted-foreground">Cargando catálogo…</p>}
        {isReady && disponibles.length === 0 && <p className="py-8 text-sm text-muted-foreground">No hay lotes disponibles para compra.</p>}
        <div className="grid gap-3 md:grid-cols-2">
          {disponibles.map((item) => <button key={item.id} type="button" onClick={() => { setLotId(item.id); setQuantity(1); setConfirmed(false) }} className={cn('rounded-xl border p-4 text-left transition-colors', lotId === item.id ? 'border-primary bg-primary/5 ring-1 ring-primary/20' : 'border-border hover:bg-muted/50')}><span className="flex items-start justify-between gap-3"><span><span className="block text-sm font-semibold">{item.origin}</span><span className="mt-1 block text-xs text-muted-foreground">{item.profile} · {item.code}</span></span>{lotId === item.id && <Check className="size-4 text-primary" />}</span><span className="mt-3 block text-xs text-muted-foreground">Stock disponible: {(item.stockGrams / 1000).toLocaleString('es-AR')} kg</span></button>)}
        </div>
      </DashboardCard>
      <DashboardCard
        title={user?.tipoCliente === 'mayorista' ? 'Beneficio mayorista' : 'Beneficio de envío particular'}
        description={user?.tipoCliente === 'mayorista'
          ? 'Compras habituales con al menos 10 paquetes por compra.'
          : 'Comprás 1 kg o más y esta compra tiene envío gratuito.'}
      >
        {user?.tipoCliente === 'particular' ? (
          <div className="flex items-center gap-4 rounded-xl border border-border bg-secondary p-4 text-secondary-foreground dark:border-border dark:bg-secondary">
            <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground"><Truck className="size-5" /></span>
            <div className="min-w-0 flex-1"><p className="text-sm font-semibold text-secondary-foreground">Envío gratis por esta compra</p><p className="mt-1 text-xs text-secondary-foreground/75 dark:text-muted-foreground">Se activa al seleccionar 1 kg de café o más.</p></div>
            <span className={cn('shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold', particularFreeShipping ? 'border border-success/30 bg-success/15 text-success dark:border-success/40 dark:text-green-300' : 'border border-border bg-card text-card-foreground')}>{particularFreeShipping ? 'ACTIVO' : `${(totalWeightGrams / 1000).toFixed(2)} / 1 kg`}</span>
          </div>
        ) : (
          <div className="space-y-3">
            <div className="flex items-center gap-4 rounded-xl border border-border bg-card p-4 text-card-foreground">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-primary text-primary-foreground"><Gift className="size-5" /></span>
              <div className="min-w-0 flex-1"><p className="text-sm font-semibold text-foreground">Beneficios mayoristas activos</p><p className="mt-1 text-xs text-muted-foreground">Envío gratis por un mes y 5% de descuento para la próxima compra.</p></div>
              <span className={cn('shrink-0 rounded-full px-2.5 py-1 text-xs font-semibold', wholesaleAlreadyActive || wholesaleDiscountActive ? 'bg-success/15 text-success' : 'bg-muted text-muted-foreground')}>{wholesaleAlreadyActive || wholesaleDiscountActive ? 'ACTIVO' : `${Math.min(recentWholesalePurchases.length, 2)} / 2 compras`}</span>
            </div>
            {!wholesaleAlreadyActive && !wholesaleDiscountActive && (
              <p className="text-xs text-muted-foreground">Progreso: {Math.min(recentWholesalePurchases.length, 2)} de 2 compras de al menos 10 paquetes en los últimos 30 días.</p>
            )}
          </div>
        )}
      </DashboardCard>
      <DashboardCard title="Configurá tu compra" description="Seleccioná presentación y molienda antes de continuar al pago.">
        <div className="grid gap-4 sm:grid-cols-2"><label><span className="mb-2 block text-sm font-medium">Presentación</span><select value={presentationId} onChange={(event) => { setPresentationId(event.target.value); setQuantity(1); setConfirmed(false) }} className="h-11 w-full rounded-lg border border-input bg-card px-3 text-sm"><option value="">Seleccionar presentación</option>{presentations.map((item) => <option key={item.idPresentacion} value={item.idPresentacion}>{item.nombreEnvase} · {item.pesoGramos} g</option>)}</select></label><label><span className="mb-2 block text-sm font-medium">Molienda</span><select value={grindingId} onChange={(event) => { setGrindingId(event.target.value); setConfirmed(false) }} className="h-11 w-full rounded-lg border border-input bg-card px-3 text-sm"><option value="">Seleccionar molienda</option>{grindingTypes.map((item) => <option key={item.idTipoMolienda} value={item.idTipoMolienda}>{item.nombreMolienda}</option>)}</select></label></div>
        <div className="mt-5 flex items-center justify-between rounded-xl border border-border bg-background p-4"><div><p className="text-sm font-medium">Cantidad</p><p className="mt-1 text-xs text-muted-foreground">Paquetes de {presentation?.pesoGramos ?? 0} g</p></div><div className="flex items-center gap-3"><button type="button" onClick={() => { setQuantity(Math.max(1, quantity - 1)); setConfirmed(false) }} className="flex size-9 items-center justify-center rounded-lg border border-border bg-card"><Minus className="size-4" /></button><span className="w-8 text-center text-sm font-semibold tabular-nums">{quantity}</span><button type="button" disabled={quantity >= maxQuantity} onClick={() => { setQuantity(Math.min(maxQuantity, quantity + 1)); setConfirmed(false) }} className="flex size-9 items-center justify-center rounded-lg border border-border bg-card disabled:cursor-not-allowed disabled:opacity-40"><Plus className="size-4" /></button></div></div>
      </DashboardCard>
      <DashboardCard title="Método de pago" description="Demostración local: no se realizará un cobro real."><div className="grid gap-3 sm:grid-cols-2">{paymentOptions.map(({ value, label, detail, icon: Icon }) => <button key={value} type="button" onClick={() => { setPayment(value); setConfirmed(false) }} className={cn('flex items-center gap-3 rounded-xl border p-4 text-left', payment === value ? 'border-primary bg-primary/5 ring-1 ring-primary/20' : 'border-border hover:bg-muted/50')}><span className="flex size-9 items-center justify-center rounded-lg bg-secondary text-primary"><Icon className="size-4" /></span><span><span className="block text-sm font-medium">{label}</span><span className="mt-1 block text-xs text-muted-foreground">{detail}</span></span></button>)}</div></DashboardCard>
    </div>
    <aside className="h-fit xl:sticky xl:top-24"><DashboardCard title="Resumen de compra" description="Demostración sin cargo real"><div className="space-y-3 border-b border-border pb-5 text-sm"><p className="flex justify-between gap-3"><span className="text-muted-foreground">Cliente</span><strong className="text-right">{user?.nombre}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Tipo</span><strong className="capitalize">{user?.tipoCliente ?? 'particular'}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Café</span><strong className="max-w-44 text-right">{lot?.origin ?? 'No seleccionado'}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Presentación</span><strong>{presentation ? `${presentation.pesoGramos} g × ${quantity}` : '—'}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Molienda</span><strong>{grinding?.nombreMolienda ?? '—'}</strong></p></div><div className="space-y-2 border-b border-border py-4 text-sm"><p className="flex justify-between"><span className="text-muted-foreground">Subtotal</span><strong>{currency(subtotal)}</strong></p>{discount > 0 && <p className="flex justify-between text-success"><span>Descuento mayorista 5%</span><strong>− {currency(discount)}</strong></p>}<p className="flex justify-between"><span className="text-muted-foreground">Envío</span><strong>{freeShipping ? 'Gratis' : currency(shipping)}</strong></p></div><div className="flex items-end justify-between py-5"><span className="text-sm text-muted-foreground">Total estimado</span><strong className="text-2xl">{currency(total)}</strong></div>{confirmed ? <div role="status" className="rounded-xl border border-success/20 bg-success/10 p-4"><div className="flex items-center gap-2 text-sm font-semibold text-success"><Check className="size-4" />Compra confirmada</div><p className="mt-2 text-xs leading-relaxed text-muted-foreground">La confirmación fue simulada. Todavía no se registró un pedido ni se realizó un pago.</p></div> : <button type="button" disabled={!lot || !presentation || !grinding} onClick={confirmar} className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary text-sm font-semibold text-primary-foreground disabled:cursor-not-allowed disabled:opacity-50"><ShoppingBag className="size-4" />Confirmar compra</button>}</DashboardCard></aside>
  </div>
}
