import type { Metadata } from 'next'
import { PageHeading } from '@/components/dashboard/page-heading'
import { HojasRutaWorkspace } from '@/components/dashboard/hojas-ruta-workspace'

export const metadata: Metadata = { title: 'Hojas de ruta | Café de Finca' }

export default function HojasRutaPage() {
  return <><PageHeading eyebrow="VENTAS / REPARTO" title="Hojas de ruta" description="Paradas y pedidos de entrega ordenados por fecha."/><HojasRutaWorkspace /></>
}
