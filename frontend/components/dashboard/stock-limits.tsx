'use client'

import { useState } from 'react'
import { useAuth } from '@/components/auth-provider'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'
import { DashboardCard } from './dashboard-card'

export function StockLimits() {
  const { stockRows, isReady, updateStockMinimum, storageError } = useRoasteryDemo()
  const { user } = useAuth()
  const [editando, setEditando] = useState<number | null>(null)
  const [minimoKg, setMinimoKg] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const puedeEditar = user?.role === 'ADMIN' || user?.role === 'TUESTE'

  async function guardar(idOrigen: number) {
    const minimo = Number(minimoKg.replace(',', '.'))
    if (!Number.isFinite(minimo) || minimo < 0) {
      setError('Ingresá un mínimo válido mayor o igual a 0.')
      return
    }
    setGuardando(true)
    setError(null)
    try {
      await updateStockMinimum(idOrigen, Math.round(minimo * 1000))
      setEditando(null)
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'No se pudo guardar el mínimo.')
    } finally {
      setGuardando(false)
    }
  }

  return <DashboardCard title="Límites de Stock Mínimo" description="Umbrales reales por origen. El mínimo 0 desactiva las alertas para ese origen." className="h-full">
    <div className="-mx-6 overflow-x-auto">
      {(error || storageError) && <p className="mx-6 mt-3 rounded-md border border-destructive/20 bg-destructive/10 px-3 py-2 text-sm text-destructive">{error ?? storageError}</p>}
      <table className="w-full text-sm"><thead><tr className="border-y border-border bg-muted/50 text-left text-[11px] font-medium tracking-wider text-muted-foreground uppercase"><th className="py-2.5 pr-2 pl-6">Origen</th><th className="px-2 py-2.5 text-right">Stock</th><th className="px-2 py-2.5 text-right">Mínimo</th><th className="px-6 py-2.5 text-right">Estado</th></tr></thead><tbody className="divide-y divide-border">{stockRows.map((row) => {
        const critical = row.estado === 'CRITICO' || row.estado === 'AGOTADO'
        const low = row.estado === 'BAJO' || row.estado === 'CRITICO'
        return <tr key={row.idOrigen} className="hover:bg-muted/40"><td className="whitespace-nowrap py-3.5 pr-2 pl-6"><p className="font-medium text-foreground">{row.pais} {row.region}</p><p className="text-xs text-muted-foreground">{row.variedad}</p></td><td className={cn('whitespace-nowrap px-2 py-3.5 text-right tabular-nums', critical ? 'font-medium text-destructive' : 'text-foreground')}>{(row.stockActualGramos / 1000).toFixed(1)} kg</td><td className="px-2 py-3.5 text-right tabular-nums">{editando === row.idOrigen ? <div className="ml-auto flex max-w-[160px] items-center justify-end gap-1"><input aria-label={`Mínimo para ${row.pais} ${row.region}`} type="number" min="0" step="0.1" value={minimoKg} onChange={(event) => setMinimoKg(event.target.value)} className="w-20 rounded-md border border-border bg-background px-2 py-1 text-right" /><span>kg</span><button type="button" disabled={guardando} onClick={() => void guardar(row.idOrigen)} className="rounded-md bg-primary px-2 py-1 text-xs text-primary-foreground disabled:opacity-50">Guardar</button></div> : <button type="button" disabled={!puedeEditar} onClick={() => { setEditando(row.idOrigen); setMinimoKg((row.stockMinimoGramos / 1000).toFixed(1)); setError(null) }} className="rounded-md px-2 py-1 tabular-nums disabled:cursor-default disabled:opacity-100">{(row.stockMinimoGramos / 1000).toFixed(1)} kg</button>}</td><td className="px-6 py-3.5 text-right"><span className={cn('rounded-md px-2 py-0.5 text-xs font-semibold', critical ? 'bg-destructive text-white' : low ? 'border border-warning/20 bg-warning/10 text-warning dark:text-amber-300' : 'border border-success/20 bg-success/10 text-success dark:text-green-300')}>{critical ? 'Crítico' : low ? 'Bajo' : 'Ok'}</span></td></tr>
      })}</tbody></table>{!isReady && <p className="p-5 text-sm text-muted-foreground">Cargando stock por origen…</p>}{isReady && stockRows.length === 0 && <p className="p-5 text-sm text-muted-foreground">No hay datos de stock.</p>}
    </div>
  </DashboardCard>
}
