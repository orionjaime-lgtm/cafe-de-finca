import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { cn } from '@/lib/utils'

const GREEN = '#15803d'
const ORANGE = '#c2410c'
const RED = '#b91c1c'

const milestones = [
  {
    title: 'Día 1 - 7 (Gas Desprendido)',
    text: 'Desgasificación activa. Aroma en crecimiento constante.',
    dot: 'bg-success',
  },
  {
    title: 'Día 8 - 21 (Punto Óptimo)',
    text: 'Sabor más balanceado. Máxima expresión de notas.',
    dot: 'bg-success',
  },
  {
    title: 'Día 22 - 30 (Alerta Comercial)',
    text: 'Disminución de brillo ácido. Priorizar salida.',
    dot: 'bg-accent',
  },
  {
    title: 'Día 30+ (Consumo Restante)',
    text: 'Pérdida de aceites esenciales y fragancia.',
    dot: 'bg-destructive',
  },
]

const xLabels = ['Día 0', 'Día 10', 'Día 20', 'Día 30']

export function DegradationCurve() {
  return (
    <DashboardCard title="Curva de Degradación" description="Pérdida de gases y notas aromáticas estimadas.">
      <figure className="rounded-xl bg-secondary/70 p-4">
        <div className="flex gap-2">
          <p className="w-4 shrink-0 self-center text-[10px] font-medium whitespace-nowrap text-muted-foreground [writing-mode:vertical-rl] rotate-180">
            Aroma & Sabor (%)
          </p>
          <div className="min-w-0 flex-1">
            <svg
              viewBox="0 0 300 160"
              className="h-40 w-full"
              preserveAspectRatio="none"
              role="img"
              aria-label="La calidad aromática se mantiene alta hasta el día 21, desciende en la zona de alerta y cae tras el día 30."
            >
              {[20, 60, 100, 140].map((y) => (
                <line key={y} x1="0" x2="300" y1={y} y2={y} stroke="#d6d3d1" strokeDasharray="2 4" strokeWidth="1" vectorEffect="non-scaling-stroke" />
              ))}
              <path
                d="M0 40 L 140 26 L 210 46"
                fill="none"
                stroke={GREEN}
                strokeWidth="3"
                strokeLinecap="butt"
                strokeLinejoin="miter"
                vectorEffect="non-scaling-stroke"
              />
              <path
                d="M210 46 L 270 96"
                fill="none"
                stroke={ORANGE}
                strokeWidth="3"
                strokeDasharray="6 5"
                strokeLinecap="butt"
                strokeLinejoin="miter"
                vectorEffect="non-scaling-stroke"
              />
              <path
                d="M270 96 L 300 146"
                fill="none"
                stroke={RED}
                strokeWidth="3"
                strokeLinecap="butt"
                strokeLinejoin="miter"
                vectorEffect="non-scaling-stroke"
              />
            </svg>
            <div className="mt-2 flex justify-between text-[11px] font-medium text-muted-foreground">
              {xLabels.map((label) => (
                <span key={label}>{label}</span>
              ))}
            </div>
          </div>
        </div>
      </figure>

      <div className="mt-6">
        <h3 className="text-sm font-semibold text-foreground">Hitos de Calidad:</h3>
        <ol className="mt-4 flex flex-col gap-4">
          {milestones.map((m) => (
            <li key={m.title} className="flex gap-3">
              <span aria-hidden="true" className={cn('mt-1.5 size-2.5 shrink-0 rounded-full', m.dot)} />
              <div>
                <p className="text-sm font-medium text-foreground">{m.title}</p>
                <p className="mt-0.5 text-xs leading-relaxed text-muted-foreground">{m.text}</p>
              </div>
            </li>
          ))}
        </ol>
      </div>
    </DashboardCard>
  )
}
