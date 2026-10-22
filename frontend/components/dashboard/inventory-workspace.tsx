'use client'

import { useMemo, useState } from 'react'
import { Check, PackagePlus, RotateCcw } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { cn } from '@/lib/utils'

type InventoryTab = 'green' | 'roasted'

const gramsFormatter = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const kilogramsFormatter = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 2 })
const dateFormatter = new Intl.DateTimeFormat('es-AR', {
  day: '2-digit',
  month: 'short',
  year: 'numeric',
  timeZone: 'UTC',
})

function dateLabel(date: string) {
  return dateFormatter.format(new Date(`${date}T00:00:00Z`))
}

function getDaysRemaining(expirationDate: string) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const expiration = new Date(`${expirationDate}T00:00:00`)
  return Math.ceil((expiration.getTime() - today.getTime()) / 86_400_000)
}

function remainingLabel(days: number) {
  if (days < 0) {
    return { text: 'Vencido', className: 'bg-red-50 text-destructive' }
  }
  if (days <= 7) {
    return { text: days === 0 ? 'Vence hoy' : `${days} días`, className: 'bg-amber-50 text-amber-700' }
  }
  return { text: `${days} días`, className: 'bg-emerald-50 text-emerald-700' }
}

function DemoNotice({ error }: { error: string | null }) {
  return (
    <p
      role={error ? 'alert' : 'note'}
      className={cn(
        'rounded-xl border px-4 py-3 text-sm leading-relaxed',
        error
          ? 'border-red-200 bg-red-50 text-destructive'
          : 'border-[#e8d3bf] bg-[#fbf3ea] text-[#6b4c3a]',
      )}
    >
      {error ??
        'Modo demostración: los ingresos de café, los tuestes y el stock se guardan en este navegador; todavía no se sincronizan con el backend.'}
    </p>
  )
}

