import type { Metadata } from 'next'
import { PageHeading } from '@/components/dashboard/page-heading'
import { RoastWorkspace } from '@/components/dashboard/roast-workspace'

export const metadata: Metadata = {
  title: 'Producción y Tueste | Café de Finca',
  description: 'Registra órdenes de tueste, merma, reposo y producción de café tostado.',
}

export default function TuestePage() {
  return (
    <>
      <PageHeading
        eyebrow="PRODUCCIÓN / ÓRDENES DE TUESTE"
        title="Producción y Tueste"
        description="Registrá el café verde utilizado, el perfil de tueste, la merma real y el reposo antes de la venta."
      />
      <RoastWorkspace />
    </>
  )
}
