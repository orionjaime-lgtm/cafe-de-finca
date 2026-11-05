'use client'

import { useMemo, useState } from 'react'
import { Info } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

const grams = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const kilograms = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 2 })
const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' })
const inputClass = 'h-11 w-full rounded-lg border border-input bg-card px-3.5 text-sm outline-none focus-visible:border-primary focus-visible:ring-3 focus-visible:ring-primary/15'

type OrderLine = { key: number; loteTostadoId: string; presentacionId: string; tipoMoliendaId: string; cantidadEnvases: string; precioUnitario: string }

export function OrderForm() {
  const { roastedLots, customers, presentations, grindingTypes, saleChannels, orders, isReady, recordOrder, quoteOrder, changeOrderStatus } = useRoasteryDemo()
  const availableLots = useMemo(() => roastedLots.filter((lot) => lot.availableForSale && lot.stockGrams > 0), [roastedLots])
  const [customerId, setCustomerId] = useState('')
  const [channelId, setChannelId] = useState('')
  const [lines, setLines] = useState<OrderLine[]>([{ key: 1, loteTostadoId: '', presentacionId: '', tipoMoliendaId: '', cantidadEnvases: '1', precioUnitario: '0' }])
  const [saving, setSaving] = useState(false)
  const [message, setMessage] = useState<string | null>(null)
  const [quote, setQuote] = useState<{ montoTotal: number; porcentajeDescuentoAplicado: number; montoDescuentoAplicado: number; ahorroPorPlan: Array<{ planId: number; nombrePlan: string; porcentajeDescuento: number; montoAhorro: number }> } | null>(null)

  function updateLine(key: number, field: keyof Omit<OrderLine, 'key'>, value: string) {
    setLines((current) => current.map((line) => line.key === key ? { ...line, [field]: value } : line))
  }

  async function refreshQuote() {
    if (!customerId || !lines.length) { setQuote(null); return }
    const validLines = lines.filter((line) => line.loteTostadoId && line.presentacionId && line.tipoMoliendaId && Number(line.cantidadEnvases) > 0)
    if (!validLines.length) { setQuote(null); return }
    try {
      const result = await quoteOrder({ clienteId: Number(customerId), items: validLines.map((line) => ({ loteTostadoId: Number(line.loteTostadoId), presentacionId: Number(line.presentacionId), tipoMoliendaId: Number(line.tipoMoliendaId), cantidadEnvases: Number(line.cantidadEnvases), precioUnitario: Number(line.precioUnitario || 0) })) })
      setQuote(result)
    } catch (error) { setMessage(error instanceof Error ? error.message : 'No se pudo cotizar el pedido.') }
  }

  async function changeStatus(pedidoId: number, estado: string) {
    try { await changeOrderStatus(pedidoId, estado); setMessage('Estado del pedido actualizado.') } catch (error) { setMessage(error instanceof Error ? error.message : 'No se pudo actualizar el estado.') }
  }

  async function submit(event: React.FormEvent) {
    event.preventDefault()
    if (!customerId || !channelId || lines.some((line) => !line.loteTostadoId || !line.presentacionId || !line.tipoMoliendaId || Number(line.cantidadEnvases) < 1)) return
    setSaving(true); setMessage(null)
    try {
      await recordOrder({ clienteId: Number(customerId), canalVentaId: Number(channelId), items: lines.map((line) => ({ loteTostadoId: Number(line.loteTostadoId), presentacionId: Number(line.presentacionId), tipoMoliendaId: Number(line.tipoMoliendaId), cantidadEnvases: Number(line.cantidadEnvases), precioUnitario: Number(line.precioUnitario || 0) })) })
      setMessage('Pedido creado y stock descontado correctamente.')
      setLines([{ key: Date.now(), loteTostadoId: '', presentacionId: '', tipoMoliendaId: '', cantidadEnvases: '1', precioUnitario: '0' }])
      setQuote(null)
    } catch (error) { setMessage(error instanceof Error ? error.message : 'No se pudo crear el pedido.') }
    finally { setSaving(false) }
  }

  const subtotal = lines.reduce((sum, line) => sum + Number(line.cantidadEnvases || 0) * Number(line.precioUnitario || 0), 0)
  const totalGramos = lines.reduce((sum, line) => { const presentation = presentations.find((item) => item.idPresentacion === Number(line.presentacionId)); return sum + Number(line.cantidadEnvases || 0) * (presentation?.pesoGramos ?? 0) }, 0)

  return <DashboardCard title="Nuevo pedido" description="Cargá uno o varios productos reales y cotizá descuentos antes de confirmar." action={<span className="rounded-full bg-secondary px-2.5 py-1 text-[11px] font-semibold text-primary">API CONECTADA</span>}>
    <form className="flex flex-col gap-6" onSubmit={submit}>
      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-2"><label className="text-sm font-medium" htmlFor="order-client">Cliente</label><select id="order-client" className={inputClass} value={customerId} onChange={(event) => { setCustomerId(event.target.value); setQuote(null) }} disabled={!isReady} required><option value="">Seleccionar cliente</option>{customers.map((customer) => <option key={customer.idCliente} value={customer.idCliente}>{customer.nombreRazonSocial} · {customer.tipoCliente}</option>)}</select></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium" htmlFor="order-channel">Canal de venta</label><select id="order-channel" className={inputClass} value={channelId} onChange={(event) => setChannelId(event.target.value)} disabled={!isReady} required><option value="">Seleccionar canal</option>{saleChannels.map((channel) => <option key={channel.idCanalVenta} value={channel.idCanalVenta}>{channel.nombreCanal}</option>)}</select></div>
      </div>

      <div className="flex flex-col gap-4"><div className="flex items-center justify-between"><h3 className="text-sm font-semibold">Renglones del pedido</h3><button type="button" onClick={() => setLines((current) => [...current, { key: Date.now(), loteTostadoId: '', presentacionId: '', tipoMoliendaId: '', cantidadEnvases: '1', precioUnitario: '0' }])} disabled={!availableLots.length} className="rounded-lg border border-primary px-3 py-2 text-xs font-semibold text-primary">Agregar renglón</button></div>
      {lines.map((line, index) => <div key={line.key} className="grid gap-3 rounded-xl border border-border p-4 sm:grid-cols-2 lg:grid-cols-3"><div className="flex flex-col gap-2"><label className="text-xs font-medium">Lote tostado {index + 1}</label><select className={inputClass} value={line.loteTostadoId} onChange={(event) => updateLine(line.key, 'loteTostadoId', event.target.value)} required><option value="">Seleccionar lote</option>{availableLots.map((lot) => <option key={lot.numericId} value={lot.numericId}>{lot.code} · {kilograms.format(lot.stockGrams / 1000)} kg</option>)}</select></div><div className="flex flex-col gap-2"><label className="text-xs font-medium">Presentación</label><select className={inputClass} value={line.presentacionId} onChange={(event) => updateLine(line.key, 'presentacionId', event.target.value)} required><option value="">Seleccionar presentación</option>{presentations.map((item) => <option key={item.idPresentacion} value={item.idPresentacion}>{item.nombreEnvase} · {grams.format(item.pesoGramos)} g</option>)}</select></div><div className="flex flex-col gap-2"><label className="text-xs font-medium">Molienda</label><select className={inputClass} value={line.tipoMoliendaId} onChange={(event) => updateLine(line.key, 'tipoMoliendaId', event.target.value)} required><option value="">Seleccionar molienda</option>{grindingTypes.map((item) => <option key={item.idTipoMolienda} value={item.idTipoMolienda}>{item.nombreMolienda}</option>)}</select></div><div className="flex flex-col gap-2"><label className="text-xs font-medium">Cantidad envases</label><input className={inputClass} type="number" min="1" step="1" value={line.cantidadEnvases} onChange={(event) => updateLine(line.key, 'cantidadEnvases', event.target.value)} required /></div><div className="flex flex-col gap-2"><label className="text-xs font-medium">Precio unitario</label><input className={inputClass} type="number" min="0" step="0.01" value={line.precioUnitario} onChange={(event) => updateLine(line.key, 'precioUnitario', event.target.value)} required /></div><div className="flex items-end"><button type="button" onClick={() => setLines((current) => current.length === 1 ? current : current.filter((item) => item.key !== line.key))} disabled={lines.length === 1} className="h-11 rounded-lg border border-destructive/30 px-3 text-xs font-semibold text-destructive disabled:opacity-40">Quitar</button></div></div>)}</div>

      <div role="status" className="flex flex-col gap-2 rounded-xl border border-[#e8d3bf] bg-[#fbf3ea] px-4 py-3.5 text-[#6b4c3a]"><p className="text-sm">Peso total: <strong>{grams.format(totalGramos)} g</strong> · Subtotal: <strong>{money.format(subtotal)}</strong></p>{quote && <p className="text-sm">Descuento aplicado: <strong>{quote.porcentajeDescuentoAplicado}%</strong> ({money.format(quote.montoDescuentoAplicado)}) · Total: <strong>{money.format(quote.montoTotal)}</strong></p>}{quote?.ahorroPorPlan.length ? <p className="text-xs">Planes disponibles: {quote.ahorroPorPlan.map((plan) => `${plan.nombrePlan} (${plan.porcentajeDescuento}%: ahorrás ${money.format(plan.montoAhorro)})`).join(' · ')}</p> : null}</div>
      {message && <p role="status" className="text-sm text-foreground">{message}</p>}
      {availableLots.length === 0 && <p role="alert" className="text-sm text-destructive">No hay lotes tostados disponibles para venta.</p>}
      <div className="flex flex-wrap gap-3"><button type="button" onClick={refreshQuote} disabled={!isReady || !customerId || !lines.length} className="h-11 rounded-lg border border-primary px-4 text-sm font-semibold text-primary">Cotizar pedido</button><button type="submit" disabled={saving || !isReady || !availableLots.length} className="h-11 rounded-lg bg-[#6b4c3a] px-4 text-sm font-semibold text-white disabled:opacity-50">{saving ? 'Creando pedido...' : 'Confirmar pedido'}</button></div>
    </form>

    <div className="mt-6 border-t border-border pt-5"><h3 className="mb-3 text-sm font-semibold">Pedidos recientes</h3><div className="flex flex-col gap-2">{orders.map((order) => <div key={order.idPedido} className="flex flex-col gap-3 rounded-lg border border-border px-3 py-3"><div className="flex flex-wrap items-start justify-between gap-3"><div><p className="text-sm font-medium">Pedido #{order.idPedido} · {money.format(order.montoTotal)}</p><p className="text-xs text-muted-foreground">Preparación: {order.estadoPreparacion} · Cobro: {order.estadoCobro}{order.montoDescuento > 0 ? ` · Descuento: ${money.format(order.montoDescuento)}` : ''}</p></div>{order.estadoPreparacion !== 'ENTREGADO' && order.estadoPreparacion !== 'CANCELADO' && <div className="flex flex-wrap gap-2"><button type="button" onClick={() => changeStatus(order.idPedido, 'EN_PREPARACION')} className="rounded-md border px-2 py-1 text-xs">Preparar</button><button type="button" onClick={() => changeStatus(order.idPedido, 'LISTO')} className="rounded-md border px-2 py-1 text-xs">Listo</button><button type="button" onClick={() => changeStatus(order.idPedido, 'ENTREGADO')} className="rounded-md border px-2 py-1 text-xs">Entregar</button></div>}</div><details className="text-xs text-muted-foreground"><summary className="cursor-pointer font-medium text-foreground">Ver {order.items.length} renglón(es)</summary><ul className="mt-2 space-y-1">{order.items.map((item, index) => <li key={`${order.idPedido}-${index}`}>Lote #{item.loteTostadoId} · Presentación #{item.presentacionId} · {item.cantidadEnvases} envase(s) · {grams.format(item.pesoTotalGramos)} g · {money.format(item.subtotal)}</li>)}</ul>{order.factura && <p className="mt-2">{order.factura.tipoComprobante} {order.factura.numeroComprobante} · Vence {order.factura.fechaVencimiento}</p>}</details></div>)}</div>{orders.length === 0 && <p className="text-sm text-muted-foreground">Todavía no hay pedidos registrados.</p>}</div>
  </DashboardCard>
}
