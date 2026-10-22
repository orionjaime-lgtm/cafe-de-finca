import { AppSidebar } from '@/components/dashboard/app-sidebar'
import { RoasteryDemoProvider } from '@/components/dashboard/roastery-demo-provider'
import { TopHeader } from '@/components/dashboard/top-header'

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  return (
    <RoasteryDemoProvider>
      <div className="flex min-h-screen bg-background">
        <AppSidebar />
        <div className="flex min-w-0 flex-1 flex-col">
          <TopHeader />
          <main className="flex-1 px-6 py-8 lg:px-10 lg:py-10">
            <div className="mx-auto max-w-7xl">{children}</div>
          </main>
        </div>
      </div>
    </RoasteryDemoProvider>
  )
}
