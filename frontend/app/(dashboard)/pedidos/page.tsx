import type { Metadata } from 'next'
import { OrderForm } from '@/components/dashboard/pedidos/order-form'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Nuevo Pedido | Café de Finca',
  description: 'Registra salidas de café y convierte unidades automáticamente para un inventario preciso.',
}

export default function PedidosPage() {
  return (
    <>
      <PageHeading
        eyebrow="PEDIDOS / CONVERSIÓN DE UNIDADES"
        title="Nuevo Pedido (Conversor)"
        description="Registra una salida y convierte automáticamente la cantidad para mantener el inventario preciso."
      />
      <div className="mt-8 max-w-3xl">
        <OrderForm />
      </div>
    </>
  )
}
