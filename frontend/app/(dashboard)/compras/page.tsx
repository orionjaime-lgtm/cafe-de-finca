import type { Metadata } from 'next'
import { ComprasWorkspace } from '@/components/dashboard/cliente/compras-workspace'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Mis Compras | Café de Finca',
  description: 'Compra de café y selección de forma de pago para clientes.',
}

export default function ComprasPage() {
  return (
    <>
      <PageHeading eyebrow="CLIENTE / MIS COMPRAS" title="Mis Compras" description="Elegí café disponible, configurá tu pedido y completá el pago." />
      <ComprasWorkspace />
    </>
  )
}
