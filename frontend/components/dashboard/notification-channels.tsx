'use client'

import { useState } from 'react'
import { Mail, MessageCircle, Smartphone, type LucideIcon } from 'lucide-react'
import { cn } from '@/lib/utils'
import { DashboardCard } from './dashboard-card'

type Channel = { id: string; label: string; detail: string; icon: LucideIcon }

const channels: Channel[] = [
  { id: 'push', label: 'Mensajería Push', detail: 'App móvil y escritorio', icon: Smartphone },
  { id: 'email', label: 'Correo Electrónico', detail: 'mateo@cafedefinca.co', icon: Mail },
  { id: 'whatsapp', label: 'WhatsApp Directo', detail: '+57 300 ··· 4821', icon: MessageCircle },
]

export function NotificationChannels() {
  const [enabled, setEnabled] = useState<Record<string, boolean>>({
    push: true,
    email: true,
    whatsapp: false,
  })

  return (
    <DashboardCard
      title="Canales de Notificación"
      description="Activa dónde deseas recibir los avisos predictivos automáticos."
    >
      <div className="grid gap-3 sm:grid-cols-3">
        {channels.map(({ id, label, detail, icon: Icon }) => {
          const isOn = enabled[id]
          const labelId = `channel-${id}`
          return (
            <div
              key={id}
              className={cn(
                'flex flex-col gap-4 rounded-xl border p-4 transition-colors',
                isOn ? 'border-primary/20 bg-secondary' : 'border-border bg-card',
              )}
            >
              <div className="flex items-center justify-between">
                <span
                  className={cn(
                    'flex size-8 items-center justify-center rounded-lg',
                    isOn ? 'bg-primary/10 text-primary' : 'bg-muted text-muted-foreground',
                  )}
                >
                  <Icon className="size-4" aria-hidden="true" />
                </span>
                <button
                  type="button"
                  role="switch"
                  aria-checked={isOn}
                  aria-labelledby={labelId}
                  onClick={() => setEnabled((prev) => ({ ...prev, [id]: !prev[id] }))}
                  className={cn(
                    'relative inline-flex h-5.5 w-10 shrink-0 cursor-pointer items-center rounded-full transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring',
                    isOn ? 'bg-primary' : 'bg-stone-300',
                  )}
                >
                  <span
                    className={cn(
                      'inline-block size-4.5 rounded-full bg-white shadow-sm transition-transform',
                      isOn ? 'translate-x-5' : 'translate-x-0.5',
                    )}
                  />
                </button>
              </div>
              <div>
                <p id={labelId} className="text-sm font-medium text-foreground">
                  {label}
                </p>
                <p className="mt-0.5 truncate text-xs text-muted-foreground">
                  {isOn ? detail : 'Desactivado'}
                </p>
              </div>
            </div>
          )
        })}
      </div>
    </DashboardCard>
  )
}
