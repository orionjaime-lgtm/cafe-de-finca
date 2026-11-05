'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
  Bell,
  CalendarClock,
  Flame,
  Leaf,
  Package,
  Settings,
  ShoppingBag,
  ShoppingCart,
  Coffee,
  Wallet,
  Users,
  Repeat2,
  Route,
  ShieldCheck,
  X,
  type LucideIcon,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'
import { useAuth } from '@/components/auth-provider'

type NavItem = { label: string; icon: LucideIcon; href: string; section: string; badge?: number }

const navItems: NavItem[] = [
  { label: 'Inventario', icon: Package, href: '/inventario', section: 'inventario' },
  { label: 'Tueste', icon: Flame, href: '/tueste', section: 'tueste' },
  { label: 'Cafés', icon: Coffee, href: '/catalogo', section: 'catalogo' },
  { label: 'Mis Compras', icon: ShoppingBag, href: '/compras', section: 'compras' },
  { label: 'Pedidos', icon: ShoppingCart, href: '/pedidos', section: 'pedidos' },
  { label: 'Clientes', icon: Users, href: '/clientes', section: 'clientes' },
  { label: 'Abonos', icon: Repeat2, href: '/abonos', section: 'abonos' },
  { label: 'Hojas de ruta', icon: Route, href: '/hojas-ruta', section: 'hojas-ruta' },
  { label: 'Usuarios', icon: ShieldCheck, href: '/usuarios', section: 'usuarios' },
  { label: 'Cobros', icon: Wallet, href: '/cobros', section: 'cobros' },
  { label: 'Alertas', icon: Bell, href: '/', section: 'alertas' },
  { label: 'Caducidad', icon: CalendarClock, href: '/caducidad', section: 'caducidad' },
  { label: 'Configuración', icon: Settings, href: '/configuracion', section: 'configuracion' },
]

export function AppSidebar() {
  const pathname = usePathname()
  const { alerts, isReady } = useRoasteryDemo()
  const { user } = useAuth()
  const [mobileOpen, setMobileOpen] = useState(false)
  const alertBadge = isReady ? alerts.length : undefined
  const visibleNavItems = navItems.filter((item) => user?.secciones.includes(item.section))

  useEffect(() => {
    const abrir = () => setMobileOpen(true)
    window.addEventListener('cafe:abrir-menu', abrir)
    return () => window.removeEventListener('cafe:abrir-menu', abrir)
  }, [])

  useEffect(() => {
    if (!mobileOpen) return
    const cerrarConEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') setMobileOpen(false)
    }
    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', cerrarConEscape)
    return () => {
      document.body.style.overflow = ''
      window.removeEventListener('keydown', cerrarConEscape)
    }
  }, [mobileOpen])

  return (
    <>
      {mobileOpen && (
        <button
          type="button"
          aria-label="Cerrar menú"
          className="fixed inset-0 z-40 bg-black/45 backdrop-blur-[1px] lg:hidden"
          onClick={() => setMobileOpen(false)}
        />
      )}
      <aside className={cn('fixed inset-y-0 left-0 z-50 flex h-dvh w-[min(18rem,85vw)] shrink-0 flex-col bg-sidebar px-4 py-5 shadow-2xl transition-transform duration-200 lg:sticky lg:top-0 lg:z-auto lg:h-screen lg:w-64 lg:translate-x-0 lg:shadow-none lg:py-6', mobileOpen ? 'translate-x-0' : '-translate-x-full')}>
      <div className="flex items-center justify-between gap-3 px-2">
        <div className="flex items-center gap-3">
          <div className="flex size-9 items-center justify-center rounded-lg bg-sidebar-primary">
            <Leaf className="size-4.5 text-sidebar-primary-foreground" aria-hidden="true" />
          </div>
          <div className="leading-tight">
            <p className="text-sm font-semibold text-white">Café de Finca</p>
            <p className="text-xs text-sidebar-foreground">Control de Stock</p>
          </div>
        </div>
        <button type="button" aria-label="Cerrar menú" onClick={() => setMobileOpen(false)} className="flex size-9 items-center justify-center rounded-lg text-sidebar-foreground hover:bg-white/10 hover:text-white lg:hidden">
          <X className="size-4.5" />
        </button>
      </div>

      <nav aria-label="Operaciones" className="mt-10 flex-1">
        <p className="px-3 text-[11px] font-medium tracking-[0.12em] text-stone-500">OPERACIONES</p>
        <ul className="mt-3 flex flex-col gap-1">
          {visibleNavItems.map(({ label, icon: Icon, href, badge: itemBadge }) => {
            const active = href !== '#' && pathname === href
            const badge = label === 'Alertas' ? alertBadge : itemBadge
            return (
            <li key={label}>
              <Link
                href={href}
                onClick={() => setMobileOpen(false)}
                aria-current={active ? 'page' : undefined}
                className={cn(
                  'relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm transition-colors',
                  active
                    ? 'bg-sidebar-accent font-medium text-sidebar-accent-foreground'
                    : 'text-sidebar-foreground hover:bg-white/5 hover:text-white',
                )}
              >
                {active && (
                  <span
                    aria-hidden="true"
                    className="absolute inset-y-2 left-0 w-0.5 rounded-full bg-[#c08a63]"
                  />
                )}
                <Icon className={cn('size-4', active && 'text-[#c08a63]')} aria-hidden="true" />
                <span className="flex-1">{label}</span>
                {badge ? (
                  <span className="rounded-full bg-destructive px-1.5 py-0.5 text-[10px] font-semibold leading-none text-white">
                    {badge}
                    <span className="sr-only"> alertas activas</span>
                  </span>
                ) : null}
              </Link>
            </li>
            )
          })}
        </ul>
      </nav>

      <div className="rounded-xl border border-sidebar-border bg-[#262626] p-4">
        <div className="flex items-center gap-2">
          <span className="relative flex size-2">
            <span className="absolute inline-flex size-full animate-ping rounded-full bg-emerald-400 opacity-60" />
            <span className="relative inline-flex size-2 rounded-full bg-emerald-400" />
          </span>
          <p className="text-sm font-medium text-white">Planta operativa</p>
        </div>
        <p className="mt-1 pl-4 text-xs text-sidebar-foreground">Stock sincronizado · hace 2 min</p>
      </div>
    </aside>
    </>
  )
}
