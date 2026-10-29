'use client'

import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'
import { DashboardCard } from './dashboard-card'

export function StockLimits() {
  const { stockRows, isReady } = useRoasteryDemo()
  return <DashboardCard title="Límites de Stock Mínimo" description="Valores reales calculados por origen; la edición no está disponible hasta que exista un endpoint de actualización." className="h-full">
    <div className="-mx-6 overflow-x-auto"><table className="w-full text-sm"><thead><tr className="border-y border-border bg-muted/50 text-left text-[11px] font-medium tracking-wider text-muted-foreground uppercase"><th className="py-2.5 pr-2 pl-6">Origen</th><th className="px-2 py-2.5 text-right">Stock</th><th className="px-2 py-2.5 text-right">Mínimo</th><th className="px-6 py-2.5 text-right">Estado</th></tr></thead><tbody className="divide-y divide-border">{stockRows.map((row) => {
      const critical = row.estado === 'CRITICO' || row.estado === 'AGOTADO'
      const low = row.estado === 'BAJO' || row.estado === 'CRITICO'
      return <tr key={row.idOrigen} className="hover:bg-muted/40"><td className="whitespace-nowrap py-3.5 pr-2 pl-6"><p className="font-medium text-foreground">{row.pais} {row.region}</p><p className="text-xs text-muted-foreground">{row.variedad}</p></td><td className={cn('whitespace-nowrap px-2 py-3.5 text-right tabular-nums', critical ? 'font-medium text-destructive' : 'text-foreground')}>{(row.stockActualGramos / 1000).toFixed(1)} kg</td><td className="px-2 py-3.5 text-right tabular-nums">{(row.stockMinimoGramos / 1000).toFixed(1)} kg</td><td className="px-6 py-3.5 text-right"><span className={cn('rounded-md px-2 py-0.5 text-xs font-semibold', critical ? 'bg-destructive text-white' : low ? 'border border-warning/20 bg-warning/10 text-warning dark:text-amber-300' : 'border border-success/20 bg-success/10 text-success dark:text-green-300')}>{critical ? 'Crítico' : low ? 'Bajo' : 'Ok'}</span></td></tr>
    })}</tbody></table>{!isReady && <p className="p-5 text-sm text-muted-foreground">Cargando stock por origen…</p>}{isReady && stockRows.length === 0 && <p className="p-5 text-sm text-muted-foreground">No hay datos de stock.</p>}</div>
  </DashboardCard>
}
