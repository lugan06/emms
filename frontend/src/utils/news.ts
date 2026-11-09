import { publicApi } from '@/api/public'
import type { PublicContentList } from '@/types/public'

export const newsQuery = { includeChildren: true, sort: 'LATEST' } as const
export const newsRoute = { path: '/category/news', query: { includeChildren: 'true', sort: 'LATEST' } }

export async function loadAllNews(): Promise<{ items: PublicContentList[]; total: number }> {
  const pageSize = 100
  const first = await publicApi.categoryContents('news', { ...newsQuery, page: 1, pageSize })
  const records = [...first.records]
  // Retrieve every page instead of the limited home sections.
  for (let page = 2; page <= Math.ceil(first.total / pageSize); page++) {
    const result = await publicApi.categoryContents('news', { ...newsQuery, page, pageSize })
    records.push(...result.records)
  }
  const items = [...new Map(records.map(item => [item.id, item])).values()]
  items.sort((a, b) => (b.publishedAt || '').localeCompare(a.publishedAt || '') || b.id - a.id)
  return { items, total: first.total }
}
