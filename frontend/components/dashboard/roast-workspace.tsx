'use client'

import { useMemo, useState } from 'react'
import { Check, Flame } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

const grams = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 })
const fieldClass = 'h-11 w-full rounded-lg border border-input bg-card px-3.5 text-sm outline-none focus-visible:border-primary focus-visible:ring-3 focus-visible:ring-primary/15'

export function RoastWorkspace() {
  const { greenLots, roastOrders, roastProfiles, isReady, storageError, recordRoast } = useRoasteryDemo()
  const [lotId, setLotId] = useState('')
  const [profileId, setProfileId] = useState('')
  const [greenAmount, setGreenAmount] = useState('5')
  const [roastedAmount, setRoastedAmount] = useState('4.3')
  const [unit, setUnit] = useState<'kg' | 'lb' | 'g'>('kg')
  const [operator, setOperator] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)
  const availableLots = useMemo(() => greenLots.filter((lot) => lot.stockGrams > 0), [greenLots])
  const selectedLot = availableLots.find((lot) => lot.numericId === Number(lotId)) ?? availableLots[0]
  const selectedProfile = roastProfiles.find((profile) => profile.idPerfilTueste === Number(profileId)) ?? roastProfiles[0]

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(null); setSuccess(null)
    if (!selectedLot || !selectedProfile) { setError('Seleccioná un lote verde y un perfil de tueste.') ; return }
    const verde = Number(greenAmount), tostado = Number(roastedAmount)
    if (!Number.isFinite(verde) || verde <= 0 || !Number.isFinite(tostado) || tostado <= 0) { setError('Las cantidades deben ser mayores que cero.') ; return }
    try {
      await recordRoast({ idLoteVerde: selectedLot.numericId, idPerfilTueste: selectedProfile.idPerfilTueste, pesoVerdeUsado: verde, pesoTostadoObtenido: tostado, unidad: unit, operadorTostador: operator.trim() || undefined })
      setSuccess('Orden de tueste registrada y stock actualizado.')
    } catch (caught) { setError(caught instanceof Error ? caught.message : 'No se pudo registrar el tueste.') }
  }

  return <div className="mt-8 flex flex-col gap-6">
    {storageError && <p role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-destructive">{storageError}</p>}
    <div className="grid gap-6 xl:grid-cols-[minmax(0,1.35fr)_minmax(300px,0.65fr)]">
      <DashboardCard title="Registrar orden de tueste" description="El backend descuenta el verde y crea el lote tostado con reposo y caducidad." action={<span className="inline-flex size-9 items-center justify-center rounded-lg bg-[#f3e8dd] text-[#6b4c3a]"><Flame className="size-4.5" /></span>}>
        <form onSubmit={submit} className="flex flex-col gap-5">
          <div className="flex flex-col gap-2"><label className="text-sm font-medium">Lote verde disponible</label><select className={fieldClass} value={selectedLot?.numericId ?? ''} onChange={(event) => setLotId(event.target.value)} disabled={!availableLots.length}><option value="">Seleccionar lote</option>{availableLots.map((lot) => <option key={lot.id} value={lot.numericId}>{lot.code} · {lot.origin} · {grams.format(lot.stockGrams)} g</option>)}</select></div>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="flex flex-col gap-2"><label className="text-sm font-medium">Perfil de tueste</label><select className={fieldClass} value={selectedProfile?.idPerfilTueste ?? ''} onChange={(event) => setProfileId(event.target.value)}><option value="">Seleccionar perfil</option>{roastProfiles.map((profile) => <option key={profile.idPerfilTueste} value={profile.idPerfilTueste}>{profile.nombrePerfil}</option>)}</select></div>
            <div className="flex flex-col gap-2"><label className="text-sm font-medium">Operador / responsable</label><input className={fieldClass} value={operator} onChange={(event) => setOperator(event.target.value)} placeholder="Nombre del operador" /></div>
            <div className="flex flex-col gap-2"><label className="text-sm font-medium">Café verde usado</label><input className={fieldClass} type="number" min="0.001" step="any" value={greenAmount} onChange={(event) => setGreenAmount(event.target.value)} /></div>
            <div className="flex flex-col gap-2"><label className="text-sm font-medium">Café tostado obtenido</label><input className={fieldClass} type="number" min="0.001" step="any" value={roastedAmount} onChange={(event) => setRoastedAmount(event.target.value)} /></div>
            <div className="sm:col-span-2 flex flex-col gap-2"><label className="text-sm font-medium">Unidad</label><div className="flex rounded-lg border border-input bg-secondary p-1">{(['kg', 'lb', 'g'] as const).map((option) => <button key={option} type="button" onClick={() => setUnit(option)} className={`flex-1 rounded-md py-2 text-sm font-medium ${unit === option ? 'bg-card shadow-sm' : 'text-muted-foreground'}`}>{option}</button>)}</div></div>
          </div>
          {selectedProfile && <p className="rounded-lg bg-[#fbf3ea] px-4 py-3 text-sm text-[#6b4c3a]">Perfil {selectedProfile.nombrePerfil}: reposo mínimo de {selectedProfile.horasReposoMinimas} horas, merma esperada {grams.format(selectedProfile.mermaEsperadaPct)}%.</p>}
          {error && <p role="alert" className="text-sm text-destructive">{error}</p>}{success && <p role="status" className="flex items-center gap-2 text-sm text-success"><Check className="size-4" />{success}</p>}
          <button type="submit" disabled={!isReady || !availableLots.length} className="h-11 rounded-lg bg-[#6b4c3a] text-sm font-semibold text-white disabled:opacity-50">Registrar tueste</button>
        </form>
      </DashboardCard>
      <DashboardCard title="Última referencia" description="Datos reales de perfiles y lotes"><div className="flex flex-col gap-4 text-sm">{selectedLot && <div><p className="text-xs text-muted-foreground">Stock verde</p><p className="mt-1 font-semibold">{grams.format(selectedLot.stockGrams)} g</p></div>}{selectedProfile && <div><p className="text-xs text-muted-foreground">Temperatura objetivo</p><p className="mt-1 font-semibold">{selectedProfile.tempObjetivoC} °C</p></div>}</div></DashboardCard>
    </div>
    <DashboardCard title="Órdenes de tueste" description="Historial generado por el backend"><div className="overflow-x-auto"><table className="w-full text-sm"><thead><tr className="border-y bg-muted/50 text-left text-xs text-muted-foreground"><th className="px-5 py-3">Orden / lote</th><th className="px-5 py-3">Origen y perfil</th><th className="px-5 py-3 text-right">Peso verde</th><th className="px-5 py-3 text-right">Peso tostado</th><th className="px-5 py-3 text-right">Merma</th></tr></thead><tbody className="divide-y">{roastOrders.map((order) => <tr key={order.id}><td className="px-5 py-4 font-medium">{order.id}</td><td className="px-5 py-4">{order.origin}<br /><span className="text-xs text-muted-foreground">{order.profile}</span></td><td className="px-5 py-4 text-right tabular-nums">{grams.format(order.greenInputGrams)} g</td><td className="px-5 py-4 text-right tabular-nums">{grams.format(order.roastedOutputGrams)} g</td><td className="px-5 py-4 text-right tabular-nums">{order.lossPercent}%</td></tr>)}</tbody></table>{roastOrders.length === 0 && <p className="p-5 text-sm text-muted-foreground">No hay órdenes registradas.</p>}</div></DashboardCard>
  </div>
}
