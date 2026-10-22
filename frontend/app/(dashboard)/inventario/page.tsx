import type { Metadata } from 'next'
import { InventoryWorkspace } from '@/components/dashboard/inventory-workspace'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Inventario | Café de Finca',
  description: 'Control de café verde recibido y lotes tostados por frescura.',
}

export default function InventarioPage() {
  return (
    <>
      <PageHeading
        eyebrow="OPERACIONES / INVENTARIO"
        title="Inventario de Café"
        description="Controlá el café verde recibido y el café tostado disponible, con trazabilidad por lote y frescura."
      />
      <InventoryWorkspace />
    </>
  )
}
