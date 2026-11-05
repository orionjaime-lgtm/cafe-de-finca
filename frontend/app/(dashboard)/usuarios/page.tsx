import type { Metadata } from 'next'
import { PageHeading } from '@/components/dashboard/page-heading'
import { UsuariosWorkspace } from '@/components/dashboard/usuarios-workspace'

export const metadata: Metadata = { title: 'Usuarios | Café de Finca' }

export default function UsuariosPage() {
  return <><PageHeading eyebrow="SISTEMA / USUARIOS" title="Usuarios" description="Administración de cuentas, roles y accesos conectada al backend."/><UsuariosWorkspace /></>
}
