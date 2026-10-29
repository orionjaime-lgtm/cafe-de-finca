import type { Metadata } from 'next'
import { ClientsWorkspace } from '@/components/dashboard/clients-workspace'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Clientes | Café de Finca',
  description: 'Clientes mayoristas y minoristas registrados.',
}

export default function ClientesPage() {
  return (
    <>
      <PageHeading
        eyebrow="VENTAS / CLIENTES"
        title="Clientes"
        description="Consultá los clientes B2B y B2C disponibles para operaciones de venta."
      />
      <ClientsWorkspace />
    </>
  )
}
