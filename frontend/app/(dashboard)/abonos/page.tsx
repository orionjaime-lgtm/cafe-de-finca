import type { Metadata } from 'next'
import { PageHeading } from '@/components/dashboard/page-heading'
import { SuscripcionesWorkspace } from '@/components/dashboard/suscripciones-workspace'

export const metadata: Metadata = { title: 'Abonos | Café de Finca' }

export default function AbonosPage() {
  return <><PageHeading eyebrow="VENTAS / ABONOS" title="Abonos recurrentes" description="Suscripciones, generación automática y planes de membresía conectados al backend."/><SuscripcionesWorkspace /></>
}
