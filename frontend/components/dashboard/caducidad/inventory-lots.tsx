'use client'

import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'

const dateFormatter = new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: 'short', year: 'numeric', timeZone: 'UTC' })
const grams = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })

export function InventoryLots() {
  const { roastedLots, isReady } = useRoasteryDemo()
  return (
    <DashboardCard title="Lotes en Inventario" description="Stock tostado, disponibilidad y frescura calculados por el backend.">
      <div className="-mx-6 overflow-x-auto">
        <table className="w-full min-w-[760px] text-left text-sm">
          <thead>
            <tr className="border-y border-border bg-secondary/60 text-xs font-medium text-muted-foreground">
              <th className="px-6 py-3">Lote</th>
              <th className="px-3 py-3">Origen / Varietal</th>
              <th className="px-3 py-3">F. Tueste</th>
              <th className="px-3 py-3">Caducidad</th>
              <th className="px-3 py-3 text-right">Stock</th>
              <th className="px-3 py-3">Disponibilidad</th>
              <th className="py-3 pr-6 pl-3 text-right">Frescura</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {roastedLots.map((lot) => {
              const expired = lot.freshness === 'VENCIDO'
              const warning = lot.freshness === 'POR_VENCER'
              const resting = lot.availabilityStatus === 'REPOSO'
              const available = lot.availabilityStatus === 'DISPONIBLE'
              const unavailable = lot.availabilityStatus === 'AGOTADO'
              const availableDate = lot.availableDate ? new Date(lot.availableDate) : null
              return (
                <tr key={lot.id} className="hover:bg-secondary/40">
                  <td className="px-6 py-4 font-semibold whitespace-nowrap text-foreground">{lot.code}</td>
                  <td className="px-3 py-4 text-foreground">{lot.origin}<br /><span className="text-xs text-muted-foreground">{lot.profile}</span></td>
                  <td className="px-3 py-4 whitespace-nowrap text-muted-foreground">{lot.roastDate ? dateFormatter.format(new Date(`${lot.roastDate}T00:00:00Z`)) : '-'}</td>
                  <td className="px-3 py-4 whitespace-nowrap text-muted-foreground">{lot.expirationDate ? dateFormatter.format(new Date(`${lot.expirationDate}T00:00:00Z`)) : '-'}</td>
                  <td className="px-3 py-4 text-right font-medium tabular-nums">{grams.format(lot.stockGrams)} g</td>
                  <td className="px-3 py-4">
                    <span className={cn('inline-flex rounded-full px-2.5 py-1 text-xs font-semibold', resting ? 'bg-amber-100 text-amber-800' : available ? 'bg-success/10 text-success' : unavailable || expired ? 'bg-destructive/10 text-destructive' : 'bg-secondary text-muted-foreground')}>
                      {resting ? 'En reposo' : available ? 'Disponible' : lot.availabilityStatus === 'VENCIDO' ? 'Vencido' : unavailable ? 'Agotado' : lot.availabilityStatus}
                    </span>
                    {resting && availableDate && !Number.isNaN(availableDate.getTime()) && (
                      <span className="mt-1 block text-[11px] whitespace-nowrap text-muted-foreground">Desde {dateFormatter.format(availableDate)}</span>
                    )}
                  </td>
                  <td className={cn('py-4 pr-6 pl-3 text-right font-semibold', expired ? 'text-destructive' : warning ? 'text-accent' : 'text-success')}>
                    {lot.freshness === 'POR_VENCER' ? 'Por vencer' : lot.freshness === 'VENCIDO' ? 'Vencido' : 'Óptimo'}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
        {!isReady && <p className="p-5 text-sm text-muted-foreground">Cargando lotes…</p>}
        {isReady && roastedLots.length === 0 && <p className="p-5 text-sm text-muted-foreground">No hay lotes tostados registrados.</p>}
      </div>
    </DashboardCard>
  )
}
