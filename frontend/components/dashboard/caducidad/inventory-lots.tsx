'use client'

import { useState } from 'react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { cn } from '@/lib/utils'

type Lot = {
  id: string
  origin: string
  roastDate: string
  shelfLife: number
  daysLeft: number
}

const lots: Lot[] = [
  { id: 'LT-1082', origin: 'Caturra Huila - Tueste Medio', roastDate: '12 Ene 2026', shelfLife: 30, daysLeft: 26 },
  { id: 'LT-1079', origin: 'Bourbon Nariño - Tueste Oscuro', roastDate: '04 Ene 2026', shelfLife: 30, daysLeft: 18 },
  { id: 'LT-1055', origin: 'Geisha Panamá - Origen Puro', roastDate: '21 Dic 2025', shelfLife: 30, daysLeft: 4 },
  { id: 'LT-1041', origin: 'Colombia Huila - Blend Especial', roastDate: '10 Dic 2025', shelfLife: 30, daysLeft: 0 },
]

function remainingLabel(daysLeft: number) {
  if (daysLeft <= 0) return { text: 'Vencido', className: 'text-destructive' }
  if (daysLeft <= 7) return { text: `${daysLeft} días`, className: 'text-accent' }
  return { text: `${daysLeft} días`, className: 'text-success' }
}

const checkboxClass = 'size-4 cursor-pointer rounded accent-[#6b4c3a]'

export function InventoryLots() {
  const [selected, setSelected] = useState<Set<string>>(() => new Set(['LT-1041']))

  const allSelected = selected.size === lots.length
  const noneSelected = selected.size === 0

  function toggle(id: string) {
    setSelected((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id)
      else next.add(id)
      return next
    })
  }

  function toggleAll() {
    setSelected(allSelected ? new Set() : new Set(lots.map((lot) => lot.id)))
  }

  return (
    <DashboardCard
      title="Lotes en Inventario"
      description="Frescura calculada desde la fecha de tueste (Vida útil ideal: 30 días)."
      action={
        <div className="flex shrink-0 gap-2">
          <button
            type="button"
            disabled={noneSelected}
            className="h-9 rounded-lg border border-input bg-card px-3.5 text-sm font-medium text-foreground shadow-xs transition-colors hover:bg-secondary disabled:cursor-not-allowed disabled:opacity-50"
          >
            Aplicar Descuento
          </button>
          <button
            type="button"
            disabled={noneSelected}
            className="h-9 rounded-lg border border-destructive/60 bg-card px-3.5 text-sm font-medium text-destructive shadow-xs transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Descartar
          </button>
        </div>
      }
    >
      <div className="-mx-6 overflow-x-auto">
        <table className="w-full min-w-[640px] text-left text-sm">
          <thead>
            <tr className="border-y border-border bg-secondary/60 text-xs font-medium text-muted-foreground">
              <th scope="col" className="w-12 py-3 pr-2 pl-6">
                <input
                  type="checkbox"
                  aria-label="Seleccionar todos los lotes"
                  checked={allSelected}
                  ref={(el) => {
                    if (el) el.indeterminate = !allSelected && !noneSelected
                  }}
                  onChange={toggleAll}
                  className={checkboxClass}
                />
              </th>
              <th scope="col" className="px-3 py-3 font-medium">Lote</th>
              <th scope="col" className="px-3 py-3 font-medium">Origen / Varietal</th>
              <th scope="col" className="px-3 py-3 font-medium">F. Tueste</th>
              <th scope="col" className="px-3 py-3 font-medium">Vida Útil</th>
              <th scope="col" className="py-3 pr-6 pl-3 text-right font-medium">Días Restantes</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {lots.map((lot) => {
              const remaining = remainingLabel(lot.daysLeft)
              const isSelected = selected.has(lot.id)
              return (
                <tr key={lot.id} className={cn('transition-colors', isSelected ? 'bg-[#fbf3ea]' : 'hover:bg-secondary/40')}>
                  <td className="py-4 pr-2 pl-6">
                    <input
                      type="checkbox"
                      aria-label={`Seleccionar lote ${lot.id}`}
                      checked={isSelected}
                      onChange={() => toggle(lot.id)}
                      className={checkboxClass}
                    />
                  </td>
                  <td className="px-3 py-4 font-semibold whitespace-nowrap tabular-nums text-foreground">{lot.id}</td>
                  <td className="px-3 py-4 text-foreground">{lot.origin}</td>
                  <td className="px-3 py-4 whitespace-nowrap text-muted-foreground">{lot.roastDate}</td>
                  <td className="px-3 py-4 whitespace-nowrap text-muted-foreground">{lot.shelfLife} días</td>
                  <td className={cn('py-4 pr-6 pl-3 text-right font-bold whitespace-nowrap tabular-nums', remaining.className)}>
                    {remaining.text}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
      <p className="mt-4 text-xs text-muted-foreground">
        {selected.size} de {lots.length} lotes seleccionados
      </p>
    </DashboardCard>
  )
}