function PurchaseForm({ onClose }: { onClose: () => void }) {
  const { recordPurchase } = useRoasteryDemo()
  const [origin, setOrigin] = useState('Colombia')
  const [region, setRegion] = useState('Huila')
  const [variety, setVariety] = useState('Caturra lavado')
  const [supplier, setSupplier] = useState('')
  const [amountKg, setAmountKg] = useState('')
  const [purchaseDate, setPurchaseDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const amount = Number.parseFloat(amountKg)
    if (!supplier.trim() || !Number.isFinite(amount) || amount <= 0 || !purchaseDate) {
      setError('Completá proveedor, fecha y una cantidad mayor que cero.')
      return
    }

    if (
      !recordPurchase({
        origin,
        region,
        variety,
        supplier,
        purchaseDate,
        amountGrams: Math.round(amount * 1000),
      })
    ) {
      setError('No se pudo registrar el ingreso. Revisá los datos e intentá nuevamente.')
      return
    }

    setError(null)
    setSaved(true)
    setSupplier('')
    setAmountKg('')
  }

  const fieldClass =
    'h-11 w-full rounded-lg border border-input bg-card px-3.5 text-sm text-foreground outline-none transition-colors focus-visible:border-primary focus-visible:ring-3 focus-visible:ring-primary/15'

  return (
    <DashboardCard
      title="Registrar ingreso de café verde"
      description="Registrá la recepción del café con su origen, proveedor, cantidad y fecha."
      action={
        <button
          type="button"
          onClick={onClose}
          className="rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground hover:bg-secondary hover:text-foreground"
        >
          Cerrar
        </button>
      }
    >
      <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-2">
          <label htmlFor="purchase-origin" className="text-sm font-medium">País de origen</label>
          <select id="purchase-origin" value={origin} onChange={(event) => setOrigin(event.target.value)} className={fieldClass}>
            <option>Colombia</option>
            <option>Brasil</option>
            <option>Etiopía</option>
            <option>Perú</option>
            <option>Guatemala</option>
          </select>
        </div>
        <div className="flex flex-col gap-2">
          <label htmlFor="purchase-region" className="text-sm font-medium">Región</label>
          <input id="purchase-region" value={region} onChange={(event) => setRegion(event.target.value)} required className={fieldClass} />
        </div>
        <div className="flex flex-col gap-2">
          <label htmlFor="purchase-variety" className="text-sm font-medium">Variedad / proceso</label>
          <input id="purchase-variety" value={variety} onChange={(event) => setVariety(event.target.value)} required className={fieldClass} />
        </div>
        <div className="flex flex-col gap-2">
          <label htmlFor="purchase-supplier" className="text-sm font-medium">Proveedor</label>
          <input id="purchase-supplier" value={supplier} onChange={(event) => setSupplier(event.target.value)} placeholder="Nombre del proveedor" required className={fieldClass} />
        </div>
        <div className="flex flex-col gap-2">
          <label htmlFor="purchase-amount" className="text-sm font-medium">Cantidad recibida</label>
          <div className="flex items-center rounded-lg border border-input bg-card focus-within:border-primary focus-within:ring-3 focus-within:ring-primary/15">
            <input id="purchase-amount" type="number" inputMode="decimal" min="0.001" step="any" value={amountKg} onChange={(event) => setAmountKg(event.target.value)} placeholder="0" required className="h-11 w-full bg-transparent px-3.5 text-sm tabular-nums outline-none" />
            <span className="pr-3 text-xs text-muted-foreground">kg</span>
          </div>
        </div>
        <div className="flex flex-col gap-2">
          <label htmlFor="purchase-date" className="text-sm font-medium">Fecha de ingreso</label>
          <input id="purchase-date" type="date" value={purchaseDate} onChange={(event) => setPurchaseDate(event.target.value)} required className={fieldClass} />
        </div>
        <div className="flex flex-col gap-2 sm:col-span-2">
          {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
          {saved && (
            <p role="status" className="flex items-center gap-2 text-sm text-success">
              <Check className="size-4" aria-hidden="true" />
              Ingreso agregado al inventario de demostración.
            </p>
          )}
          <button type="submit" className="inline-flex h-11 items-center justify-center gap-2 rounded-lg bg-[#6b4c3a] px-4 text-sm font-semibold text-white transition-colors hover:bg-[#5a3f30] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring">
            <PackagePlus className="size-4" aria-hidden="true" />
            Registrar ingreso
          </button>
        </div>
      </form>
    </DashboardCard>
  )
}

export function InventoryWorkspace() {
  const { greenLots, roastedLots, isReady, storageError, resetDemo } = useRoasteryDemo()
  const [tab, setTab] = useState<InventoryTab>('green')
  const [purchaseOpen, setPurchaseOpen] = useState(false)

  const totals = useMemo(() => {
    const greenGrams = greenLots.reduce((sum, lot) => sum + lot.stockGrams, 0)
    const roastedGrams = roastedLots.reduce((sum, lot) => sum + lot.stockGrams, 0)
    const roastedActive = roastedLots.filter((lot) => lot.stockGrams > 0).length
    const roastedExpiring = roastedLots.filter((lot) => lot.stockGrams > 0 && getDaysRemaining(lot.expirationDate) <= 7 && getDaysRemaining(lot.expirationDate) >= 0).length
    return { greenGrams, roastedGrams, roastedActive, roastedExpiring }
  }, [greenLots, roastedLots])

  return (
    <div className="mt-8 flex flex-col gap-6">
      <DemoNotice error={storageError} />

      <div className="grid gap-4 md:grid-cols-3">
        {[
          { label: 'Café verde disponible', value: `${kilogramsFormatter.format(totals.greenGrams / 1000)} kg`, detail: `${greenLots.length} lotes recibidos` },
          { label: 'Café tostado en stock', value: `${kilogramsFormatter.format(totals.roastedGrams / 1000)} kg`, detail: `${totals.roastedActive} lotes con stock` },
          { label: 'Lotes por vencer', value: `${totals.roastedExpiring}`, detail: 'En los próximos 7 días' },
        ].map((kpi) => (
          <div key={kpi.label} className="rounded-2xl border border-border bg-card p-5 shadow-[0_1px_2px_rgba(28,25,23,0.04),0_8px_24px_-12px_rgba(28,25,23,0.08)]">
            <p className="text-sm font-medium text-muted-foreground">{kpi.label}</p>
            <p className="mt-2 text-3xl font-bold tracking-tight tabular-nums text-foreground">{isReady ? kpi.value : '—'}</p>
            <p className="mt-1 text-xs text-muted-foreground">{kpi.detail}</p>
          </div>
        ))}
      </div>

      <div className="flex flex-wrap items-center justify-between gap-3">
        <div role="tablist" aria-label="Tipo de inventario" className="inline-flex rounded-xl border border-border bg-card p-1">
          {([
            ['green', 'Café verde'],
            ['roasted', 'Café tostado'],
          ] as const).map(([id, label]) => (
            <button
              key={id}
              type="button"
              role="tab"
              aria-selected={tab === id}
              onClick={() => setTab(id)}
              className={cn(
                'rounded-lg px-4 py-2 text-sm font-medium transition-colors',
                tab === id ? 'bg-[#6b4c3a] text-white shadow-sm' : 'text-muted-foreground hover:text-foreground',
              )}
            >
              {label}
            </button>
          ))}
        </div>
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            onClick={resetDemo}
            disabled={!isReady}
            className="inline-flex h-10 items-center gap-2 rounded-lg border border-input bg-card px-3.5 text-sm font-medium text-foreground transition-colors hover:bg-secondary disabled:opacity-50"
          >
            <RotateCcw className="size-4" aria-hidden="true" />
            Reiniciar demo
          </button>
          {tab === 'green' && (
            <button
              type="button"
              onClick={() => setPurchaseOpen((open) => !open)}
              className="inline-flex h-10 items-center gap-2 rounded-lg bg-[#6b4c3a] px-3.5 text-sm font-semibold text-white transition-colors hover:bg-[#5a3f30]"
            >
              <PackagePlus className="size-4" aria-hidden="true" />
              Registrar ingreso
            </button>
          )}
        </div>
      </div>

      {purchaseOpen && <PurchaseForm onClose={() => setPurchaseOpen(false)} />}

      {tab === 'green' ? (
        <DashboardCard
          title="Lotes de café verde"
          description="Recepciones de café por origen, proveedor y fecha. El stock se expresa en gramos."
        >
          <div className="-mx-6 overflow-x-auto">
            <table className="w-full min-w-[760px] text-left text-sm">
              <thead>
                <tr className="border-y border-border bg-muted/50 text-xs font-medium text-muted-foreground">
                  <th scope="col" className="px-6 py-3 font-medium">Lote / Origen</th>
                  <th scope="col" className="px-3 py-3 font-medium">Proveedor</th>
                  <th scope="col" className="px-3 py-3 font-medium">Ingreso</th>
                  <th scope="col" className="px-3 py-3 text-right font-medium">Stock</th>
                  <th scope="col" className="px-6 py-3 text-right font-medium">Mínimo / Estado</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {greenLots.map((lot) => {
                  const low = lot.stockGrams < lot.minimumGrams
                  return (
                    <tr key={lot.id} className="transition-colors hover:bg-muted/40">
                      <td className="px-6 py-4">
                        <p className="font-semibold text-foreground">{lot.id} · {lot.origin}</p>
                        <p className="mt-0.5 text-xs text-muted-foreground">{lot.region} · {lot.variety}</p>
                      </td>
                      <td className="px-3 py-4 text-foreground">{lot.supplier}</td>
                      <td className="px-3 py-4 whitespace-nowrap text-muted-foreground">{dateLabel(lot.purchaseDate)}</td>
                      <td className="px-3 py-4 text-right tabular-nums">
                        <p className={cn('font-semibold', low ? 'text-destructive' : 'text-foreground')}>
                          {kilogramsFormatter.format(lot.stockGrams / 1000)} kg
                        </p>
                        <p className="text-xs text-muted-foreground">{gramsFormatter.format(lot.stockGrams)} g</p>
                      </td>
                      <td className="px-6 py-4 text-right">
                        <p className="tabular-nums text-muted-foreground">{kilogramsFormatter.format(lot.minimumGrams / 1000)} kg</p>
                        <span className={cn('mt-1 inline-flex rounded-full px-2 py-0.5 text-[11px] font-semibold', low ? 'bg-red-50 text-destructive' : 'bg-emerald-50 text-emerald-700')}>
                          {lot.stockGrams <= 0 ? 'Agotado' : low ? 'Bajo' : 'Disponible'}
                        </span>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </DashboardCard>
      ) : (
        <DashboardCard
          title="Lotes de café tostado"
          description="Trazabilidad por lote, disponibilidad tras el reposo y vida útil desde la fecha de tueste."
        >
          <div className="-mx-6 overflow-x-auto">
            <table className="w-full min-w-[820px] text-left text-sm">
              <thead>
                <tr className="border-y border-border bg-muted/50 text-xs font-medium text-muted-foreground">
                  <th scope="col" className="px-6 py-3 font-medium">Lote / Origen</th>
                  <th scope="col" className="px-3 py-3 font-medium">Perfil</th>
                  <th scope="col" className="px-3 py-3 font-medium">F. tueste</th>
                  <th scope="col" className="px-3 py-3 text-right font-medium">Stock disponible</th>
                  <th scope="col" className="px-3 py-3 font-medium">Venta desde</th>
                  <th scope="col" className="px-6 py-3 text-right font-medium">Frescura</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {roastedLots.map((lot) => {
                  const daysRemaining = getDaysRemaining(lot.expirationDate)
                  const freshness = remainingLabel(daysRemaining)
                  const waiting = new Date(`${lot.availableDate}T00:00:00`).getTime() > new Date().setHours(0, 0, 0, 0)
                  return (
                    <tr key={lot.id} className="transition-colors hover:bg-muted/40">
                      <td className="px-6 py-4">
                        <p className="font-semibold text-foreground">{lot.id}</p>
                        <p className="mt-0.5 text-xs text-muted-foreground">{lot.origin}</p>
                      </td>
                      <td className="px-3 py-4 text-foreground">{lot.profile}</td>
                      <td className="px-3 py-4 whitespace-nowrap text-muted-foreground">{dateLabel(lot.roastDate)}</td>
                      <td className="px-3 py-4 text-right tabular-nums">
                        <p className="font-semibold text-foreground">{kilogramsFormatter.format(lot.stockGrams / 1000)} kg</p>
                        <p className="text-xs text-muted-foreground">{gramsFormatter.format(lot.stockGrams)} g</p>
                      </td>
                      <td className="px-3 py-4 whitespace-nowrap">
                        {waiting ? (
                          <span className="rounded-full bg-amber-50 px-2.5 py-1 text-xs font-medium text-amber-700">En reposo · {dateLabel(lot.availableDate)}</span>
                        ) : (
                          <span className="text-sm text-success">Disponible · {dateLabel(lot.availableDate)}</span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-right">
                        <span className={cn('inline-flex rounded-full px-2.5 py-1 text-xs font-semibold', freshness.className)}>{freshness.text}</span>
                        <p className="mt-1 text-xs text-muted-foreground">Vence {dateLabel(lot.expirationDate)}</p>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </DashboardCard>
      )}
    </div>
  )
}
