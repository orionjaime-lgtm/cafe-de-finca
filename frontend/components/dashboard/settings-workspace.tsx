'use client'

import { Check, Moon, RotateCcw, Sun, Type } from 'lucide-react'
import { DashboardCard } from '@/components/dashboard/dashboard-card'
import { FontSizePreference, ThemePreference, usePreferences } from '@/components/dashboard/preferences-provider'
import { cn } from '@/lib/utils'

const themes: { value: ThemePreference; label: string; description: string; icon: typeof Sun }[] = [
  { value: 'light', label: 'Modo claro', description: 'Fondo claro y contraste cálido.', icon: Sun },
  { value: 'dark', label: 'Modo oscuro', description: 'Fondo oscuro para ambientes con poca luz.', icon: Moon },
]

const fontSizes: { value: FontSizePreference; label: string; sample: string }[] = [
  { value: 'small', label: 'Pequeño', sample: 'Aa' },
  { value: 'medium', label: 'Mediano', sample: 'Aa' },
  { value: 'large', label: 'Grande', sample: 'Aa' },
]

export function SettingsWorkspace() {
  const { theme, fontSize, setTheme, setFontSize, resetPreferences } = usePreferences()

  return (
    <div className="mt-8 grid gap-6 xl:grid-cols-2">
      <DashboardCard
        title="Apariencia"
        description="Elegí cómo querés ver el sistema."
        action={<span className="flex size-9 items-center justify-center rounded-lg bg-secondary text-primary"><Sun className="size-4" /></span>}
      >
        <fieldset>
          <legend className="mb-3 text-sm font-medium">Tema</legend>
          <div className="grid gap-3 sm:grid-cols-2">
            {themes.map(({ value, label, description, icon: Icon }) => {
              const selected = theme === value
              return (
                <button
                  key={value}
                  type="button"
                  role="radio"
                  aria-checked={selected}
                  onClick={() => setTheme(value)}
                  className={cn(
                    'relative flex min-h-28 items-start gap-3 rounded-xl border p-4 text-left transition-colors',
                    selected ? 'border-primary bg-primary/5 ring-1 ring-primary/20' : 'border-border hover:bg-muted/50',
                  )}
                >
                  <span className={cn('flex size-9 shrink-0 items-center justify-center rounded-lg', value === 'dark' ? 'bg-secondary text-foreground' : 'bg-orange-50 text-orange-700 dark:bg-orange-950/40 dark:text-orange-300')}><Icon className="size-4" /></span>
                  <span className="pr-5"><span className="block text-sm font-semibold">{label}</span><span className="mt-1 block text-xs leading-relaxed text-muted-foreground">{description}</span></span>
                  {selected && <Check className="absolute right-3 top-3 size-4 text-primary" />}
                </button>
              )
            })}
          </div>
        </fieldset>
      </DashboardCard>

      <DashboardCard
        title="Tamaño de letra"
        description="Ajustá el tamaño general de textos y controles."
        action={<span className="flex size-9 items-center justify-center rounded-lg bg-secondary text-primary"><Type className="size-4" /></span>}
      >
        <fieldset>
          <legend className="mb-3 text-sm font-medium">Escala</legend>
          <div className="grid grid-cols-3 gap-3">
            {fontSizes.map(({ value, label, sample }) => {
              const selected = fontSize === value
              return (
                <button
                  key={value}
                  type="button"
                  role="radio"
                  aria-checked={selected}
                  onClick={() => setFontSize(value)}
                  className={cn('flex min-h-28 flex-col items-center justify-center rounded-xl border transition-colors', selected ? 'border-primary bg-primary/5 ring-1 ring-primary/20' : 'border-border hover:bg-muted/50')}
                >
                  <span className={cn('font-semibold text-foreground', value === 'small' && 'text-base', value === 'medium' && 'text-xl', value === 'large' && 'text-3xl')}>{sample}</span>
                  <span className="mt-2 text-xs font-medium text-muted-foreground">{label}</span>
                </button>
              )
            })}
          </div>
        </fieldset>
      </DashboardCard>

      <DashboardCard title="Vista previa" description="Los cambios se aplican inmediatamente." className="xl:col-span-2">
        <div className="rounded-xl border border-border bg-background p-5">
          <p className="text-xs font-semibold tracking-[0.12em] text-accent">INVENTARIO</p>
          <h3 className="mt-2 text-lg font-semibold">Café de origen único</h3>
          <p className="mt-1 max-w-xl text-sm leading-relaxed text-muted-foreground">Ejemplo de texto para comprobar la escala seleccionada y la combinación de colores del tema.</p>
          <div className="mt-4 flex gap-2"><span className="rounded-md bg-secondary px-2.5 py-1 text-xs font-medium">Disponible</span><span className="rounded-md bg-primary px-2.5 py-1 text-xs font-medium text-primary-foreground">Ver lote</span></div>
        </div>
      </DashboardCard>

      <div className="flex items-center justify-end xl:col-span-2">
        <button type="button" onClick={resetPreferences} className="inline-flex h-10 items-center gap-2 rounded-lg border border-border bg-card px-4 text-sm font-medium hover:bg-muted"><RotateCcw className="size-4" />Restablecer valores</button>
      </div>
    </div>
  )
}
