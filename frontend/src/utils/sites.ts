import type { SiteManagementSave } from '@/types/sites'

export function createManagedSiteForm(): SiteManagementSave {
  return { name: '', code: '', domains: [{ domain: '', primary: true }] }
}

export function normalizeManagedSite(form: SiteManagementSave): SiteManagementSave {
  return { name: form.name.trim(), code: form.code.trim(),
    domains: form.domains.map(item => ({ domain: item.domain.trim().toLowerCase(), primary: item.primary })) }
}

export function validateManagedSite(form: SiteManagementSave): true | string {
  const value = normalizeManagedSite(form)
  if (!value.name || value.name.length > 200) return '请输入 1 至 200 字的站点名称'
  if (!/^[a-z0-9][a-z0-9_-]{0,49}$/.test(value.code)) return '编码须为 1 至 50 位小写字母、数字、下划线或连字符，且以字母或数字开头'
  if (!value.domains.length || value.domains.length > 50) return '请填写 1 至 50 个绑定域名'
  if (value.domains.filter(item => item.primary).length !== 1) return '必须且只能设置一个主域名'
  const unique = new Set<string>()
  for (const item of value.domains) {
    if (!item.domain || item.domain.length > 253 ||
      !item.domain.split('.').every(label => /^[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?$/.test(label))) {
      return '域名格式无效，请填写不含协议、端口或路径的 ASCII 域名'
    }
    if (unique.has(item.domain)) return '绑定域名不能重复'
    unique.add(item.domain)
  }
  return true
}

