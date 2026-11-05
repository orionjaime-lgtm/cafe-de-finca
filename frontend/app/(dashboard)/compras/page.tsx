import type { Metadata } from 'next'
import { MisComprasWorkspace } from '@/components/dashboard/cliente/mis-compras-workspace'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Mis Compras | Café de Finca',
  description: 'Historial de cafés y pedidos comprados por el cliente.',
}

export default function ComprasPage() {
  return (
    <>
      <PageHeading eyebrow="CLIENTE / MIS COMPRAS" title="Mis Compras" description="Revisá los cafés y pedidos que ya compraste." />
      <MisComprasWorkspace />
    </>
  )
}
