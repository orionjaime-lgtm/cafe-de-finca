'use client'

import { Coffee, TriangleAlert, X } from 'lucide-react'
import { cn } from '@/lib/utils'
import { useRoasteryDemo } from '@/components/dashboard/roastery-demo-provider'

export function ExpiryKpis() {
  const { expirySummary } = useRoasteryDemo()
  const kpis = [{ title: 'Lotes Activos Tostados', value: expirySummary.activos, caption: 'En almacenamiento óptimo', icon: Coffee, iconClass: 'bg-[#f3e8dd] text-[#6b4c3a]' }, { title: 'Por Vencer (Próximos 7 días)', value: expirySummary.porVencer, caption: 'Requieren rotación rápida', icon: TriangleAlert, iconClass: 'bg-orange-50 text-accent' }, { title: 'Lotes Vencidos', value: expirySummary.vencidos, caption: 'Garantía de calidad superada', icon: X, iconClass: 'bg-red-50 text-destructive' }] as const
  return <ul className="grid grid-cols-1 gap-6 md:grid-cols-3">{kpis.map(({ title, value, caption, icon: Icon, iconClass }) => <li key={title} className="flex items-start justify-between gap-4 rounded-2xl border border-border bg-card p-6 shadow-[0_1px_2px_rgba(28,25,23,0.04),0_8px_24px_-12px_rgba(28,25,23,0.08)]"><div><p className="text-sm font-medium text-muted-foreground">{title}</p><p className="mt-3 text-4xl font-bold tracking-tight tabular-nums text-foreground">{value}</p><p className="mt-1.5 text-xs text-muted-foreground">{caption}</p></div><div className={cn('flex size-11 shrink-0 items-center justify-center rounded-xl', iconClass)}><Icon className="size-5" /></div></li>)}</ul>
}
