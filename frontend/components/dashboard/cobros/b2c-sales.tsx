import { CheckCircle2 } from 'lucide-react'
import { DashboardCard } from '../dashboard-card'

type Sale = { customer: string; product: string; method: string }

const sales: Sale[] = [
  { customer: 'Lucas', product: '250g Colombia Huila', method: 'Mercado Pago' },
  { customer: 'Camila', product: '1kg Blend Espresso', method: 'Efectivo' },
]

export function B2cSales() {
  return (
    <DashboardCard
      title="Últimos Cobros B2C (Mostrador / Instagram)"
      description="Ventas directas recientes, cobradas al momento y sin deuda."
    >
      <ul className="flex flex-col divide-y divide-border">
        {sales.map((sale) => (
          <li key={`${sale.customer}-${sale.product}`} className="flex items-center gap-3 py-3.5 first:pt-0">
            <span
              aria-hidden="true"
              className="flex size-9 shrink-0 items-center justify-center rounded-full bg-secondary text-xs font-semibold text-primary"
            >
              {sale.customer.charAt(0)}
            </span>
            <div className="min-w-0 flex-1">
              <p className="text-sm font-medium text-foreground">
                {sale.customer}
                <span className="font-normal text-muted-foreground"> · {sale.product}</span>
              </p>
              <p className="mt-0.5 text-xs text-muted-foreground">Medio de pago: {sale.method}</p>
            </div>
            <span className="inline-flex shrink-0 items-center gap-1.5 rounded-md border border-success/20 bg-success/10 px-2 py-0.5 text-xs font-semibold text-success dark:text-green-300">
              <CheckCircle2 className="size-3.5" aria-hidden="true" />
              <span className="sr-only">Estado: </span>Pagado
            </span>
          </li>
        ))}
      </ul>
    </DashboardCard>
  )
}
