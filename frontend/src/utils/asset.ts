export function resolveAssetUrl(url: string | null | undefined): string {
  if (!url || /^(?:https?:)?\/\//i.test(url) || /^(?:data|blob):/i.test(url)) {
    return url || ''
  }

  const baseUrl = String(import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  return `${baseUrl}${url.startsWith('/') ? url : `/${url}`}`
}
