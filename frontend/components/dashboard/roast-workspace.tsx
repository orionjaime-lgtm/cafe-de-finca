'use client'

import { useMemo, useState } from 'react'
import { Check, Flame, Info } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'

const gramsFormatter = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const dateTimeFormatter = new Intl.DateTimeFormat('es-AR', {
  day: '2-digit',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
})

const profiles = ['Tueste claro', 'Tueste medio', 'Tueste oscuro']

export function RoastWorkspace() {
  const { greenLots, roastOrders, isReady, storageError, recordRoast } = useRoasteryDemo()
  const [greenLotId, setGreenLotId] = useState('')
  const [profile, setProfile] = useState(profiles[1])
  const [inputKg, setInputKg] = useState('15')
  const [lossPercent, setLossPercent] = useState('16')
  const [restingDays, setRestingDays] = useState('2')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const [formSubmitted, setFormSubmitted] = useState(false)

  function handleFormChange() {
    setSuccess(null)
    setFormSubmitted(false)
  }

  const availableLots = useMemo(() => greenLots.filter((lot) => lot.stockGrams > 0), [greenLots])
  const selectedLot = availableLots.find((lot) => lot.id === greenLotId) ?? availableLots[0]
  const amount = Number.parseFloat(inputKg)
  const loss = Number.parseFloat(lossPercent)
  const inputGrams = Number.isFinite(amount) && amount > 0 ? Math.round(amount * 1000) : 0
  const outputGrams =
    inputGrams > 0 && Number.isFinite(loss) && loss >= 0 && loss < 100
      ? Math.round(inputGrams * (1 - loss / 100))
      : 0
  const lossGrams = Math.max(0, inputGrams - outputGrams)
  const exceedsStock = Boolean(selectedLot && inputGrams > selectedLot.stockGrams)

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSuccess(null)

    const days = Number.parseInt(restingDays, 10)
    if (!isReady || !selectedLot || inputGrams <= 0 || !Number.isFinite(loss) || loss < 0 || loss >= 100 || !Number.isInteger(days) || days < 0) {
      setError('Revisá el lote y completá cantidades, merma y reposo con valores válidos.')
      return
    }
    if (exceedsStock) {
      setError('El lote seleccionado no tiene suficiente café verde para esta orden.')
      return
    }

    const saved = recordRoast({
      greenLotId: selectedLot.id,
      profile,
      inputGrams,
      lossPercent: loss,
      restingDays: days,
    })
    if (!saved) {
      setError('No se pudo registrar el tueste. Actualizá la página y volvé a intentarlo.')
      return
    }

    setError(null)
    setSuccess(`Tueste registrado: ${gramsFormatter.format(outputGrams)} g tostados. El stock verde se actualizó y se creó un nuevo lote.`)
    setFormSubmitted(true)
  }

  const fieldClass =
    'h-11 w-full rounded-lg border border-input bg-card px-3.5 text-sm text-foreground outline-none transition-colors focus-visible:border-primary focus-visible:ring-3 focus-visible:ring-primary/15'

  return (
    <div className="mt-8 flex flex-col gap-6">
      <p
        role={storageError ? 'alert' : 'note'}
        className={cn(
          'rounded-xl border px-4 py-3 text-sm leading-relaxed',
          storageError
            ? 'border-red-200 bg-red-50 text-destructive'
            : 'border-[#e8d3bf] bg-[#fbf3ea] text-[#6b4c3a]',
        )}
      >
        {storageError ??
          'Modo demostración: la orden descuenta el café verde y crea un lote tostado en este navegador. Todavía no se conecta al backend.'}
      </p>

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(300px,0.65fr)]">
        <DashboardCard
          title="Registrar orden de tueste"
          description="Elegí un lote verde y registrá el perfil, la merma real y el tiempo de reposo."
          action={
            <span className="inline-flex size-9 items-center justify-center rounded-lg bg-[#f3e8dd] text-[#6b4c3a]">
              <Flame className="size-4.5" aria-hidden="true" />
            </span>
          }
        >
          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            <div className="flex flex-col gap-2">
              <label htmlFor="roast-green-lot" className="text-sm font-medium">Lote de café verde</label>
              <select
                id="roast-green-lot"
                value={selectedLot?.id ?? ''}
                onChange={(event) => { setGreenLotId(event.target.value); handleFormChange() }}
                disabled={!availableLots.length}
                className={fieldClass}
              >
                {!availableLots.length && <option value="">No hay lotes con stock disponible</option>}
                {availableLots.map((lot) => (
                  <option key={lot.id} value={lot.id}>
                    {lot.id} · {lot.origin} {lot.region} · {lot.variety}
                  </option>
                ))}
              </select>
              {selectedLot && (
                <p className="text-xs text-muted-foreground">
                  Disponible: {gramsFormatter.format(selectedLot.stockGrams)} g (
                  {(selectedLot.stockGrams / 1000).toLocaleString('es-AR', { maximumFractionDigits: 2 })} kg)
                </p>
              )}
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <div className="flex flex-col gap-2">
                <label htmlFor="roast-profile" className="text-sm font-medium">Perfil de tueste</label>
                <select id="roast-profile" value={profile} onChange={(event) => { setProfile(event.target.value); handleFormChange() }} className={fieldClass}>
                  {profiles.map((option) => <option key={option}>{option}</option>)}
                </select>
              </div>
              <div className="flex flex-col gap-2">
                <label htmlFor="roast-input" className="text-sm font-medium">Café verde a tostar</label>
                <div className="flex items-center rounded-lg border border-input bg-card focus-within:border-primary focus-within:ring-3 focus-within:ring-primary/15">
                  <input id="roast-input" type="number" inputMode="decimal" min="0.001" step="any" value={inputKg} onChange={(event) => { setInputKg(event.target.value); handleFormChange() }} required className="h-11 w-full bg-transparent px-3.5 text-sm tabular-nums outline-none" />
                  <span className="pr-3 text-xs text-muted-foreground">kg</span>
                </div>
              </div>
              <div className="flex flex-col gap-2">
                <label htmlFor="roast-loss" className="text-sm font-medium">Merma real</label>
                <div className="flex items-center rounded-lg border border-input bg-card focus-within:border-primary focus-within:ring-3 focus-within:ring-primary/15">
                  <input id="roast-loss" type="number" min="0" max="99.99" step="0.1" value={lossPercent} onChange={(event) => { setLossPercent(event.target.value); handleFormChange() }} required className="h-11 w-full bg-transparent px-3.5 text-sm tabular-nums outline-none" />
                  <span className="pr-3 text-xs text-muted-foreground">%</span>
                </div>
              </div>
              <div className="flex flex-col gap-2">
                <label htmlFor="roast-rest" className="text-sm font-medium">Reposo antes de la venta</label>
                <div className="flex items-center rounded-lg border border-input bg-card focus-within:border-primary focus-within:ring-3 focus-within:ring-primary/15">
                  <input id="roast-rest" type="number" min="0" step="1" value={restingDays} onChange={(event) => { setRestingDays(event.target.value); handleFormChange() }} required className="h-11 w-full bg-transparent px-3.5 text-sm tabular-nums outline-none" />
                  <span className="pr-3 text-xs text-muted-foreground">días</span>
                </div>
              </div>
            </div>

            <div role={exceedsStock ? 'alert' : 'status'} className="flex items-start gap-3 rounded-xl border border-[#e8d3bf] bg-[#fbf3ea] px-4 py-3.5 text-[#6b4c3a]">
              <Info className="mt-0.5 size-4.5 shrink-0" aria-hidden="true" />
              <div className="text-sm leading-relaxed">
                <p>Salida estimada según la merma informada: <strong className="tabular-nums">{gramsFormatter.format(outputGrams)} g tostados</strong>.</p>
                <p className="mt-1 text-xs">Merma: {gramsFormatter.format(lossGrams)} g · queda disponible tras {restingDays || '0'} días de reposo.</p>
                {exceedsStock && <p className="mt-1 font-semibold">La cantidad supera el stock de este lote.</p>}
              </div>
            </div>

            {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
            {success && <p role="status" className="text-sm leading-relaxed text-success">{success}</p>}
            {storageError && <p role="alert" className="text-sm leading-relaxed text-destructive">{storageError}</p>}

            <button
              type="submit"
              disabled={!isReady || !selectedLot || inputGrams <= 0 || outputGrams <= 0 || exceedsStock || formSubmitted}
              className="inline-flex h-12 items-center justify-center gap-2 rounded-lg bg-[#6b4c3a] px-4 text-sm font-semibold text-white shadow-sm transition-colors hover:bg-[#5a3f30] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring disabled:cursor-not-allowed disabled:opacity-50"
            >
              <Check className="size-4.5" aria-hidden="true" />
              Registrar tueste y actualizar inventario
            </button>
          </form>
        </DashboardCard>

        <div className="flex flex-col gap-6">
          <DashboardCard
            title="Resumen de producción"
            description="Lo que generará esta orden."
          >
            <dl className="flex flex-col divide-y divide-border">
              {[
                ['Café verde de entrada', `${(inputGrams / 1000).toLocaleString('es-AR', { maximumFractionDigits: 2 })} kg`],
                ['Merma estimada', `${(lossGrams / 1000).toLocaleString('es-AR', { maximumFractionDigits: 2 })} kg (${Number.isFinite(loss) ? loss : 0}%)`],
                ['Café tostado resultante', `${(outputGrams / 1000).toLocaleString('es-AR', { maximumFractionDigits: 2 })} kg`],
                ['Reposo', `${restingDays || 0} días`],
              ].map(([label, value]) => (
                <div key={label} className="flex items-center justify-between gap-4 py-3 first:pt-0 last:pb-0">
                  <dt className="text-sm text-muted-foreground">{label}</dt>
                  <dd className="text-sm font-semibold tabular-nums text-foreground">{value}</dd>
                </div>
              ))}
            </dl>
            <div className="mt-5 rounded-xl bg-secondary/70 px-4 py-3 text-xs leading-relaxed text-muted-foreground">
              La merma se calcula sobre el peso verde ingresado. El lote tostado hereda su origen, perfil y fechas de reposo y caducidad.
            </div>
          </DashboardCard>

          <DashboardCard
            title="Stock verde por lote"
            description="Disponibilidad antes de confirmar la orden."
          >
            <ul className="flex flex-col divide-y divide-border">
              {greenLots.map((lot) => {
                const isSelected = lot.id === selectedLot?.id
                return (
                  <li key={lot.id} className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                    <div className="min-w-0">
                      <p className={cn('truncate text-sm font-medium', isSelected ? 'text-primary' : 'text-foreground')}>{lot.id} · {lot.origin} {lot.region}</p>
                      <p className="mt-0.5 text-xs text-muted-foreground">{lot.variety}</p>
                    </div>
                    <p className={cn('shrink-0 text-sm font-semibold tabular-nums', lot.stockGrams <= lot.minimumGrams ? 'text-destructive' : 'text-foreground')}>
                      {(lot.stockGrams / 1000).toLocaleString('es-AR', { maximumFractionDigits: 2 })} kg
                    </p>
                  </li>
                )
              })}
            </ul>
          </DashboardCard>
        </div>
      </div>

      <DashboardCard
        title="Órdenes de tueste recientes"
        description="Historial de producción con entrada, merma y peso tostado resultante."
      >
        <div className="-mx-6 overflow-x-auto">
          <table className="w-full min-w-[720px] text-left text-sm">
            <thead>
              <tr className="border-y border-border bg-muted/50 text-xs font-medium text-muted-foreground">
                <th scope="col" className="px-6 py-3 font-medium">Orden / Origen</th>
                <th scope="col" className="px-3 py-3 font-medium">Perfil</th>
                <th scope="col" className="px-3 py-3 text-right font-medium">Entrada verde</th>
                <th scope="col" className="px-3 py-3 text-right font-medium">Merma</th>
                <th scope="col" className="px-3 py-3 text-right font-medium">Salida tostada</th>
                <th scope="col" className="px-6 py-3 text-right font-medium">Fecha</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {roastOrders.map((order) => (
                <tr key={order.id} className="transition-colors hover:bg-muted/40">
                  <td className="px-6 py-4">
                    <p className="font-semibold text-foreground">{order.id}</p>
                    <p className="mt-0.5 text-xs text-muted-foreground">{order.origin}</p>
                  </td>
                  <td className="px-3 py-4 text-foreground">{order.profile}</td>
                  <td className="px-3 py-4 text-right tabular-nums">{gramsFormatter.format(order.greenInputGrams)} g</td>
                  <td className="px-3 py-4 text-right tabular-nums text-accent">{order.lossPercent}% · {gramsFormatter.format(order.greenInputGrams - order.roastedOutputGrams)} g</td>
                  <td className="px-3 py-4 text-right font-semibold tabular-nums text-foreground">{gramsFormatter.format(order.roastedOutputGrams)} g</td>
                  <td className="px-6 py-4 text-right whitespace-nowrap text-muted-foreground">{dateTimeFormatter.format(new Date(order.createdAt))}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </DashboardCard>
    </div>
  )
}
