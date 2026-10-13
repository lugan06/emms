import { adminApi } from './admin'
import { cmsApi } from './cms'
import { systemApi } from './system'

// Read pagination totals without loading every record.
export async function loadDashboardStats(): Promise<Array<number | null>> {
  const results = await Promise.allSettled([
    adminApi.exhibitions({ page: 1, pageSize: 1 }),
    cmsApi.contents({ status: 'PUBLISHED', page: 1, pageSize: 1 }),
    systemApi.guestbooks({ status: 'UNREAD', page: 1, pageSize: 1 }),
  ])
  return results.map(result => result.status === 'fulfilled' ? result.value.total : null)
}
