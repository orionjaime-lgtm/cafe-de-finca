import type { Metadata } from 'next'
import { SettingsWorkspace } from '@/components/dashboard/settings-workspace'
import { PageHeading } from '@/components/dashboard/page-heading'

export const metadata: Metadata = {
  title: 'Configuración | Café de Finca',
  description: 'Preferencias de apariencia del sistema.',
}

export default function ConfiguracionPage() {
  return (
    <>
      <PageHeading
        eyebrow="SISTEMA / CONFIGURACIÓN"
        title="Configuración"
        description="Personalizá la apariencia del sistema según tus preferencias."
      />
      <SettingsWorkspace />
    </>
  )
}
