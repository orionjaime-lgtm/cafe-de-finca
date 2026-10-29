'use client'

import { useMemo, useState } from 'react'
import { Check, PackagePlus } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

const grams = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const dateFormatter = new Intl.DateTimeFormat('es-AR', { day: '2-digit', month: 'short', year: 'numeric', timeZone: 'UTC' })
const fieldClass = 'h-11 w-full rounded-lg border border-input bg-card px-3.5 text-sm outline-none focus-visible:border-primary focus-visible:ring-3 focus-visible:ring-primary/15'

export function InventoryWorkspace() {
  const { greenLots, roastedLots, origins, suppliers, isReady, storageError, recordPurchase, refresh } = useRoasteryDemo()
  const [tab, setTab] = useState<'green' | 'roasted'>('green')
  const [open, setOpen] = useState(false)
  const [originId, setOriginId] = useState('')
  const [supplierId, setSupplierId] = useState('')
  const [code, setCode] = useState('')
  const [bags, setBags] = useState('1')
  const [amount, setAmount] = useState('')
  const [unit, setUnit] = useState<'kg' | 'lb' | 'g'>('kg')
  const [date, setDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [error, setError] = useState<string | null>(null)
  const [saved, setSaved] = useState(false)

  const totals = useMemo(() => ({
    green: greenLots.reduce((sum, lot) => sum + lot.stockGrams, 0),
    roasted: roastedLots.reduce((sum, lot) => sum + lot.stockGrams, 0),
  }), [greenLots, roastedLots])

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null); setSaved(false)
    const quantity = Number(amount)
    if (!originId || !supplierId || !code.trim() || !Number.isFinite(quantity) || quantity <= 0 || Number(bags) <= 0) {
      setError('Completá origen, proveedor, código, bolsas y una cantidad válida.')
      return
    }
    try {
      await recordPurchase({ idOrigen: Number(originId), idProveedor: Number(supplierId), codigoLote: code.trim(), cantidad: quantity, unidad: unit, cantBolsas: Number(bags), fechaIngreso: date || undefined })
      setSaved(true); setCode(''); setAmount(''); setBags('1')
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'No se pudo registrar el ingreso.') }
  }

  return <div className="mt-8 flex flex-col gap-6">
    {storageError && <p role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-destructive">{storageError}</p>}
    <div className="grid gap-4 sm:grid-cols-3">
      {[{ label: 'Café verde', value: `${grams.format(totals.green / 1000)} kg` }, { label: 'Café tostado', value: `${grams.format(totals.roasted / 1000)} kg` }, { label: 'Lotes verdes', value: String(greenLots.length) }].map((item) => <article key={item.label} className="rounded-2xl border border-border bg-card p-5 shadow-[0_1px_2px_rgba(28,25,23,0.04),0_8px_24px_-12px_rgba(28,25,23,0.08)]"><p className="text-sm text-muted-foreground">{item.label}</p><p className="mt-2 text-2xl font-semibold tabular-nums text-foreground">{item.value}</p></article>)}
      <div className="sm:col-start-3"><button type="button" onClick={() => setOpen((value) => !value)} className="inline-flex h-11 w-full items-center justify-center gap-2 rounded-lg bg-[#6b4c3a] text-sm font-semibold text-white hover:bg-[#5a3f30]"><PackagePlus className="size-4" />Registrar café verde</button></div>
    </div>

    {open && <DashboardCard title="Registrar ingreso de café verde" description="El stock y las unidades se mantienen sincronizados con el backend.">
      <form onSubmit={submit} className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-2"><label className="text-sm font-medium">Origen</label><select className={fieldClass} value={originId} onChange={(event) => setOriginId(event.target.value)}><option value="">Seleccionar origen</option>{origins.map((origin) => <option key={origin.idOrigen} value={origin.idOrigen}>{origin.pais} {origin.region} · {origin.variedad}</option>)}</select></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium">Proveedor</label><select className={fieldClass} value={supplierId} onChange={(event) => setSupplierId(event.target.value)}><option value="">Seleccionar proveedor</option>{suppliers.map((supplier) => <option key={supplier.idProveedor} value={supplier.idProveedor}>{supplier.razonSocial}</option>)}</select></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium">Código de lote</label><input className={fieldClass} value={code} onChange={(event) => setCode(event.target.value)} placeholder="LV-HUI-002" /></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium">Cantidad de bolsas</label><input className={fieldClass} type="number" min="1" value={bags} onChange={(event) => setBags(event.target.value)} /></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium">Cantidad recibida</label><div className="flex items-center rounded-lg border border-input"><input className="h-11 flex-1 bg-transparent px-3.5 text-sm outline-none" type="number" min="0.001" step="any" value={amount} onChange={(event) => setAmount(event.target.value)} /><select value={unit} onChange={(event) => setUnit(event.target.value as typeof unit)} className="h-8 border-l border-input bg-transparent px-2 text-sm"><option>kg</option><option>g</option><option>lb</option></select></div></div>
        <div className="flex flex-col gap-2"><label className="text-sm font-medium">Fecha de ingreso</label><input className={fieldClass} type="date" value={date} onChange={(event) => setDate(event.target.value)} /></div>
        <div className="sm:col-span-2 flex flex-col gap-3">{error && <p role="alert" className="text-sm text-destructive">{error}</p>}{saved && <p role="status" className="flex items-center gap-2 text-sm text-success"><Check className="size-4" />Lote verde registrado.</p>}<button type="submit" disabled={!isReady} className="h-11 rounded-lg bg-[#6b4c3a] text-sm font-semibold text-white disabled:opacity-50">Registrar ingreso</button></div>
      </form>
    </DashboardCard>}

    <DashboardCard title="Lotes en inventario" description="Datos reales provenientes del backend" action={<div className="flex rounded-lg bg-secondary p-1"><button type="button" onClick={() => setTab('green')} className={`rounded-md px-3 py-1.5 text-xs font-medium ${tab === 'green' ? 'bg-card shadow-sm' : 'text-muted-foreground'}`}>Verde</button><button type="button" onClick={() => setTab('roasted')} className={`rounded-md px-3 py-1.5 text-xs font-medium ${tab === 'roasted' ? 'bg-card shadow-sm' : 'text-muted-foreground'}`}>Tostado</button></div>}>
      <div className="overflow-x-auto"><table className="w-full text-sm"><thead><tr className="border-y bg-muted/50 text-left text-xs text-muted-foreground"><th className="px-5 py-3">Lote</th><th className="px-5 py-3">Origen</th><th className="px-5 py-3">Detalle</th><th className="px-5 py-3 text-right">Stock</th></tr></thead><tbody className="divide-y">{tab === 'green' ? greenLots.map((lot) => <tr key={lot.id}><td className="px-5 py-4 font-medium">{lot.code}</td><td className="px-5 py-4">{lot.origin}</td><td className="px-5 py-4 text-muted-foreground">{lot.supplier}<br />{lot.purchaseDate ? dateFormatter.format(new Date(`${lot.purchaseDate}T00:00:00Z`)) : ''}</td><td className="px-5 py-4 text-right font-semibold tabular-nums">{grams.format(lot.stockGrams)} g</td></tr>) : roastedLots.map((lot) => <tr key={lot.id}><td className="px-5 py-4 font-medium">{lot.code}</td><td className="px-5 py-4">{lot.origin}</td><td className="px-5 py-4 text-muted-foreground">{lot.profile}<br />Caduca: {lot.expirationDate ? dateFormatter.format(new Date(`${lot.expirationDate}T00:00:00Z`)) : '-'}</td><td className="px-5 py-4 text-right font-semibold tabular-nums">{grams.format(lot.stockGrams)} g</td></tr>)}</tbody></table>{!isReady && <p className="p-5 text-sm text-muted-foreground">Cargando inventario…</p>}</div>
    </DashboardCard>
  </div>
}
