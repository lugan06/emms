import { describe, expect, it } from 'vitest'
import { adminCreatePayload, createAdminUserForm, validateAdminCreate, validateAdminNickname, validateAdminPassword, validateAdminSites } from './users'
import type { SiteOption } from '@/types/sites'

const options: SiteOption[] = [
  { id: 1, name: 'A', code: 'a', status: 'ENABLED' },
  { id: 2, name: 'B', code: 'b', status: 'DISABLED' },
]
function valid() { return { username: ' editor ', nickname: ' Editor ', password: ' secure password ', confirmPassword: ' secure password ', siteIds: [1, 2] } }

describe('administrator forms', () => {
  it('normalizes profile values without trimming passwords or sending confirmation', () => {
    const form = valid()
    expect(validateAdminCreate(form, options)).toBe(true)
    expect(adminCreatePayload(form)).toEqual({ username: 'editor', nickname: 'Editor', password: ' secure password ', siteIds: [1, 2] })
    adminCreatePayload(form).siteIds.push(3)
    expect(form.siteIds).toEqual([1, 2])
    expect(createAdminUserForm().siteIds).toEqual([])
  })
  it.each(['', '_start', 'with space', 'a'.repeat(51)])('rejects invalid username %s', username => {
    expect(validateAdminCreate({ ...valid(), username }, options)).not.toBe(true)
  })
  it.each(['', '   ', '字'.repeat(101)])('rejects invalid nickname', nickname => {
    expect(validateAdminNickname(nickname)).not.toBe(true)
  })
  it.each(['', '       ', 'short', 'a'.repeat(73), '密'.repeat(25)])('rejects invalid or oversized BCrypt passwords', password => {
    expect(validateAdminPassword(password, password)).not.toBe(true)
  })
  it('requires matching passwords and accepts the exact UTF-8 limit', () => {
    expect(validateAdminPassword('valid-password', 'other-password')).toBe('两次输入的密码不一致')
    expect(validateAdminPassword('密'.repeat(24), '密'.repeat(24))).toBe(true)
    expect(validateAdminPassword('a'.repeat(72), 'a'.repeat(72))).toBe(true)
  })
  it('permits revoking all grants and granting disabled sites, but rejects invalid IDs', () => {
    expect(validateAdminSites([], options)).toBe(true)
    expect(validateAdminSites([1, 2], options)).toBe(true)
    for (const ids of [[1, 1], [3], [-1], [0], [1.5], [Number.MAX_SAFE_INTEGER + 1], Array(1001).fill(1)]) {
      expect(validateAdminSites(ids, options)).not.toBe(true)
    }
  })
})

