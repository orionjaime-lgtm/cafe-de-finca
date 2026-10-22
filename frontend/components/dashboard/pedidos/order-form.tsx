'use client'

import { useState } from 'react'
import { Check, ChevronDown, Info } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'

const GRAMS_PER_UNIT = { lb: 453.59237, kg: 1000, g: 1 } as const
type Unit = keyof typeof GRAMS_PER_UNIT
type ConfirmedOrder = {
  client: string
  grams: number
  lotId: string
}

const grindOptions = ['Entero', 'Fina (Espresso)', 'Filtro']

const gramsFormatter = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const kilogramsFormatter = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 2 })

const inputClass =
  'h-11 w-full rounded-lg border border-input bg-card px-3.5 text-sm text-foreground shadow-xs outline-none transition-colors placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-3 focus-visible:ring-primary/15'

const labelClass = 'text-sm font-medium text-foreground'

export function OrderForm() {
  const { roastedLots, isReady, recordCustomerOrder } = useRoasteryDemo()
  const [client, setClient] = useState('Camila Vergara')
  const [roastedLotId, setRoastedLotId] = useState('LT-1082')
  const [quantity, setQuantity] = useState('3')
  const [unit, setUnit] = useState<Unit>('lb')
  const [grind, setGrind] = useState(grindOptions[0])
  const [confirmedOrder, setConfirmedOrder] = useState<ConfirmedOrder | null>(null)
  const [error, setError] = useState<string | null>(null)
  const confirmed = confirmedOrder !== null

  const amount = Number.parseFloat(quantity)
  const grams = Number.isFinite(amount) && amount > 0 ? Math.round(amount * GRAMS_PER_UNIT[unit]) : 0
  const availableLots = roastedLots.filter((lot) => lot.stockGrams > 0)
  const selectedLot =
    availableLots.find((lot) => lot.id === roastedLotId) ?? availableLots[0]
  const totalRoastedGrams = roastedLots.reduce((total, lot) => total + lot.stockGrams, 0)

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (confirmed || grams <= 0 || !client.trim() || !selectedLot) return
    if (!recordCustomerOrder({ roastedLotId: selectedLot.id, quantityGrams: grams })) {
      setError('No hay suficiente stock en el lote seleccionado para este pedido.')
      return
    }
    setError(null)
    setConfirmedOrder({ client, grams, lotId: selectedLot.id })
  }

  return (
    <DashboardCard
      title="Detalle del pedido"
      description="Completa los datos para descontar del lote disponible."
      action={
        <span className="shrink-0 rounded-full bg-secondary px-2.5 py-1 text-[11px] font-semibold tracking-[0.08em] text-primary">
          {confirmed ? 'CONFIRMADO' : 'BORRADOR'}
        </span>
      }
    >
      <form
        onSubmit={handleSubmit}
        onChange={() => {
          setConfirmedOrder(null)
          setError(null)
        }}
        className="flex flex-col gap-6"
      >
        <div className="flex flex-col gap-2">
          <label htmlFor="client" className={labelClass}>
            Cliente
          </label>
          <input
            id="client"
            name="client"
            type="text"
            autoComplete="off"
            value={client}
            onChange={(e) => setClient(e.target.value)}
            className={inputClass}
          />
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="coffee" className={labelClass}>
            Café Disponible
          </label>
          <p className="text-xs text-muted-foreground">
            Stock tostado en inventario: {isReady ? `${kilogramsFormatter.format(totalRoastedGrams / 1000)} kg` : 'cargando…'}
          </p>
          <div className="relative">
            <select
              id="coffee"
              name="coffee"
              value={selectedLot?.id ?? ''}
              onChange={(e) => setRoastedLotId(e.target.value)}
              disabled={!isReady || availableLots.length === 0}
              className={cn(inputClass, 'appearance-none pr-10')}
            >
              {availableLots.length > 0 ? (
                availableLots.map((lot) => (
                  <option key={lot.id} value={lot.id}>
                    {lot.origin} · {lot.profile} — {kilogramsFormatter.format(lot.stockGrams / 1000)} kg
                  </option>
                ))
              ) : (
                <option value="">No hay café tostado disponible</option>
              )}
            </select>
            <ChevronDown
              className="pointer-events-none absolute top-1/2 right-3.5 size-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden="true"
            />
          </div>
        </div>

        <div className="flex flex-col gap-2">
          <label htmlFor="quantity" className={labelClass}>
            Cantidad
          </label>
          <div className="flex gap-3">
            <input
              id="quantity"
              name="quantity"
              type="number"
              inputMode="decimal"
              min="0"
              step="any"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              className={cn(inputClass, 'flex-1 tabular-nums')}
            />
            <div
              role="radiogroup"
              aria-label="Unidad de medida"
              className="flex shrink-0 rounded-lg border border-input bg-secondary p-1"
            >
              {(Object.keys(GRAMS_PER_UNIT) as Unit[]).map((option) => (
                <button
                  key={option}
                  type="button"
                  role="radio"
                  aria-checked={unit === option}
                  onClick={() => {
                    setUnit(option)
                    setConfirmedOrder(null)
                  }}
                  className={cn(
                    'min-w-12 rounded-md px-3 text-sm font-medium transition-colors',
                    unit === option
                      ? 'bg-primary text-primary-foreground shadow-sm'
                      : 'text-muted-foreground hover:text-foreground',
                  )}
                >
                  {option}
                </button>
              ))}
            </div>
          </div>

          <div
            role="status"
            className="mt-2 flex items-start gap-3 rounded-xl border border-[#e8d3bf] bg-[#fbf3ea] px-4 py-3.5 text-[#6b4c3a]"
          >
            <Info className="mt-0.5 size-4.5 shrink-0" aria-hidden="true" />
            <p className="text-sm leading-relaxed">
              Estandarizado en el sistema para control de stock:{' '}
              <strong className="font-semibold tabular-nums">= {gramsFormatter.format(grams)} gramos</strong> de café
              tostado
            </p>
          </div>
          {!confirmed && selectedLot && grams > selectedLot.stockGrams && (
            <p role="alert" className="text-sm text-destructive">
              El lote seleccionado tiene {gramsFormatter.format(selectedLot.stockGrams)} g disponibles.
            </p>
          )}
        </div>

        <fieldset className="flex flex-col gap-3">
          <legend className={cn(labelClass, 'mb-3')}>Tipo de Molienda</legend>
          <div className="grid gap-3 sm:grid-cols-3">
            {grindOptions.map((option) => (
              <label
                key={option}
                className={cn(
                  'flex cursor-pointer items-center gap-3 rounded-lg border px-4 py-3 text-sm transition-colors has-focus-visible:ring-3 has-focus-visible:ring-primary/15',
                  grind === option
                    ? 'border-primary bg-[#fbf3ea] font-medium text-foreground'
                    : 'border-input bg-card text-muted-foreground hover:border-stone-300',
                )}
              >
                <input
                  type="radio"
                  name="grind"
                  value={option}
                  checked={grind === option}
                  onChange={() => setGrind(option)}
                  className="size-4 accent-[#6b4c3a]"
                />
                {option}
              </label>
            ))}
          </div>
        </fieldset>

        <div className="border-t border-border pt-6">
          <button
            type="submit"
            disabled={!isReady || !selectedLot || grams <= 0 || !client.trim() || confirmed}
            className="flex h-12 w-full items-center justify-center gap-2 rounded-lg bg-[#6b4c3a] text-sm font-semibold text-white shadow-sm transition-colors hover:bg-[#5a3f30] focus-visible:ring-3 focus-visible:ring-primary/30 focus-visible:outline-none disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Check className="size-4.5" aria-hidden="true" />
            Confirmar y Descontar Stock
          </button>
          {error && (
            <p role="alert" className="mt-3 text-center text-sm text-destructive">
              {error}
            </p>
          )}
          {confirmed && (
            <p role="status" className="mt-3 text-center text-sm text-success">
              Pedido de {confirmedOrder.client} registrado: {gramsFormatter.format(confirmedOrder.grams)} g descontados del lote {confirmedOrder.lotId}.
            </p>
          )}
        </div>
      </form>
    </DashboardCard>
  )
}
