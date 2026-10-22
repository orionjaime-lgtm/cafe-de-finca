import type { Metadata } from 'next'
import { DegradationCurve } from '@/components/dashboard/caducidad/degradation-curve'
import { ExpiryKpis } from '@/components/dashboard/caducidad/expiry-kpis'
import { InventoryLots } from '@/components/dashboard/caducidad/inventory-lots'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Control de Caducidad | Café de Finca',
  description: 'Monitorea el ciclo de vida útil del café tostado para garantizar la máxima calidad.',
}

export default function CaducidadPage() {
  return (
    <>
      <PageHeading
        eyebrow="OPERACIONES / CONTROL DE CADUCIDAD"
        title="Control de Caducidad y Frescura"
        description="Monitorea el ciclo de vida útil del café tostado para garantizar la máxima calidad organoléptica en cada entrega."
      />
      <div className="mt-8">
        <ExpiryKpis />
      </div>
      <div className="mt-6 grid grid-cols-1 gap-6 xl:grid-cols-[minmax(0,65fr)_minmax(0,35fr)]">
        <InventoryLots />
        <DegradationCurve />
      </div>
    </>
  )
}
