'use client'

import { useEffect, useState } from 'react'
import { MapPin, Navigation } from 'lucide-react'
import { DashboardCard } from './dashboard-card'
import { apiGet } from '@/lib/api'

type Parada = { ordenVisita: number; pedidoId: number; clienteId: number; cliente: string; domicilio: string; localidad: string; montoTotal: number; estadoPreparacion: string }
type HojaRuta = { idHojaRuta: number; fechaReparto: string; estadoRuta: string; paradas: Parada[] }

export function HojasRutaWorkspace() {
  const [hojas, setHojas] = useState<HojaRuta[]>([])
  const [fecha, setFecha] = useState('')
  const [mensaje, setMensaje] = useState<string | null>(null)
  async function cargar(fechaFiltro = '') {
    try { setHojas(await apiGet<HojaRuta[]>(fechaFiltro ? `/api/hojas-ruta?fecha=${fechaFiltro}` : '/api/hojas-ruta')) } catch (error) { setMensaje(error instanceof Error ? error.message : 'No se pudieron cargar las hojas de ruta.') }
  }
  useEffect(() => { cargar() }, [])
  function currency(value: number) { return new Intl.NumberFormat('es-AR', { style: 'currency', currency: 'ARS', maximumFractionDigits: 0 }).format(value) }
  return <div className="mt-8 flex flex-col gap-6">
    {mensaje && <p role="alert" className="rounded-lg border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">{mensaje}</p>}
    <DashboardCard title="Buscar reparto" description="Consulta hojas de ruta existentes en PostgreSQL."><div className="flex flex-wrap gap-3"><input type="date" value={fecha} onChange={(e) => setFecha(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm"/><button type="button" onClick={() => cargar(fecha)} className="h-11 rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">Buscar</button><button type="button" onClick={() => { setFecha(''); cargar() }} className="h-11 rounded-lg border border-border px-4 text-sm font-semibold">Ver todas</button></div></DashboardCard>
    {hojas.map((hoja) => <DashboardCard key={hoja.idHojaRuta} title={`Ruta ${hoja.fechaReparto}`} description={`${hoja.paradas.length} paradas · Estado: ${hoja.estadoRuta}`} action={<span className="flex size-9 items-center justify-center rounded-lg bg-secondary text-primary"><Navigation className="size-4" /></span>}><div className="divide-y">{hoja.paradas.map((p) => <div key={p.pedidoId} className="flex items-start gap-4 py-4"><span className="flex size-8 shrink-0 items-center justify-center rounded-full bg-primary text-xs font-bold text-primary-foreground">{p.ordenVisita}</span><div className="min-w-0 flex-1"><p className="font-medium">{p.cliente}</p><p className="mt-1 flex items-center gap-1 text-xs text-muted-foreground"><MapPin className="size-3" />{p.domicilio}, {p.localidad}</p></div><div className="text-right"><p className="text-sm font-semibold">{currency(p.montoTotal)}</p><p className="mt-1 text-xs text-muted-foreground">Pedido #{p.pedidoId} · {p.estadoPreparacion}</p></div></div>)}</div></DashboardCard>)}
    {hojas.length === 0 && <DashboardCard title="Sin hojas de ruta" description="No hay repartos para el criterio seleccionado."><p className="py-6 text-sm text-muted-foreground">Probá con otra fecha o limpiá el filtro.</p></DashboardCard>}
  </div>
}
