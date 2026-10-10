export interface ApiResponse<T> {
  code: number | string
  message: string
  data: T
  traceId?: string | null
}

export interface ApiErrorPayload {
  code?: number | string
  message?: string
  data?: unknown
}
