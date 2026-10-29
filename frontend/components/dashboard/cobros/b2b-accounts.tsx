import { Building2 } from 'lucide-react'
import { cn } from '@/lib/utils'
import { DashboardCard } from '../dashboard-card'

type Account = {
  client: string
  invoiceType: string
  debt: string
  status: 'due-soon' | 'overdue' | 'current'
  statusLabel: string
}

const accounts: Account[] = [
  {
    client: 'Café Cortázar S.A.S.',
    invoiceType: 'Factura A - 0012',
    debt: '$250,000',
    status: 'due-soon',
    statusLabel: 'Vence en 5 días',
  },
  {
    client: 'La Panadería Central',
    invoiceType: 'Factura A - 0010',
    debt: '$85,000',
    status: 'overdue',
    statusLabel: 'VENCIDO',
  },
  {
    client: 'Hotel Andino',
    invoiceType: 'Factura A - 0015',
    debt: '$120,000',
    status: 'current',
    statusLabel: 'Al día',
  },
]

const statusStyles = {
  'due-soon': 'border-warning/20 bg-warning/10 text-warning dark:text-amber-300',
  overdue: 'border-transparent bg-destructive text-white',
  current: 'border-success/20 bg-success/10 text-success dark:text-green-300',
}

export function B2bAccounts() {
  return (
    <DashboardCard
      title="Cuentas Corrientes B2B (Mayoristas)"
      description="Saldos pendientes de clientes corporativos con facturación a crédito."
    >
      <div className="-mx-6 overflow-x-auto">
        <table className="w-full min-w-[640px] text-left text-sm">
          <thead>
            <tr className="border-y border-border bg-muted/40 text-xs font-medium text-muted-foreground">
              <th scope="col" className="px-6 py-3 font-medium">
                Cliente
              </th>
              <th scope="col" className="px-3 py-3 font-medium">
                Factura
              </th>
              <th scope="col" className="px-3 py-3 text-right font-medium">
                Deuda Total
              </th>
              <th scope="col" className="px-3 py-3 font-medium">
                Estado
              </th>
              <th scope="col" className="px-6 py-3 text-right font-medium">
                Acción
              </th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr key={account.client} className="border-b border-border last:border-b-0">
                <td className="px-6 py-4">
                  <div className="flex items-center gap-3">
                    <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-secondary">
                      <Building2 className="size-4 text-primary" aria-hidden="true" />
                    </span>
                    <span className="font-medium whitespace-nowrap text-foreground">{account.client}</span>
                  </div>
                </td>
                <td className="px-3 py-4 whitespace-nowrap text-muted-foreground">{account.invoiceType}</td>
                <td className="px-3 py-4 text-right font-semibold whitespace-nowrap tabular-nums text-foreground">
                  {account.debt}
                </td>
                <td className="px-3 py-4">
                  <span
                    className={cn(
                      'inline-flex rounded-md border px-2 py-0.5 text-xs font-semibold whitespace-nowrap',
                      statusStyles[account.status],
                    )}
                  >
                    {account.statusLabel}
                  </span>
                </td>
                <td className="px-6 py-4 text-right">
                  <button
                    type="button"
                    className="rounded-lg border border-primary px-3 py-1.5 text-xs font-semibold whitespace-nowrap text-primary transition-colors hover:bg-primary hover:text-primary-foreground focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 focus-visible:outline-none"
                  >
                    Registrar Pago
                    <span className="sr-only"> de {account.client}</span>
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <div className="mt-auto flex items-center justify-between border-t border-border pt-4 text-sm">
        <span className="text-muted-foreground">Saldo total pendiente</span>
        <span className="font-semibold tabular-nums text-foreground">$455,000</span>
      </div>
    </DashboardCard>
  )
}
