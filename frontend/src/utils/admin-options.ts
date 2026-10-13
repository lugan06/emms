import { adminApi } from '@/api/admin'
import type { AdminExhibition } from '@/types/admin'

// The backend caps pageSize at 100; fetch every page for complete dropdown options.
export async function loadExhibitionOptions(): Promise<AdminExhibition[]> {
  const records: AdminExhibition[] = []
  for (let page = 1; ; page += 1) {
    const result = await adminApi.exhibitions({ page, pageSize: 100 })
    records.push(...result.records)
    if (records.length >= result.total || result.records.length === 0) return records
  }
}
