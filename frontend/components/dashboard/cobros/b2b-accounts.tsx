'use client'

import { useState } from 'react'
import { Building2 } from 'lucide-react'
import { DashboardCard } from '../dashboard-card'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

const money = new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS' })

export function B2bAccounts() {
  const { b2bAccounts, paymentMethods, recordPayment, isReady } = useRoasteryDemo()
  const [pedidoId, setPedidoId] = useState<number | null>(null)
  const [medioPagoId, setMedioPagoId] = useState('')
  const [monto, setMonto] = useState('')
  const [message, setMessage] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const selected = b2bAccounts.find((account) => account.pedidoId === pedidoId)

  async function pay() {
    if (!pedidoId || !medioPagoId || Number(monto) <= 0) return
    setSaving(true); setMessage(null)
    try {
      await recordPayment({ pedidoId, medioPagoId: Number(medioPagoId), monto: Number(monto) })
      setMessage('Pago registrado correctamente.')
      setPedidoId(null); setMonto('')
    } catch (error) { setMessage(error instanceof Error ? error.message : 'No se pudo registrar el pago.') }
    finally { setSaving(false) }
  }

  return <DashboardCard title="Cuentas Corrientes B2B (Mayoristas)" description="Saldos pendientes de clientes corporativos con facturación a crédito."><div className="-mx-6 overflow-x-auto"><table className="w-full min-w-[640px] text-left text-sm"><thead><tr className="border-y border-border bg-muted/40 text-xs font-medium text-muted-foreground"><th className="px-6 py-3">Cliente</th><th className="px-3 py-3">Factura</th><th className="px-3 py-3 text-right">Deuda</th><th className="px-3 py-3">Estado</th><th className="px-6 py-3 text-right">Acción</th></tr></thead><tbody>{b2bAccounts.map((account) => <tr key={account.pedidoId} className="border-b border-border last:border-b-0"><td className="px-6 py-4"><div className="flex items-center gap-3"><span className="flex size-9 items-center justify-center rounded-lg bg-secondary"><Building2 className="size-4 text-primary" /></span><span className="font-medium">{account.cliente}</span></div></td><td className="px-3 py-4 text-muted-foreground">{account.factura ? `${account.factura.tipoComprobante} - ${account.factura.numeroComprobante}` : 'Sin factura'}</td><td className="px-3 py-4 text-right font-semibold tabular-nums">{money.format(account.deuda)}</td><td className="px-3 py-4"><span className="rounded-md bg-muted px-2 py-1 text-xs font-semibold">{account.estado}</span></td><td className="px-6 py-4 text-right"><button type="button" onClick={() => { setPedidoId(account.pedidoId); setMonto(String(account.deuda)) }} className="rounded-lg border border-primary px-3 py-1.5 text-xs font-semibold text-primary hover:bg-primary hover:text-primary-foreground">Registrar Pago<span className="sr-only"> de {account.cliente}</span></button></td></tr>)}</tbody></table></div>
    {b2bAccounts.length === 0 && <p className="py-4 text-sm text-muted-foreground">No hay cuentas corrientes pendientes.</p>}
    {selected && <div className="mt-4 grid gap-3 rounded-xl border border-border bg-muted/20 p-4 sm:grid-cols-3"><div><p className="text-sm font-medium">{selected.cliente}</p><p className="text-xs text-muted-foreground">Saldo: {money.format(selected.deuda)}</p></div><select className="h-11 rounded-lg border border-input bg-card px-3 text-sm" value={medioPagoId} onChange={(event) => setMedioPagoId(event.target.value)}><option value="">Medio de pago</option>{paymentMethods.map((method) => <option key={method.idMedioPago} value={method.idMedioPago}>{method.nombreMedio}</option>)}</select><div className="flex gap-2"><input className="h-11 min-w-0 flex-1 rounded-lg border border-input bg-card px-3 text-sm" type="number" min="0.01" step="0.01" value={monto} onChange={(event) => setMonto(event.target.value)} /><button type="button" disabled={saving || !isReady} onClick={pay} className="h-11 rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">{saving ? '...' : 'Pagar'}</button></div></div>}
    {message && <p className="mt-3 text-sm" role="status">{message}</p>}
    <div className="mt-auto flex items-center justify-between border-t border-border pt-4 text-sm"><span className="text-muted-foreground">Saldo total pendiente</span><span className="font-semibold tabular-nums">{money.format(b2bAccounts.reduce((sum, account) => sum + account.deuda, 0))}</span></div></DashboardCard>
}
