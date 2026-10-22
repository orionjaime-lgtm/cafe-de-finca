import { ChevronRight } from 'lucide-react'
import { cn } from '@/lib/utils'
import { DashboardCard } from './dashboard-card'

type Alert = {
  level: 'critical' | 'warning'
  origin: string
  message: string
  time: string
}

const alerts: Alert[] = [
  {
    level: 'critical',
    origin: 'Colombia Huila',
    message: 'Stock crítico: 2.3 kg restantes. El mínimo configurado es 5 kg.',
    time: 'hace 12 min',
  },
  {
    level: 'warning',
    origin: 'Brasil Santos',
    message:
      'Reabastecer en 3 días estimados según tu tasa de consumo promedio de 3.8 kg/día.',
    time: 'hace 1 h',
  },
  {
    level: 'critical',
    origin: 'Geisha Panamá',
    message: 'Stock agotado totalmente. Hay 2 pedidos pendientes que no pueden despacharse.',
    time: 'hace 3 h',
  },
]

const levelStyles = {
  critical: {
    container: 'border-red-200 bg-red-50/70 hover:border-red-300',
    badge: 'bg-destructive text-white',
    label: 'CRÍTICO',
  },
  warning: {
    container: 'border-amber-200 bg-amber-50/70 hover:border-amber-300',
    badge: 'bg-warning text-white',
    label: 'ADVERTENCIA',
  },
}

export function ActiveAlerts() {
  return (
    <DashboardCard
      title="Alertas Activas"
      description="Incidencias críticas que requieren atención inmediata."
      action={
        <span className="rounded-full bg-red-50 px-2.5 py-1 text-xs font-medium text-destructive">
          {alerts.length} abiertas
        </span>
      }
    >
      <ul className="flex flex-col gap-3">
        {alerts.map((alert) => {
          const style = levelStyles[alert.level]
          return (
            <li key={alert.origin}>
              <a
                href="#"
                className={cn(
                  'group flex items-center gap-4 rounded-xl border p-4 transition-colors',
                  style.container,
                )}
              >
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-2.5">
                    <span
                      className={cn(
                        'rounded px-2 py-0.5 text-[10px] font-semibold tracking-wider',
                        style.badge,
                      )}
                    >
                      {style.label}
                    </span>
                    <h3 className="text-sm font-semibold text-foreground">{alert.origin}</h3>
                    <span className="text-xs text-muted-foreground">{alert.time}</span>
                  </div>
                  <p className="mt-1.5 text-sm leading-relaxed text-stone-600">{alert.message}</p>
                </div>
                <ChevronRight
                  className="size-4 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5 group-hover:text-foreground"
                  aria-hidden="true"
                />
                <span className="sr-only">Ver detalle de {alert.origin}</span>
              </a>
            </li>
          )
        })}
      </ul>
    </DashboardCard>
  )
}
