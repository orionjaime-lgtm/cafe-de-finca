'use client'

import { useState } from 'react'
import { Check } from 'lucide-react'
import { cn } from '@/lib/utils'
import { DashboardCard } from './dashboard-card'

type Row = { origin: string; region: string; stock: number; minimum: number }

const initialRows: Row[] = [
  { origin: 'Colombia Huila', region: 'Lavado · Caturra', stock: 2.3, minimum: 5 },
  { origin: 'Brasil Santos', region: 'Natural · Bourbon', stock: 12, minimum: 10 },
  { origin: 'Geisha Panamá', region: 'Honey · Geisha', stock: 0, minimum: 3 },
  { origin: 'Etiopía Yirgacheffe', region: 'Lavado · Heirloom', stock: 18.4, minimum: 6 },
  { origin: 'Guatemala Antigua', region: 'Lavado · Bourbon', stock: 9.7, minimum: 8 },
]

function getStatus(stock: number, minimum: number) {
  if (stock <= 0) return 'critical'
  if (stock < minimum) return 'low'
  return 'ok'
}

function StatusCell({ status }: { status: ReturnType<typeof getStatus> }) {
  if (status === 'critical') {
    return (
      <span className="rounded-md bg-destructive px-2 py-0.5 text-xs font-semibold text-white">
        Crítico
      </span>
    )
  }
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 text-sm font-medium',
        status === 'low' ? 'text-destructive' : 'text-success',
      )}
    >
      <span
        aria-hidden="true"
        className={cn('size-1.5 rounded-full', status === 'low' ? 'bg-destructive' : 'bg-success')}
      />
      {status === 'low' ? 'Bajo' : 'Ok'}
    </span>
  )
}

export function StockLimits() {
  const [rows, setRows] = useState(initialRows)
  const [saved, setSaved] = useState(false)

  function updateMinimum(origin: string, value: string) {
    const parsed = Number.parseFloat(value)
    setSaved(false)
    setRows((prev) =>
      prev.map((row) =>
        row.origin === origin ? { ...row, minimum: Number.isFinite(parsed) && parsed >= 0 ? parsed : 0 } : row,
      ),
    )
  }

  return (
    <DashboardCard
      title="Límites de Stock Mínimo"
      description="Establece el umbral de alerta según el origen del grano."
      className="h-full"
    >
      <form
        className="flex flex-1 flex-col"
        onSubmit={(e) => {
          e.preventDefault()
          setSaved(true)
        }}
      >
        <div className="-mx-6 flex-1 overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-y border-border bg-muted/50 text-left text-[11px] font-medium tracking-wider text-muted-foreground uppercase">
                <th scope="col" className="py-2.5 pr-2 pl-6 font-medium">Origen</th>
                <th scope="col" className="px-2 py-2.5 text-right font-medium">Stock</th>
                <th scope="col" className="px-2 py-2.5 text-right font-medium">Mínimo</th>
                <th scope="col" className="px-6 py-2.5 text-right font-medium">Estado</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {rows.map((row) => {
                const status = getStatus(row.stock, row.minimum)
                const inputId = `min-${row.origin.replace(/\s+/g, '-').toLowerCase()}`
                return (
                  <tr key={row.origin} className="transition-colors hover:bg-muted/40">
                    <td className="whitespace-nowrap py-3.5 pr-2 pl-6">
                      <p className="font-medium text-foreground">{row.origin}</p>
                      <p className="text-xs text-muted-foreground">{row.region}</p>
                    </td>
                    <td
                      className={cn(
                        'whitespace-nowrap px-2 py-3.5 text-right tabular-nums',
                        status === 'ok' ? 'text-foreground' : 'font-medium text-destructive',
                      )}
                    >
                      {row.stock.toFixed(1)} kg
                    </td>
                    <td className="px-2 py-3.5 text-right">
                      <label htmlFor={inputId} className="sr-only">
                        Stock mínimo para {row.origin} en kilogramos
                      </label>
                      <div className="ml-auto flex w-[4.5rem] items-center rounded-md border border-input bg-card focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/15">
                        <input
                          id={inputId}
                          type="number"
                          inputMode="decimal"
                          min={0}
                          step={0.5}
                          value={row.minimum}
                          onChange={(e) => updateMinimum(row.origin, e.target.value)}
                          className="w-full bg-transparent py-1 pl-2 text-right text-sm tabular-nums outline-none [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none"
                        />
                        <span className="pr-2 pl-1 text-xs text-muted-foreground">kg</span>
                      </div>
                    </td>
                    <td className="px-6 py-3.5 text-right">
                      <StatusCell status={status} />
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>

        <div className="mt-6 flex flex-col gap-3">
          <p className="text-xs text-muted-foreground" aria-live="polite">
            {saved
              ? 'Umbrales guardados. Las alertas se recalcularán en la próxima sincronización.'
              : 'Los cambios se aplican a todas las tostadurías vinculadas.'}
          </p>
          <button
            type="submit"
            className="inline-flex h-11 w-full items-center justify-center gap-2 rounded-xl bg-[#6b4c3a] text-sm font-semibold text-white shadow-sm transition-colors hover:bg-[#5a3f30] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          >
            {saved && <Check className="size-4" aria-hidden="true" />}
            {saved ? 'Umbrales Guardados' : 'Guardar Nuevos Umbrales'}
          </button>
        </div>
      </form>
    </DashboardCard>
  )
}
