'use client'

import Link from 'next/link'
import { useRouter } from 'next/navigation'
import { useState } from 'react'
import { Coffee, Eye, EyeOff, LockKeyhole, Mail } from 'lucide-react'
import { useAuth } from '@/components/auth-provider'

export function AuthLogin() {
  const router = useRouter()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [error, setError] = useState('')

  function submit(event: React.FormEvent) {
    event.preventDefault()
    const result = login(email, password)
    if (!result.ok) { setError(result.mensaje); return }
    router.replace(result.role === 'cliente' ? '/compras' : '/')
  }

  return (
    <main className="grid min-h-screen lg:grid-cols-[1.05fr_0.95fr]">
      <section className="relative hidden overflow-hidden bg-[#1c1c1c] p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(192,138,99,0.18),transparent_35%),linear-gradient(135deg,#20201e,#171513)]" />
        <div className="relative flex items-center gap-3"><span className="flex size-11 items-center justify-center rounded-xl bg-[#6b4c3a]"><Coffee className="size-5" /></span><div><p className="font-semibold">Café de Finca</p><p className="text-xs text-stone-400">Sistema de gestión</p></div></div>
        <div className="relative max-w-lg"><p className="text-xs font-semibold tracking-[0.18em] text-[#c08a63]">BIENVENIDO</p><h1 className="mt-4 text-5xl font-semibold leading-tight">El café se controla mejor con trazabilidad.</h1><p className="mt-5 text-base leading-relaxed text-stone-300">Accedé a inventario, producción, ventas y cobros desde un único panel.</p></div>
        <p className="relative text-xs text-stone-500">© 2026 Café de Finca</p>
      </section>
      <section className="flex items-center justify-center bg-[#f7f6f2] px-6 py-10">
        <div className="w-full max-w-md"><div className="mb-8 lg:hidden flex items-center gap-3"><span className="flex size-10 items-center justify-center rounded-xl bg-primary text-primary-foreground"><Coffee className="size-5" /></span><div><p className="font-semibold">Café de Finca</p><p className="text-xs text-muted-foreground">Sistema de gestión</p></div></div>
          <p className="text-xs font-semibold tracking-[0.14em] text-accent">CUENTA LOCAL</p><h1 className="mt-2 text-3xl font-bold">Iniciar sesión</h1><p className="mt-2 text-sm text-muted-foreground">Ingresá tus datos para acceder al panel.</p>
          <form onSubmit={submit} className="mt-8 space-y-5"><label className="block"><span className="mb-2 block text-sm font-medium">Correo electrónico</span><span className="relative block"><Mail className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" /><input type="email" required value={email} onChange={(event) => setEmail(event.target.value)} placeholder="nombre@correo.com" className="h-11 w-full rounded-lg border border-input bg-card pl-10 pr-3 text-sm outline-none focus:border-primary focus:ring-3 focus:ring-primary/10" /></span></label>
          <label className="block"><span className="mb-2 block text-sm font-medium">Contraseña</span><span className="relative block"><LockKeyhole className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" /><input type={showPassword ? 'text' : 'password'} required value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Ingresá tu contraseña" className="h-11 w-full rounded-lg border border-input bg-card pl-10 pr-11 text-sm outline-none focus:border-primary focus:ring-3 focus:ring-primary/10" /><button type="button" onClick={() => setShowPassword((value) => !value)} aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'} className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground">{showPassword ? <EyeOff className="size-4" /> : <Eye className="size-4" />}</button></span></label>
          {error && <p role="alert" className="rounded-lg border border-destructive/20 bg-destructive/10 px-3 py-2.5 text-sm text-destructive">{error}</p>}
          <button type="submit" className="h-11 w-full rounded-lg bg-primary text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90">Ingresar</button></form>
          <p className="mt-6 text-center text-sm text-muted-foreground">¿No tenés cuenta? <Link href="/registro" className="font-semibold text-primary hover:underline">Registrate</Link></p>
          <details className="mt-7 rounded-xl border border-border bg-card p-4 text-xs text-muted-foreground"><summary className="cursor-pointer font-medium text-foreground">Usuarios de prueba</summary><div className="mt-3 grid gap-2"><p><strong>Administrador:</strong> admin@cafedefinca.com / admin123</p><p><strong>Cliente particular:</strong> cliente@cafedefinca.com / cliente123</p><p><strong>Mayorista:</strong> mayorista@cafedefinca.com / mayorista123</p></div></details>
        </div>
      </section>
    </main>
  )
}
