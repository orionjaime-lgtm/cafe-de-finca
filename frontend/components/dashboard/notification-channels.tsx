'use client'

import { DashboardCard } from './dashboard-card'

export function NotificationChannels() {
  return <DashboardCard title="Canales de Notificación" description="Las notificaciones externas aún no están configuradas en el backend.">
    <p className="rounded-lg border border-border bg-muted/30 p-4 text-sm text-muted-foreground">
      Las alertas se muestran en este panel. Push, correo y WhatsApp requieren cuentas de servicio y tablas de preferencias que aún no existen, por lo que no se ofrecen controles simulados.
    </p>
  </DashboardCard>
}
