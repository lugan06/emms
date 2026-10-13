import type { ExhibitionStatus } from '@/types/admin'

export const exhibitionStatusOptions: Array<{ label: string; value: ExhibitionStatus }> = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已下架', value: 'OFFLINE' },
]

export function exhibitionStatusLabel(status: ExhibitionStatus): string {
  return exhibitionStatusOptions.find((item) => item.value === status)?.label || status
}

export function exhibitionStatusTagType(status: ExhibitionStatus): 'success' | 'info' | 'warning' {
  if (status === 'PUBLISHED') {
    return 'success'
  }
  if (status === 'OFFLINE') {
    return 'info'
  }
  return 'warning'
}

export function validateExhibitionTime(startAt: string, endAt: string): string | true {
  if (!startAt || !endAt) {
    return true
  }

  const start = new Date(startAt.replace(' ', 'T')).getTime()
  const end = new Date(endAt.replace(' ', 'T')).getTime()
  if (Number.isNaN(start) || Number.isNaN(end) || start <= end) {
    return true
  }
  return '结束时间不能早于开始时间'
}

export function createExhibitionForm() {
  return {
    exhibitionCode: '',
    title: '',
    subtitle: null,
    year: undefined,
    edition: null,
    coverUrl: null,
    summary: null,
    description: null,
    venue: null,
    address: null,
    startAt: '',
    endAt: '',
    registrationUrl: null,
    contactName: null,
    contactPhone: null,
    showOnHome: 0,
    homeSort: 0,
    extraData: null,
  }
}
