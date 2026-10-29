import { cn } from '@/lib/utils'

type DashboardCardProps = {
  title: string
  description: string
  action?: React.ReactNode
  className?: string
  children: React.ReactNode
}

export function DashboardCard({ title, description, action, className, children }: DashboardCardProps) {
  return (
    <section
      className={cn(
        'flex flex-col rounded-2xl border border-border bg-card p-6 shadow-[0_1px_2px_rgba(28,25,23,0.04),0_8px_24px_-12px_rgba(28,25,23,0.08)] dark:shadow-[0_1px_2px_rgba(0,0,0,0.25),0_8px_24px_-12px_rgba(0,0,0,0.35)]',
        className,
      )}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <h2 className="text-base font-semibold text-card-foreground">{title}</h2>
          <p className="mt-1 text-sm text-muted-foreground">{description}</p>
        </div>
        {action}
      </div>
      <div className="mt-5 flex flex-1 flex-col">{children}</div>
    </section>
  )
}
