import type { Metadata } from 'next'
import { CatalogoWorkspace } from '@/components/dashboard/cliente/catalogo-workspace'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = { title: 'Cafés | Café de Finca', description: 'Catálogo de cafés disponibles.' }

export default function CatalogoPage() {
  return <><PageHeading eyebrow="CLIENTE / CAFÉS" title="Cafés disponibles" description="Elegí un café, configurá tu presentación y continuá al checkout." /><CatalogoWorkspace /></>
}
