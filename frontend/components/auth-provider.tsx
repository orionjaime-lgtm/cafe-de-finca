'use client'

import { createContext, useContext, useEffect, useState } from 'react'
import { usePathname, useRouter } from 'next/navigation'
import { apiPost } from '@/lib/api'

export type UserRole = 'admin' | 'tueste' | 'vendedor' | 'cliente'
type SessionUser = { idUsuario: number; nombre: string; email: string; role: UserRole; idCliente?: number | null; secciones: string[]; tipoCliente?: 'particular' | 'mayorista'; token?: string }
type AuthResult = { ok: boolean; mensaje: string; role?: UserRole }
type ApiSession = { idUsuario: number; nombre: string; email: string; rol: 'ADMIN' | 'TUESTE' | 'VENDEDOR' | 'CLIENTE'; idCliente: number | null; secciones: string[]; token: string }
type AuthContextType = {
  user: SessionUser | null
  isReady: boolean
  login: (email: string, password: string) => Promise<AuthResult>
  register: (datos: { nombre: string; documento: string; email: string; password: string; confirmacion: string; contacto?: string }) => Promise<AuthResult>
  logout: () => void
}

const AuthContext = createContext<AuthContextType | null>(null)
const SESSION_KEY = 'cafe-api-session'

export function obtenerRutaInicio(secciones: string[], role: UserRole): string {
  const rutas: Record<string, string> = {
    alertas: '/',
    inventario: '/inventario',
    tueste: '/tueste',
    pedidos: '/pedidos',
    cobros: '/cobros',
    caducidad: '/caducidad',
    clientes: '/clientes',
    compras: '/compras',
    configuracion: '/configuracion',
  }
  if (role === 'vendedor') return rutas.pedidos
  if (role === 'cliente') return rutas.compras
  if (role === 'tueste') return rutas[secciones.find((seccion) => seccion in rutas) ?? 'alertas'] ?? '/'
  return rutas[secciones.find((seccion) => seccion in rutas) ?? 'alertas'] ?? '/'
}

export function obtenerNombreRol(role: UserRole): string {
  return { admin: 'Administrador', tueste: 'Operador de tueste', vendedor: 'Vendedor', cliente: 'Cliente' }[role]
}

function normalizarRol(rol: ApiSession['rol']): UserRole {
  return rol.toLowerCase() as UserRole
}

function guardarSesion(data: ApiSession): SessionUser {
  const session: SessionUser = {
    idUsuario: data.idUsuario,
    nombre: data.nombre,
    email: data.email,
    role: normalizarRol(data.rol),
    idCliente: data.idCliente,
    secciones: data.secciones,
    tipoCliente: 'particular',
    token: data.token,
  }
  localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  return session
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<SessionUser | null>(null)
  const [isReady, setIsReady] = useState(false)

  useEffect(() => {
    try {
      const stored = localStorage.getItem(SESSION_KEY)
      if (stored) setUser(JSON.parse(stored) as SessionUser)
    } catch {
      localStorage.removeItem(SESSION_KEY)
    } finally {
      setIsReady(true)
    }

    function invalidarSesion() {
      localStorage.removeItem(SESSION_KEY)
      setUser(null)
      window.location.replace('/login')
    }
    window.addEventListener('cafe:sesion-invalida', invalidarSesion)
    return () => window.removeEventListener('cafe:sesion-invalida', invalidarSesion)
  }, [])

  async function login(email: string, password: string): Promise<AuthResult> {
    try {
      const data = await apiPost<ApiSession>('/api/auth/login', { email: email.trim(), password })
      const session = guardarSesion(data)
      setUser(session)
      return { ok: true, mensaje: 'Sesión iniciada correctamente.', role: session.role }
    } catch (error) {
      return { ok: false, mensaje: error instanceof Error ? error.message : 'No se pudo iniciar sesión.' }
    }
  }

  async function register(datos: { nombre: string; documento: string; email: string; password: string; confirmacion: string; contacto?: string }): Promise<AuthResult> {
    try {
      const data = await apiPost<ApiSession>('/api/auth/registro', datos)
      const session = guardarSesion(data)
      setUser(session)
      return { ok: true, mensaje: 'Cuenta creada correctamente.', role: session.role }
    } catch (error) {
      return { ok: false, mensaje: error instanceof Error ? error.message : 'No se pudo crear la cuenta.' }
    }
  }

  function logout() {
    localStorage.removeItem(SESSION_KEY)
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
    if (!isReady || !user) return
    const ruta = pathname === '/' ? 'alertas' : pathname.replace(/^\//, '').split('/')[0]
    if (!user.secciones.includes(ruta)) router.replace(obtenerRutaInicio(user.secciones, user.role))
  }, [isReady, user, pathname, router])

  if (!isReady || !user) return null
  const ruta = pathname === '/' ? 'alertas' : pathname.replace(/^\//, '').split('/')[0]
  if (!user.secciones.includes(ruta)) return null
  return <>{children}</>
}

export function AuthGuard({ children }: { children: React.ReactNode }) {
  const { user, isReady } = useAuth()
  const pathname = usePathname()
  const router = useRouter()

  useEffect(() => {
    if (isReady && !user && !pathname.startsWith('/login') && !pathname.startsWith('/registro')) router.replace('/login')
  }, [isReady, user, pathname, router])

  if (!isReady) return <div className="flex min-h-screen items-center justify-center bg-background text-sm text-muted-foreground">Cargando sesión…</div>
  if (!user) return null
  return <>{children}</>
}
