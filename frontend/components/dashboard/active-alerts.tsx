'use client'

import { ChevronRight } from 'lucide-react'
import { DashboardCard } from './dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

const grams = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 1 })
export function ActiveAlerts() {
  const { alerts, isReady } = useRoasteryDemo()
  return <DashboardCard title="Alertas Activas" description="Incidencias calculadas por el backend según stock mínimo y consumo." action={<span className="rounded-full bg-destructive/10 px-2.5 py-1 text-xs font-medium text-destructive">{alerts.length} abiertas</span>}>
    {!isReady ? <p className="p-2 text-sm text-muted-foreground">Cargando alertas…</p> : <ul className="flex flex-col gap-3">{alerts.map((alert) => {
      const critical = alert.nivel === 'CRITICA' || alert.nivel === 'CRÍTICA'
      return <li key={`${alert.idOrigen}-${alert.nivel}`}><div className={`group flex items-center gap-4 rounded-xl border p-4 ${critical ? 'border-destructive/20 bg-destructive/5' : 'border-warning/20 bg-warning/5'}`}><div className="min-w-0 flex-1"><div className="flex flex-wrap items-center gap-2.5"><span className={`rounded px-2 py-0.5 text-[10px] font-semibold tracking-wider text-white ${critical ? 'bg-destructive' : 'bg-warning'}`}>{critical ? 'CRÍTICO' : 'ADVERTENCIA'}</span><h3 className="text-sm font-semibold text-foreground">{alert.origen}</h3></div><p className="mt-1.5 text-sm leading-relaxed text-muted-foreground">{alert.mensaje}</p><p className="mt-2 text-xs text-muted-foreground">Stock: {grams.format(alert.stockActualGramos / 1000)} kg · Mínimo: {grams.format(alert.stockMinimoGramos / 1000)} kg{alert.diasEstimados != null ? ` · Reposición estimada: ${alert.diasEstimados} días` : ''}</p></div><ChevronRight className="size-4 shrink-0 text-muted-foreground" /></div></li>
    })}</ul>}{alerts.length === 0 && isReady && <p className="rounded-lg border border-success/20 bg-success/5 p-4 text-sm text-success">No hay alertas activas.</p>}
  </DashboardCard>
}
