'use client'

import { useEffect, useMemo, useRef, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Banknote, Building2, Check, CreditCard, Gift, Percent, ShoppingBag, Smartphone, X } from 'lucide-react'
import { apiPost } from '@/lib/api'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useAuth } from '@/components/auth-provider'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'

const precios: Record<number, number> = { 250: 5800, 500: 9800, 1000: 17500 }
type Selection = { lotId: string; presentationId: number; grindingId: number; quantity: number }
type OrderQuote = { montoTotal: number; porcentajeDescuentoAplicado: number; montoDescuentoAplicado: number; ahorroPorPlan: Array<{ planId: number; nombrePlan: string; porcentajeDescuento: number; montoAhorro: number }> }

export function CheckoutWorkspace() {
  const router = useRouter()
  const { user } = useAuth()
  const { roastedLots, presentations, grindingTypes, paymentMethods, saleChannels, customers, quoteOrder, recordCustomerOrder } = useRoasteryDemo()
  const [selection, setSelection] = useState<Selection | null>(null)
  const [payment, setPayment] = useState<'efectivo' | 'mercadopago' | 'transferencia' | 'tarjeta'>('mercadopago')
  const [quote, setQuote] = useState<OrderQuote | null>(null)
  const [showMembership, setShowMembership] = useState(false)
  const membershipPromptShown = useRef(false)
  const [selectedPlanId, setSelectedPlanId] = useState<number | null>(null)
  const [activating, setActivating] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [confirmed, setConfirmed] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    try { setSelection(JSON.parse(sessionStorage.getItem('cafe-checkout') ?? 'null')) } catch { setSelection(null) }
  }, [])

  const lot = selection ? roastedLots.find((item) => item.id === selection.lotId) : undefined
  const presentation = selection ? presentations.find((item) => item.idPresentacion === selection.presentationId) : undefined
  const grinding = selection ? grindingTypes.find((item) => item.idTipoMolienda === selection.grindingId) : undefined
  const unitPrice = presentation ? (precios[presentation.pesoGramos] ?? Math.max(2500, Math.round((presentation.pesoGramos / 250) * 5800))) : 0
  const quantity = selection?.quantity ?? 1
  const subtotal = unitPrice * quantity
  const weight = presentation ? presentation.pesoGramos * quantity : 0
  const customer = customers.find((item) => item.idCliente === user?.idCliente)
  const freeShipping = customer?.tipoCliente !== 'B2B' && weight >= 1000
  const shipping = freeShipping ? 0 : 3500
  const recommended = useMemo(() => quote?.ahorroPorPlan.reduce<OrderQuote['ahorroPorPlan'][number] | null>((best, plan) => !best || plan.porcentajeDescuento > best.porcentajeDescuento ? plan : best, null) ?? null, [quote])
  const discount = quote?.montoDescuentoAplicado ?? 0
  const total = subtotal - discount + shipping
  const currency = (value: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(value)

  useEffect(() => {
    if (!user?.idCliente || !lot || !presentation || !grinding || quantity < 1) return
    quoteOrder({ clienteId: user.idCliente, items: [{ loteTostadoId: Number(lot.numericId), presentacionId: presentation.idPresentacion, tipoMoliendaId: grinding.idTipoMolienda, cantidadEnvases: quantity, precioUnitario: unitPrice }] })
      .then((result) => { setQuote(result); if (!membershipPromptShown.current && (result.porcentajeDescuentoAplicado ?? 0) === 0 && (result.ahorroPorPlan?.length ?? 0) > 0) { membershipPromptShown.current = true; const mejor = result.ahorroPorPlan.reduce((best, plan) => plan.porcentajeDescuento > best.porcentajeDescuento ? plan : best); setSelectedPlanId(mejor.planId); setShowMembership(true) } })
      .catch((error) => setErrorMessage(error instanceof Error ? error.message : 'No se pudo calcular la compra.'))
  }, [user?.idCliente, lot?.numericId, presentation?.idPresentacion, grinding?.idTipoMolienda, quantity, unitPrice, quoteOrder])

  async function activarMembresia() {
    const selectedPlan = quote?.ahorroPorPlan.find((plan) => plan.planId === selectedPlanId) ?? recommended
    if (!selectedPlan || !user?.idCliente || !lot || !presentation || !grinding) return
    setActivating(true)
    try {
      await apiPost('/api/membresias/activar', { planId: selectedPlan.planId })
      const result = await quoteOrder({ clienteId: user.idCliente, items: [{ loteTostadoId: Number(lot.numericId), presentacionId: presentation.idPresentacion, tipoMoliendaId: grinding.idTipoMolienda, cantidadEnvases: quantity, precioUnitario: unitPrice }] })
      setQuote(result)
      setShowMembership(false)
    } catch (error) { setErrorMessage(error instanceof Error ? error.message : 'No se pudo activar la membresía.') } finally { setActivating(false) }
  }

  async function confirmar() {
    if (!selection || !lot || !presentation || !grinding || !user?.idCliente) return
    setIsSaving(true); setErrorMessage(null)
    try {
      const canal = saleChannels.find((item) => item.nombreCanal === 'Mostrador') ?? saleChannels[0]
      const medio = payment === 'efectivo'
        ? paymentMethods.find((item) => item.nombreMedio === 'Efectivo')
        : payment === 'mercadopago'
          ? paymentMethods.find((item) => item.nombreMedio.includes('(QR)')) ?? paymentMethods.find((item) => item.nombreMedio.includes('Mercado Pago'))
          : payment === 'transferencia'
            ? paymentMethods.find((item) => item.nombreMedio === 'Transferencia Bancaria') ?? paymentMethods.find((item) => item.nombreMedio.includes('Transferencia'))
            : paymentMethods.find((item) => item.nombreMedio.includes('Tarjeta'))
      const medioPago = medio ?? paymentMethods[0]
      await recordCustomerOrder({ clienteId: user.idCliente, loteTostadoId: Number(lot.numericId), presentacionId: presentation.idPresentacion, tipoMoliendaId: grinding.idTipoMolienda, cantidadEnvases: quantity, precioUnitario: unitPrice, canalVentaId: canal?.idCanalVenta ?? 1, medioPagoId: medio?.idMedioPago })
      setConfirmed(true); sessionStorage.removeItem('cafe-checkout')
    } catch (error) { setErrorMessage(error instanceof Error ? error.message : 'No se pudo confirmar la compra.') } finally { setIsSaving(false) }
  }

  const paymentOptions = [
    { value: 'efectivo' as const, label: 'Efectivo', detail: 'Pago al retirar', icon: Banknote },
    { value: 'mercadopago' as const, label: 'Mercado Pago', detail: 'Billetera o QR', icon: Smartphone },
    { value: 'transferencia' as const, label: 'Transferencia', detail: 'Desde tu cuenta bancaria', icon: Building2 },
    { value: 'tarjeta' as const, label: 'Tarjeta', detail: 'Crédito o débito', icon: CreditCard },
  ]

  if (!selection) return <div className="mt-8"><DashboardCard title="No hay una selección para pagar" description="Volvé al catálogo y elegí un café."><button type="button" onClick={() => router.push('/catalogo')} className="mt-2 h-11 rounded-lg bg-primary px-5 text-sm font-semibold text-primary-foreground">Ir al catálogo</button></DashboardCard></div>

  return <>
    {showMembership && quote?.ahorroPorPlan.length && <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/50 p-3 backdrop-blur-sm" role="dialog" aria-modal="true"><div className="w-full max-w-2xl rounded-2xl border border-border bg-card p-5 shadow-2xl max-h-[92vh] overflow-y-auto"><div className="flex items-start justify-between gap-4"><div className="flex gap-3"><span className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-primary text-primary-foreground"><Gift className="size-5" /></span><div><p className="text-xs font-semibold tracking-[0.12em] text-accent">BENEFICIO EXCLUSIVO</p><h2 className="mt-1 text-xl font-bold sm:text-2xl">Elegí tu membresía</h2><p className="mt-1 text-sm text-muted-foreground">Compará cuánto ahorrás en esta compra.</p></div></div><button type="button" aria-label="Cerrar" onClick={() => setShowMembership(false)} className="flex size-8 shrink-0 items-center justify-center rounded-lg text-muted-foreground hover:bg-muted"><X className="size-4" /></button></div><div className="mt-5 grid gap-3 sm:grid-cols-3">{quote.ahorroPorPlan.map((plan) => <button key={plan.planId} type="button" onClick={() => setSelectedPlanId(plan.planId)} className={cn('rounded-xl border p-4 text-left transition-colors', selectedPlanId === plan.planId ? 'border-primary bg-primary/5 ring-1 ring-primary/20' : 'border-border hover:bg-muted/50')}><div className="flex items-start justify-between gap-2"><p className="font-semibold">{plan.nombrePlan}</p>{selectedPlanId === plan.planId && <Check className="size-4 text-primary" />}</div><p className="mt-3 text-3xl font-bold text-primary">{plan.porcentajeDescuento}%</p><p className="mt-1 text-xs text-muted-foreground">de descuento</p><p className="mt-4 rounded-lg bg-success/10 px-3 py-2 text-sm font-semibold text-success">Ahorrás {currency(plan.montoAhorro)}</p></button>)}</div><button type="button" onClick={activarMembresia} disabled={activating || selectedPlanId === null} className="mt-5 flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary text-sm font-semibold text-primary-foreground disabled:opacity-50"><Percent className="size-4" />{activating ? 'Activando…' : 'Activar membresía seleccionada'}</button><button type="button" onClick={() => setShowMembership(false)} className="mt-2 h-10 w-full text-sm font-semibold text-muted-foreground hover:text-foreground">Continuar sin membresía</button></div></div>}
    <div className="mt-8 grid gap-6 xl:grid-cols-[1fr_360px]"><div className="space-y-6"><DashboardCard title="Método de pago" description="Seleccioná cómo querés pagar"><div className="grid gap-3 sm:grid-cols-2">{paymentOptions.map(({ value, label, detail, icon: Icon }) => <button key={value} type="button" onClick={() => { setPayment(value); setConfirmed(false) }} className={cn('flex items-center gap-3 rounded-xl border p-4 text-left', payment === value ? 'border-primary bg-primary/5 ring-1 ring-primary/20' : 'border-border hover:bg-muted/50')}><span className="flex size-9 items-center justify-center rounded-lg bg-secondary text-primary"><Icon className="size-4" /></span><span><span className="block text-sm font-medium">{label}</span><span className="mt-1 block text-xs text-muted-foreground">{detail}</span></span></button>)}</div></DashboardCard></div><aside><DashboardCard title="Resumen de compra" description="Total final de tu pedido"><div className="space-y-3 border-b border-border pb-5 text-sm"><p className="flex justify-between gap-3"><span className="text-muted-foreground">Café</span><strong className="max-w-44 text-right">{lot?.origin ?? '—'}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Presentación</span><strong>{presentation ? `${presentation.pesoGramos} g × ${quantity}` : '—'}</strong></p><p className="flex justify-between gap-3"><span className="text-muted-foreground">Molienda</span><strong>{grinding?.nombreMolienda ?? '—'}</strong></p></div><div className="space-y-2 border-b border-border py-4 text-sm"><p className="flex justify-between"><span className="text-muted-foreground">Subtotal</span><strong>{currency(subtotal)}</strong></p>{discount > 0 && <p className="flex justify-between text-success"><span>Membresía {quote?.porcentajeDescuentoAplicado}%</span><strong>− {currency(discount)}</strong></p>}<p className="flex justify-between"><span className="text-muted-foreground">Envío</span><strong>{freeShipping ? 'Gratis' : currency(shipping)}</strong></p></div><div className="flex items-end justify-between py-5"><span className="text-sm text-muted-foreground">Total</span><strong className="text-2xl">{currency(total)}</strong></div>{confirmed ? <div role="status" className="rounded-xl border border-success/20 bg-success/10 p-4"><div className="flex items-center gap-2 text-sm font-semibold text-success"><Check className="size-4" />Compra confirmada</div><button type="button" onClick={() => router.push('/compras')} className="mt-3 h-10 w-full rounded-lg border border-border bg-card text-sm font-semibold">Ver mis compras</button></div> : <button type="button" disabled={!lot || !presentation || !grinding || !quote || isSaving} onClick={confirmar} className="flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-primary text-sm font-semibold text-primary-foreground disabled:opacity-50"><ShoppingBag className="size-4" />{isSaving ? 'Procesando…' : 'Comprar ahora'}</button>}{errorMessage && <p role="alert" className="mt-3 text-xs text-destructive">{errorMessage}</p>}</DashboardCard></aside></div>
  </>
}
