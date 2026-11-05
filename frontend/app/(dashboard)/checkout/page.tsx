import type { Metadata } from 'next'
import { CheckoutWorkspace } from '@/components/dashboard/cliente/checkout-workspace'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = { title: 'Checkout | Café de Finca', description: 'Finalizá tu compra y elegí el medio de pago.' }

export default function CheckoutPage() {
  return <><PageHeading eyebrow="CLIENTE / CHECKOUT" title="Finalizá tu compra" description="Revisá el total, elegí el medio de pago y confirmá el pedido." /><CheckoutWorkspace /></>
}
