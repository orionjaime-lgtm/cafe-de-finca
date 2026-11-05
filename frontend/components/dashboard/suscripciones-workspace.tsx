'use client'

import { useEffect, useState } from 'react'
import { CalendarClock, Pause, Play, Trash2 } from 'lucide-react'
import { DashboardCard } from './dashboard-card'
import { apiDelete, apiGet, apiPatch, apiPost } from '@/lib/api'

export type Suscripcion = { idSuscripcion: number; clienteId: number; cliente: string; frecuencia: string; diaEntrega: number; fechaInicio: string; fechaFin: string | null; proximaFechaGeneracion: string; estado: string; items: Array<{ presentacionId: number; presentacion: string; tipoMoliendaId: number; tipoMolienda: string; cantidadEnvases: number; precioUnitario: number }> }
type Cliente = { idCliente: number; nombreRazonSocial: string; tipoCliente: string }
type Presentacion = { idPresentacion: number; nombreEnvase: string; pesoGramos: number }
type Molienda = { idTipoMolienda: number; nombreMolienda: string }
type Plan = { idPlan: number; nombre: string; porcentajeDescuento: number; descripcion: string }
type Membresia = { idMembresia: number; clienteId: number; cliente: string; planId: number; nombrePlan: string; porcentajeDescuento: number; fechaInicio: string; fechaFin: string|null; estado: string }

