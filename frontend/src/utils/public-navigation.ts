import type { PublicCategory } from '@/types/public'

export function isExternalCategoryLink(linkUrl?: string | null): linkUrl is string {
  return Boolean(linkUrl && /^(?:https?:\/\/|mailto:|tel:)/i.test(linkUrl.trim()))
}

export function getCategoryInternalPath(category: PublicCategory, pageExists: (path: string) => boolean): string | null {
  const link = category.linkUrl?.trim()
  if (link) {
    if (link.startsWith('/') && !link.startsWith('//') && pageExists(link)) return link
    // Configured links to unavailable pages retain the upcoming-feature feedback.
    return null
  }
  const keys = [category.code, category.urlName].map(value => value?.toLowerCase())
  if (keys.includes('contact')) return '/contact'
  if (keys.some(key => key && ['exhibition', 'exhibitions', 'exhibition-overview', 'overview'].includes(key))) {
    return '/exhibitions'
  }
  if (category.code && ['PAGE', 'ARTICLE'].includes(category.modelCode.toUpperCase())) {
    return `/category/${encodeURIComponent(category.code)}`
  }
  return null
}
