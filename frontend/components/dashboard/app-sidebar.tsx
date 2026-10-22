'use client'

import Link from 'next/link'
import { usePathname } from 'next/navigation'
import {
  Bell,
  CalendarClock,
  Flame,
  Leaf,
  Package,
  Settings,
  ShoppingCart,
  Wallet,
  type LucideIcon,
} from 'lucide-react'
import { cn } from '@/lib/utils'

type NavItem = { label: string; icon: LucideIcon; href: string; badge?: number }

const navItems: NavItem[] = [
  { label: 'Inventario', icon: Package, href: '/inventario' },
  { label: 'Tueste', icon: Flame, href: '/tueste' },
  { label: 'Pedidos', icon: ShoppingCart, href: '/pedidos' },
  { label: 'Cobros', icon: Wallet, href: '/cobros' },
  { label: 'Alertas', icon: Bell, href: '/', badge: 3 },
  { label: 'Caducidad', icon: CalendarClock, href: '/caducidad' },
  { label: 'Configuración', icon: Settings, href: '#' },
]

export function AppSidebar() {
  const pathname = usePathname()

  return (
    <aside className="sticky top-0 hidden h-screen w-64 shrink-0 flex-col bg-sidebar px-4 py-6 lg:flex">
      <div className="flex items-center gap-3 px-2">
        <div className="flex size-9 items-center justify-center rounded-lg bg-sidebar-primary">
          <Leaf className="size-4.5 text-sidebar-primary-foreground" aria-hidden="true" />
        </div>
        <div className="leading-tight">
          <p className="text-sm font-semibold text-white">Café de Finca</p>
          <p className="text-xs text-sidebar-foreground">Control de Stock</p>
        </div>
      </div>

      <nav aria-label="Operaciones" className="mt-10 flex-1">
        <p className="px-3 text-[11px] font-medium tracking-[0.12em] text-stone-500">OPERACIONES</p>
        <ul className="mt-3 flex flex-col gap-1">
          {navItems.map(({ label, icon: Icon, href, badge }) => {
            const active = href !== '#' && pathname === href
            return (
            <li key={label}>
              <Link
                href={href}
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
  )
}
