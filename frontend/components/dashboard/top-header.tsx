'use client'

import { LogOut, Menu, Warehouse } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { obtenerNombreRol, useAuth } from '@/components/auth-provider'

export function TopHeader() {
  const { user, logout } = useAuth()
  const router = useRouter()

  function cerrarSesion() {
    logout()
    router.replace('/login')
  }

  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-background/80 px-3 backdrop-blur sm:px-6 lg:px-10">
      <div className="flex min-w-0 items-center gap-2.5 text-sm text-foreground">
        <button type="button" aria-label="Abrir menú" onClick={() => window.dispatchEvent(new CustomEvent('cafe:abrir-menu'))} className="flex size-9 shrink-0 items-center justify-center rounded-lg border border-border bg-card text-foreground hover:bg-muted lg:hidden">
          <Menu className="size-4.5" />
        </button>
        <span className="flex size-8 items-center justify-center rounded-md border border-border bg-card">
          <Warehouse className="size-4 text-primary" aria-hidden="true" />
        </span>
        <span className="max-w-36 truncate font-medium sm:max-w-none">Tostaduría Principal</span>
        <span className="hidden text-muted-foreground sm:inline" aria-hidden="true">
          ·
        </span>
        <span className="hidden text-muted-foreground sm:inline">Buenos Aires</span>
      </div>

      <div className="flex items-center gap-3">
        <div className="hidden text-right leading-tight sm:block">
          <p className="text-sm font-medium">{user?.nombre ?? 'Usuario'}</p>
          <p className="text-xs text-muted-foreground">{user ? obtenerNombreRol(user.role) : 'Usuario'}</p>
        </div>
        <div
          className="flex size-9 items-center justify-center rounded-full bg-secondary text-xs font-semibold text-primary"
          aria-label={user?.nombre ?? 'Usuario'}
          role="img"
        >
          {(user?.nombre ?? 'U').split(' ').map((part) => part[0]).join('').slice(0, 2).toUpperCase()}
        </div>
        <button type="button" onClick={cerrarSesion} className="flex size-9 items-center justify-center rounded-lg border border-border bg-card text-muted-foreground hover:text-foreground" aria-label="Cerrar sesión" title="Cerrar sesión">
          <LogOut className="size-4" />
        </button>
      </div>
    </header>
  )
}
