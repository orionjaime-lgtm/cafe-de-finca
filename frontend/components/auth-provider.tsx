'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { usePathname, useRouter } from 'next/navigation'

type UserRole = 'admin' | 'cliente'
export type ClientType = 'particular' | 'mayorista'
type LocalUser = { nombre: string; email: string; password: string; role: UserRole; tipoCliente?: ClientType }
type SessionUser = { nombre: string; email: string; role: UserRole; tipoCliente?: ClientType }
type AuthContextType = {
  user: SessionUser | null
  isReady: boolean
  login: (email: string, password: string) => { ok: boolean; mensaje: string; role?: UserRole }
  register: (nombre: string, email: string, password: string, tipoCliente?: ClientType) => { ok: boolean; mensaje: string; role?: UserRole }
  logout: () => void
}

const AuthContext = createContext<AuthContextType | null>(null)
const DEMO_USERS: LocalUser[] = [
  { nombre: 'Mateo Ríos', email: 'admin@cafedefinca.com', password: 'admin123', role: 'admin' },
  { nombre: 'Camila Vergara', email: 'cliente@cafedefinca.com', password: 'cliente123', role: 'cliente', tipoCliente: 'particular' },
  { nombre: 'Mayorista La Esquina', email: 'mayorista@cafedefinca.com', password: 'mayorista123', role: 'cliente', tipoCliente: 'mayorista' },
]

function getUsers(): LocalUser[] {
  try {
    const stored = localStorage.getItem('cafe-local-users')
    const registered = stored ? JSON.parse(stored) as LocalUser[] : []
    return [...DEMO_USERS, ...registered]
  } catch {
    return DEMO_USERS
  }
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(null)
  const [isReady, setIsReady] = useState(false)

  useEffect(() => {
    try {
      const stored = localStorage.getItem('cafe-local-session')
      if (stored) {
        const session = JSON.parse(stored) as SessionUser
        setUser({ ...session, tipoCliente: session.tipoCliente ?? 'particular' })
      }
    } catch {
      localStorage.removeItem('cafe-local-session')
    } finally {
      setIsReady(true)
    }
  }, [])

  function login(email: string, password: string) {
    const normalizedEmail = email.trim().toLowerCase()
    const found = getUsers().find((item) => item.email.toLowerCase() === normalizedEmail && item.password === password)
    if (!found) return { ok: false, mensaje: 'El correo o la contraseña son incorrectos.' }
    const session = { nombre: found.nombre, email: found.email, role: found.role, tipoCliente: found.tipoCliente ?? 'particular' }
    localStorage.setItem('cafe-local-session', JSON.stringify(session))
    setUser(session)
    return { ok: true, mensaje: 'Sesión iniciada correctamente.', role: found.role }
  }

  function register(nombre: string, email: string, password: string, tipoCliente: ClientType = 'particular') {
    const normalizedEmail = email.trim().toLowerCase()
    if (!nombre.trim()) return { ok: false, mensaje: 'Ingresá tu nombre o razón social.' }
    if (!normalizedEmail.includes('@')) return { ok: false, mensaje: 'Ingresá un correo válido.' }
    if (password.length < 6) return { ok: false, mensaje: 'La contraseña debe tener al menos 6 caracteres.' }

    const stored = localStorage.getItem('cafe-local-users')
    const registered = stored ? JSON.parse(stored) as LocalUser[] : []
    if (getUsers().some((item) => item.email.toLowerCase() === normalizedEmail)) {
      return { ok: false, mensaje: 'Ya existe una cuenta con ese correo.' }
    }
    const newUser: LocalUser = { nombre: nombre.trim(), email: normalizedEmail, password, role: 'cliente', tipoCliente }
    localStorage.setItem('cafe-local-users', JSON.stringify([...registered, newUser]))
    const session = { nombre: newUser.nombre, email: newUser.email, role: newUser.role, tipoCliente: newUser.tipoCliente }
    localStorage.setItem('cafe-local-session', JSON.stringify(session))
    setUser(session)
    return { ok: true, mensaje: 'Cuenta creada correctamente.', role: newUser.role }
  }

  function logout() {
    localStorage.removeItem('cafe-local-session')
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, isReady, login, register, logout }}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth debe usarse dentro de AuthProvider')
  return context
}

export function RoleGuard({ children }: { children: React.ReactNode }) {
  const { user, isReady } = useAuth()
  const pathname = usePathname()
  const router = useRouter()

  useEffect(() => {
    if (!isReady || !user || user.role === 'admin') return
    const allowed = pathname === '/compras' || pathname === '/cobros' || pathname === '/configuracion'
    if (!allowed) router.replace('/compras')
  }, [isReady, user, pathname, router])

  if (!isReady || !user) return null
  if (user.role === 'cliente' && !['/compras', '/cobros', '/configuracion'].includes(pathname)) return null
  return <>{children}</>
}

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const { user, isReady } = useAuth()
  const pathname = usePathname()
  const router = useRouter()

  useEffect(() => {
    if (isReady && !user && !pathname.startsWith('/login') && !pathname.startsWith('/registro')) {
      router.replace('/login')
    }
  }, [isReady, user, pathname, router])

  if (!isReady) return <div className="flex min-h-screen items-center justify-center bg-background text-sm text-muted-foreground">Cargando sesión…</div>
  if (!user) return null
  return <>{children}</>
}