export function SuscripcionesWorkspace() {
  const [suscripciones, setSuscripciones] = useState<Suscripcion[]>([])
  const [clientes, setClientes] = useState<Cliente[]>([])
  const [presentaciones, setPresentaciones] = useState<Presentacion[]>([])
  const [moliendas, setMoliendas] = useState<Molienda[]>([])
  const [planes, setPlanes] = useState<Plan[]>([])
  const [membresias, setMembresias] = useState<Membresia[]>([])
  const [clienteMembresiaId, setClienteMembresiaId] = useState('')
  const [planId, setPlanId] = useState('')
  const [clienteId, setClienteId] = useState('')
  const [frecuencia, setFrecuencia] = useState('SEMANAL')
  const [diaEntrega, setDiaEntrega] = useState('3')
  const [presentacionId, setPresentacionId] = useState('')
  const [moliendaId, setMoliendaId] = useState('')
  const [cantidad, setCantidad] = useState(1)
  const [precio, setPrecio] = useState(0)
  const [mensaje, setMensaje] = useState<string | null>(null)

  async function cargar() {
    const [lista, clientesApi, presentacionesApi, moliendasApi, planesApi, membresiasApi] = await Promise.all([
      apiGet<Suscripcion[]>('/api/suscripciones'), apiGet<Cliente[]>('/api/clientes?tipo=B2B'), apiGet<Presentacion[]>('/api/presentaciones'), apiGet<Molienda[]>('/api/tipos-molienda'), apiGet<Plan[]>('/api/planes-suscripcion'), apiGet<Membresia[]>('/api/membresias')])
    setSuscripciones(lista); setClientes(clientesApi); setPresentaciones(presentacionesApi); setMoliendas(moliendasApi); setPlanes(planesApi); setMembresias(membresiasApi)
  }
  useEffect(() => { cargar().catch((error) => setMensaje(error instanceof Error ? error.message : 'No se pudieron cargar los abonos.')) }, [])

  async function crear() {
    if (!clienteId || !presentacionId || !moliendaId || cantidad < 1) return
    try {
      await apiPost('/api/suscripciones', { clienteId: Number(clienteId), frecuencia, diaEntrega: Number(diaEntrega), items: [{ presentacionId: Number(presentacionId), tipoMoliendaId: Number(moliendaId), cantidadEnvases: cantidad, precioUnitario: precio }] })
      setMensaje('Abono recurrente creado correctamente.')
      await cargar()
    } catch (error) { setMensaje(error instanceof Error ? error.message : 'No se pudo crear el abono.') }
  }
  async function cambiar(id: number, estado: string) {
    try { await apiPatch<Suscripcion>(`/api/suscripciones/${id}`, { estado }); await cargar() } catch (error) { setMensaje(error instanceof Error ? error.message : 'No se pudo cambiar el abono.') }
  }
  async function generar() {
    try { await apiPost('/api/suscripciones/generar', {}); setMensaje('Pedidos recurrentes generados correctamente.'); await cargar() } catch (error) { setMensaje(error instanceof Error ? error.message : 'No se pudo generar pedidos.') }
  }

  async function asignarMembresia() {
    if (!clienteMembresiaId || !planId) return
    try { await apiPost<Membresia>('/api/membresias', { clienteId: Number(clienteMembresiaId), planId: Number(planId) }); setMensaje('Membresía creada correctamente.'); await cargar() } catch (error) { setMensaje(error instanceof Error ? error.message : 'No se pudo crear la membresía.') }
  }
  async function cancelarMembresia(id: number) {
    try { await apiDelete<Membresia>(`/api/membresias/${id}`); setMensaje('Membresía cancelada correctamente.'); await cargar() } catch (error) { setMensaje(error instanceof Error ? error.message : 'No se pudo cancelar la membresía.') }
  }

  return <div className="mt-8 flex flex-col gap-6">
    {mensaje && <p role="status" className="rounded-lg border border-border bg-card px-4 py-3 text-sm">{mensaje}</p>}
    <DashboardCard title="Nuevo abono recurrente" description="Crea una entrega periódica para un cliente mayorista."><div className="grid gap-4 md:grid-cols-3 lg:grid-cols-6"><select value={clienteId} onChange={(e) => setClienteId(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm"><option value="">Cliente B2B</option>{clientes.map((c) => <option key={c.idCliente} value={c.idCliente}>{c.nombreRazonSocial}</option>)}</select><select value={frecuencia} onChange={(e) => setFrecuencia(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm"><option>SEMANAL</option><option>MENSUAL</option></select><input type="number" min={frecuencia === 'SEMANAL' ? 1 : 1} max={frecuencia === 'SEMANAL' ? 7 : 28} value={diaEntrega} onChange={(e) => setDiaEntrega(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm" placeholder="Día"/><select value={presentacionId} onChange={(e) => setPresentacionId(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm"><option value="">Presentación</option>{presentaciones.map((p) => <option key={p.idPresentacion} value={p.idPresentacion}>{p.nombreEnvase}</option>)}</select><select value={moliendaId} onChange={(e) => setMoliendaId(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm"><option value="">Molienda</option>{moliendas.map((m) => <option key={m.idTipoMolienda} value={m.idTipoMolienda}>{m.nombreMolienda}</option>)}</select><input type="number" min="1" value={cantidad} onChange={(e) => setCantidad(Number(e.target.value))} className="h-11 rounded-lg border border-input bg-card px-3 text-sm" placeholder="Envases"/></div><div className="mt-4 flex flex-wrap items-end gap-3"><label className="text-sm"><span className="mb-1 block text-xs text-muted-foreground">Precio pactado por envase</span><input type="number" min="0" step="0.01" value={precio} onChange={(e) => setPrecio(Number(e.target.value))} className="h-11 w-44 rounded-lg border border-input bg-card px-3 text-sm" /></label><button type="button" onClick={crear} className="h-11 rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">Crear abono</button><button type="button" onClick={generar} className="h-11 rounded-lg border border-border px-4 text-sm font-semibold"><CalendarClock className="mr-2 inline size-4" />Generar pedidos</button></div></DashboardCard>
    <DashboardCard title="Planes de membresía" description="Planes disponibles para descuentos automáticos en pedidos."><div className="grid gap-3 md:grid-cols-3">{planes.map((p) => <div key={p.idPlan} className="rounded-xl border border-border p-4"><p className="text-sm font-semibold">{p.nombre}</p><p className="mt-1 text-2xl font-bold text-primary">{p.porcentajeDescuento}%</p><p className="mt-2 text-xs text-muted-foreground">{p.descripcion}</p></div>)}</div></DashboardCard>
    <DashboardCard title="Membresías de clientes" description="Alta y cancelación mediante la API, sin datos locales."><div className="mb-4 grid gap-3 md:grid-cols-[1fr_1fr_auto]"><select value={clienteMembresiaId} onChange={(e)=>setClienteMembresiaId(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm"><option value="">Cliente</option>{clientes.map((c)=><option key={c.idCliente} value={c.idCliente}>{c.nombreRazonSocial}</option>)}</select><select value={planId} onChange={(e)=>setPlanId(e.target.value)} className="h-11 rounded-lg border border-input bg-card px-3 text-sm"><option value="">Plan</option>{planes.map((p)=><option key={p.idPlan} value={p.idPlan}>{p.nombre}</option>)}</select><button type="button" onClick={asignarMembresia} className="h-11 rounded-lg bg-primary px-4 text-sm font-semibold text-primary-foreground">Asignar plan</button></div><div className="divide-y">{membresias.map((m)=><div key={m.idMembresia} className="flex items-center justify-between py-3"><div><p className="text-sm font-medium">{m.cliente}</p><p className="mt-1 text-xs text-muted-foreground">{m.nombrePlan} · {m.porcentajeDescuento}% · {m.estado}</p></div>{m.estado==='ACTIVA'&&<button type="button" onClick={()=>cancelarMembresia(m.idMembresia)} className="h-9 rounded-lg border border-destructive/30 px-3 text-xs font-semibold text-destructive">Cancelar</button>}</div>)}</div></DashboardCard>
    <DashboardCard title="Abonos recurrentes" description="Frecuencia, próxima entrega y estado real desde PostgreSQL."><div className="divide-y">{suscripciones.map((s) => <div key={s.idSuscripcion} className="flex flex-col gap-3 py-4 md:flex-row md:items-center md:justify-between"><div><p className="font-medium">{s.cliente}</p><p className="mt-1 text-xs text-muted-foreground">{s.frecuencia} · Día {s.diaEntrega} · Próxima: {s.proximaFechaGeneracion}</p><p className="mt-1 text-xs text-muted-foreground">{s.items.map((i) => `${i.cantidadEnvases} × ${i.presentacion}`).join(' · ')}</p></div><div className="flex items-center gap-2"><span className="rounded-full bg-secondary px-2.5 py-1 text-xs font-semibold">{s.estado}</span>{s.estado !== 'ACTIVA' && <button type="button" onClick={() => cambiar(s.idSuscripcion, 'ACTIVA')} aria-label="Activar" className="flex size-9 items-center justify-center rounded-lg border"><Play className="size-4" /></button>}{s.estado !== 'PAUSADA' && <button type="button" onClick={() => cambiar(s.idSuscripcion, 'PAUSADA')} aria-label="Pausar" className="flex size-9 items-center justify-center rounded-lg border"><Pause className="size-4" /></button>}<button type="button" onClick={() => cambiar(s.idSuscripcion, 'CANCELADA')} aria-label="Cancelar" className="flex size-9 items-center justify-center rounded-lg border text-destructive"><Trash2 className="size-4" /></button></div></div>)}</div></DashboardCard>
  </div>
}
