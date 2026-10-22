import { ActiveAlerts } from '@/components/dashboard/active-alerts'
import { NotificationChannels } from '@/components/dashboard/notification-channels'
import { PageHeading } from '@/components/dashboard/page-heading'
import { StockLimits } from '@/components/dashboard/stock-limits'

export default function AlertasPage() {
  return (
    <>
      <PageHeading
        eyebrow="OPERACIONES / ALERTAS PREDICTIVAS"
        title="Panel de Alertas Predictivas"
        description="Monitoreo automático de disponibilidad, previsión de consumo y notificaciones multicanal en origen."
      />
      <div className="mt-8 grid gap-6 lg:grid-cols-5">
        <div className="flex flex-col gap-6 lg:col-span-3">
          <ActiveAlerts />
          <NotificationChannels />
        </div>
        <div className="lg:col-span-2">
          <StockLimits />
        </div>
      </div>
    </>
  )
}
