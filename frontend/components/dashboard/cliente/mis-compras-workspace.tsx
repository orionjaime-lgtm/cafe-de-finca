'use client'

import { PackageCheck, Coffee } from 'lucide-react'
import Link from 'next/link'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useAuth } from '@/components/auth-provider'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

export function MisComprasWorkspace() {
  const { user } = useAuth()
  const { orders, roastedLots, presentations, grindingTypes, isReady } = useRoasteryDemo()
  const compras = orders.filter((order) => order.clienteId === user?.idCliente)
  const currency = (value: number) => new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(value)
  const dateTime = (value: string) => new Intl.DateTimeFormat('es-AR', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))

  return <div className="mt-8">
    <DashboardCard title="Mis pedidos" description="Historial de cafés que compraste.">
      {!isReady ? <p className="py-8 text-sm text-muted-foreground">Cargando compras…</p> : compras.length === 0 ? <div className="py-10 text-center"><span className="mx-auto flex size-12 items-center justify-center rounded-xl bg-secondary text-primary"><Coffee className="size-5" /></span><p className="mt-3 text-sm font-semibold">Todavía no realizaste compras</p><p className="mt-1 text-xs text-muted-foreground">Visitá el catálogo para elegir tu próximo café.</p><Link href="/catalogo" className="mt-4 inline-flex h-10 items-center rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">Ver cafés</Link></div> : <div className="space-y-3">
        {compras.map((order) => <article key={order.idPedido} className="rounded-xl border border-border bg-card p-4"><div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between"><div className="flex gap-3"><span className="flex size-10 shrink-0 items-center justify-center rounded-lg bg-secondary text-primary"><PackageCheck className="size-5" /></span><div><p className="text-sm font-semibold">Pedido #{order.idPedido}</p><p className="mt-1 text-xs text-muted-foreground">{order.fechaHoraPedido ? dateTime(order.fechaHoraPedido) : 'Fecha no disponible'}</p></div></div><div className="text-left sm:text-right"><p className="text-lg font-bold">{currency(order.montoTotal)}</p><p className="mt-1 text-xs text-muted-foreground">{order.estadoPreparacion} · {order.estadoCobro}</p></div></div><div className="mt-4 grid gap-2 border-t border-border pt-4 sm:grid-cols-2">{order.items.map((item, index) => { const lot = roastedLots.find((candidate) => candidate.numericId === item.loteTostadoId); const presentation = presentations.find((candidate) => candidate.idPresentacion === item.presentacionId); const grinding = grindingTypes.find((candidate) => candidate.idTipoMolienda === item.tipoMoliendaId); return <div key={`${order.idPedido}-${index}`} className="rounded-lg bg-muted/40 px-3 py-2"><p className="text-sm font-semibold">{lot?.origin ?? 'Café comprado'}</p><p className="mt-1 text-xs text-muted-foreground">{presentation?.nombreEnvase} · {presentation?.pesoGramos ?? 0} g · {grinding?.nombreMolienda ?? 'Molienda'} · {item.cantidadEnvases} unidad(es)</p></div> })}</div></article>)}
      </div>}
    </DashboardCard>
  </div>
}
