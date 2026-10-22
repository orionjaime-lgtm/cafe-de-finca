import { Warehouse } from 'lucide-react'

export function TopHeader() {
  return (
    <header className="flex h-16 items-center justify-between border-b border-border bg-background/80 px-6 backdrop-blur lg:px-10">
      <div className="flex items-center gap-2.5 text-sm text-foreground">
        <span className="flex size-8 items-center justify-center rounded-md border border-border bg-card">
          <Warehouse className="size-4 text-primary" aria-hidden="true" />
        </span>
        <span className="font-medium">Tostaduría Principal</span>
        <span className="text-muted-foreground" aria-hidden="true">
          ·
        </span>
        <span className="text-muted-foreground">Buenos Aires</span>
      </div>

      <div className="flex items-center gap-3">
        <div className="hidden text-right leading-tight sm:block">
          <p className="text-sm font-medium">Mateo Ríos</p>
          <p className="text-xs text-muted-foreground">Administrador</p>
        </div>
        <div
          className="flex size-9 items-center justify-center rounded-full bg-[#fbd9c2] text-xs font-semibold text-[#9a3f12]"
          aria-label="Mateo Ríos"
          role="img"
        >
          MR
        </div>
      </div>
    </header>
  )
}
