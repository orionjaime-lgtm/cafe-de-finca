'use client'

import { CheckCircle2 } from 'lucide-react'
import { DashboardCard } from '../dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' })

export function B2cSales() {
  const { b2cSales } = useRoasteryDemo()
  return <DashboardCard title="Últimos Cobros B2C (Mostrador / Instagram)" description="Ventas directas recientes, cobradas al momento y sin deuda."><ul className="flex flex-col divide-y divide-border">{b2cSales.map((sale) => <li key={sale.pagoId} className="flex items-center gap-3 py-3.5 first:pt-0"><span aria-hidden="true" className="flex size-9 shrink-0 items-center justify-center rounded-full bg-secondary text-xs font-semibold text-primary">{sale.cliente.charAt(0)}</span><div className="min-w-0 flex-1"><p className="text-sm font-medium text-foreground">{sale.cliente}<span className="font-normal text-muted-foreground"> · Pedido #{sale.pedidoId}</span></p><p className="mt-0.5 text-xs text-muted-foreground">{sale.medioPago} · {money.format(sale.montoPagado)} · {sale.fechaHoraPago?.replace('T', ' ').slice(0, 16)}</p></div><span className="inline-flex shrink-0 items-center gap-1.5 rounded-md border border-success/20 bg-success/10 px-2 py-0.5 text-xs font-semibold text-success dark:text-green-300"><CheckCircle2 className="size-3.5" />Pagado</span></li>)}</ul>{b2cSales.length === 0 && <p className="py-4 text-sm text-muted-foreground">No hay cobros B2C registrados.</p>}</DashboardCard>
}
