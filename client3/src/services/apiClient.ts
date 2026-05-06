export type ApiResult<T> = {
  ok: boolean
  status: number
  data: T | null
  error: string
}

function resolveUrl(path: string): string {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path
  }

  const base = (import.meta.env.VITE_API_BASE_URL ?? '').trim()
  if (!base) {
    return path
  }

  const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base
  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `${normalizedBase}${normalizedPath}`
}

function extractErrorMessage(payload: unknown, fallback: string): string {
  if (payload && typeof payload === 'object') {
    const maybeError = (payload as Record<string, unknown>).error
    if (typeof maybeError === 'string' && maybeError.trim().length > 0) {
      return maybeError
    }

    const maybeDetail = (payload as Record<string, unknown>).detail
    if (typeof maybeDetail === 'string' && maybeDetail.trim().length > 0) {
      return maybeDetail
    }
  }

  if (typeof payload === 'string' && payload.trim().length > 0) {
    return payload
  }

  return fallback
}

export async function apiRequest<T>(
  path: string,
  init?: RequestInit,
): Promise<ApiResult<T>> {
  const url = resolveUrl(path)

  try {
    const response = await fetch(url, init)
    const raw = await response.text()

    let payload: unknown = null
    if (raw.trim().length > 0) {
      try {
        payload = JSON.parse(raw)
      } catch {
        payload = raw
      }
    }

    if (!response.ok) {
      const fallback = `HTTP ${response.status} ${response.statusText}`.trim()
      return {
        ok: false,
        status: response.status,
        data: null,
        error: extractErrorMessage(payload, fallback),
      }
    }

    return {
      ok: true,
      status: response.status,
      data: (payload as T) ?? null,
      error: '',
    }
  } catch (error) {
    return {
      ok: false,
      status: 0,
      data: null,
      error: error instanceof Error ? error.message : 'Unknown network error',
    }
  }
}
