const API_URL = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost:8080'

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, { cache: 'no-store' })
  if (!response.ok) throw new Error(await getErrorMessage(response))
  const data = await response.json() as T
  return normalizeMojibake(data)
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  })
  if (!response.ok) throw new Error(await getErrorMessage(response))
  const data = await response.json() as T
  return normalizeMojibake(data)
}

function normalizeMojibake<T>(value: T): T {
  if (typeof value === 'string') {
    if (!/[ÃÂ]/u.test(value)) return value
    try {
      const decoded = new TextDecoder('utf-8').decode(Uint8Array.from(value, (character) => character.charCodeAt(0)))
      return decoded as T
    } catch {
      return value
    }
  }
  if (Array.isArray(value)) return value.map((item) => normalizeMojibake(item)) as T
  if (value && typeof value === 'object') {
    return Object.fromEntries(Object.entries(value).map(([key, item]) => [key, normalizeMojibake(item)])) as T
  }
  return value
}

async function getErrorMessage(response: Response) {
  try {
    const data = (await response.json()) as { mensaje?: string }
    return data.mensaje ?? `Error HTTP ${response.status}`
  } catch {
    return `Error HTTP ${response.status}`
  }
}
