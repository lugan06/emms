import { describe, expect, it } from 'vitest'
import { createCompanyForm, createSiteForm, isAdminMenuActive, isSuperAdmin, validateCompanyForm, validateLogRange, validateReply, validateSiteForm } from './system'
describe('system forms and permissions', () => {
  it('highlights the dashboard only at /admin and keeps detail routes in their module', () => {
    expect(isAdminMenuActive('/admin', '/admin')).toBe(true)
    for (const path of ['/admin/exhibitions', '/admin/categories', '/admin/contents/3/edit', '/admin/site']) expect(isAdminMenuActive(path, '/admin')).toBe(false)
    expect(isAdminMenuActive('/admin/contents/3/edit', '/admin/contents')).toBe(true)
    expect(isAdminMenuActive('/admin/contents-other', '/admin/contents')).toBe(false)
  })
  it('denies privileged access when the role is editor, missing or unknown', () => {
    const user = { id: 1, username: 'u', nickname: null, avatarUrl: null, roleCode: 'EDITOR' }
    expect(isSuperAdmin(user)).toBe(false); expect(isSuperAdmin(null)).toBe(false)
    expect(isSuperAdmin({ ...user, roleCode: 'ADMIN' })).toBe(false)
    expect(isSuperAdmin({ ...user, roleCode: 'SUPER_ADMIN' })).toBe(true)
  })
  it('requires site title and a published current exhibition, while allowing null', () => {
    const form = createSiteForm()
    expect(validateSiteForm(form, [3])).not.toBe(true)
    form.siteTitle = '站点'; expect(validateSiteForm(form, [3])).toBe(true)
    form.currentExhibitionId = 2; expect(validateSiteForm(form, [3])).not.toBe(true)
    form.currentExhibitionId = 3; expect(validateSiteForm(form, [3])).toBe(true)
  })
  it('validates company names and optional email', () => {
    const form = createCompanyForm()
    expect(validateCompanyForm(form)).not.toBe(true)
    form.companyName = '公司'; form.email = 'invalid'; expect(validateCompanyForm(form)).not.toBe(true)
    form.email = 'admin@example.com'; expect(validateCompanyForm(form)).toBe(true)
    form.email = null; expect(validateCompanyForm(form)).toBe(true)
  })
  it('rejects blank and oversized replies', () => {
    expect(validateReply('  ')).not.toBe(true)
    expect(validateReply('字'.repeat(2001))).not.toBe(true)
    expect(validateReply('已收到留言')).toBe(true)
  })
  it('rejects reversed log dates but allows equal or open-ended ranges', () => {
    expect(validateLogRange({ startAt: '2026-10-02 00:00:00', endAt: '2026-10-01 00:00:00' })).not.toBe(true)
    expect(validateLogRange({ startAt: '2026-10-01 00:00:00', endAt: '2026-10-01 00:00:00' })).toBe(true)
    expect(validateLogRange({ startAt: '2026-10-01 00:00:00' })).toBe(true)
  })
})
