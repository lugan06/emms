export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '待定'
  }

  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const date = new Date(normalized)
  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}

export function formatDateRange(startAt: string | null | undefined, endAt: string | null | undefined): string {
  if (!startAt && !endAt) {
    return '时间待定'
  }
  if (!endAt) {
    return formatDateTime(startAt)
  }
  if (!startAt) {
    return formatDateTime(endAt)
  }

  const start = formatDateTime(startAt)
  const end = formatDateTime(endAt)
  return `${start} - ${end}`
}
