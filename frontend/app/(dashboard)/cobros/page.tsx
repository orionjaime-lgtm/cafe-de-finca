import type { Metadata } from 'next'
import { B2bAccounts } from '@/components/dashboard/cobros/b2b-accounts'
import { B2cSales } from '@/components/dashboard/cobros/b2c-sales'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Gestión de Cobros | Café de Finca',
  description: 'Control de saldos corporativos B2B y registro de ventas minoristas B2C.',
}

export default function CobrosPage() {
  return (
    <>
      <PageHeading
        eyebrow="OPERACIONES / COBROS Y CUENTAS CORRIENTES"
        title="Cobros y Cuentas Corrientes"
        description="Gestión de saldos corporativos B2B y ventas minoristas B2C."
      />
      <div className="mt-8 flex flex-col gap-6">
        <B2bAccounts />
        <B2cSales />
      </div>
    </>
  )
}
