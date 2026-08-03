/**
 * 輕量 fetch 封裝（book 模組新增功能，PG2）。
 *
 * <p>暫不引入 axios 等第三方 HTTP client 套件（避免非必要相依性），統一以瀏覽器原生
 * {@link fetch} 呼叫後端 REST API；所有請求皆以 {@code /api} 為前綴，開發時由
 * vite.config.ts 的 proxy 轉發至 backend（http://localhost:8080），正式環境則由
 * backend WAR 同源提供，故不需帶完整網域。
 */

export class ApiError extends Error {
  readonly status: number
  readonly body: unknown

  constructor(status: number, message: string, body: unknown) {
    super(message)
    this.status = status
    this.body = body
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`/api${path}`, {
    ...init,
    headers: {
      'Content-Type': 'application/json',
      ...init?.headers,
    },
  })

  if (!response.ok) {
    let body: unknown
    try {
      body = await response.json()
    } catch {
      body = null
    }
    const message =
      (body as { message?: string } | null)?.message ?? `請求失敗（HTTP ${response.status}）`
    throw new ApiError(response.status, message, body)
  }

  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export function apiGet<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'GET' })
}

export function apiPost<T>(path: string, body: unknown): Promise<T> {
  return request<T>(path, { method: 'POST', body: JSON.stringify(body) })
}
