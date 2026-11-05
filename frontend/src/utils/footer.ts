export interface FooterInfo {
  organizer: string
  contractor: string
  phone: string
  contactName: string
  email: string
  copyrightOwner: string
  extraText: string
}

export function createFooterInfo(): FooterInfo {
  return { organizer: '', contractor: '', phone: '', contactName: '', email: '', copyrightOwner: '', extraText: '' }
}

const legacyFields: Record<string, keyof FooterInfo> = {
  主办单位: 'organizer', 承办单位: 'contractor', 电话: 'phone', 联系电话: 'phone',
  定展热线: 'phone', 联系人: 'contactName', 邮箱: 'email', 版权单位: 'copyrightOwner',
}

// footerInfo remains a string in the existing API. Preserve older free-form text on edit.
export function parseFooterInfo(value: string | null | undefined): FooterInfo {
  const result = createFooterInfo()
  if (!value?.trim()) return result
  try {
    const parsed: unknown = JSON.parse(value)
    if (parsed && typeof parsed === 'object' && 'version' in parsed && parsed.version === 1) {
      const fields = parsed as Record<string, unknown>
      for (const key of Object.keys(result) as Array<keyof FooterInfo>) {
        if (typeof fields[key] === 'string') result[key] = fields[key]
      }
      return result
    }
  } catch { /* Legacy footer text is not JSON. */ }

  const unrecognized: string[] = []
  for (const line of value.split(/[\r\n|｜]+/).map(part => part.trim()).filter(Boolean)) {
    const match = line.match(/^(主办单位|承办单位|联系电话|电话|定展热线|联系人|邮箱|版权单位)\s*[:：]\s*(.*)$/)
    if (match && !result[legacyFields[match[1]]]) result[legacyFields[match[1]]] = match[2].trim()
    else unrecognized.push(line)
  }
  result.extraText = unrecognized.join('\n')
  return result
}

export function serializeFooterInfo(form: FooterInfo): string {
  const fields = Object.fromEntries(Object.entries(form).map(([key, value]) => [key, value.trim()]))
  // An explicit empty string clears the existing nullable field even when null updates are ignored.
  return Object.values(fields).some(Boolean) ? JSON.stringify({ version: 1, ...fields }) : ''
}

export function validateFooterInfo(form: FooterInfo): true | string {
  if (form.email.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email.trim())) return '请输入有效的页脚邮箱地址'
  return true
}
