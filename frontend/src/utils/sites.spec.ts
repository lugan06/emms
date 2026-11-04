import { describe, expect, it } from 'vitest'
import { createManagedSiteForm, normalizeManagedSite, validateManagedSite } from './sites'

function valid() {
  return { name: ' Expo ', code: 'expo', domains: [{ domain: ' EXPO.EXAMPLE ', primary: true }] }
}
describe('site management form', () => {
  it('starts with an empty form and exactly one primary row', () => {
    const form = createManagedSiteForm()
    expect(form.domains).toEqual([{ domain: '', primary: true }])
    expect(validateManagedSite(form)).not.toBe(true)
    form.name = 'A'
    expect(createManagedSiteForm().name).toBe('')
  })
  it('normalizes without mutating the draft and accepts aliases', () => {
    const form = valid()
    form.domains.push({ domain: 'Alias.Example', primary: false })
    expect(validateManagedSite(form)).toBe(true)
    expect(normalizeManagedSite(form)).toEqual({ name: 'Expo', code: 'expo',
      domains: [{ domain: 'expo.example', primary: true }, { domain: 'alias.example', primary: false }] })
    expect(form.name).toBe(' Expo ')
    expect(form.domains[0].domain).toBe(' EXPO.EXAMPLE ')
  })
  it.each(['https://expo.example', 'expo.example:80', 'expo.example/a', 'expo.example?q=a',
    'expo.example#hash', 'user@expo.example', '*.example', 'bad_.example', '-bad.example',
    'a..example', '中文.example', '[::1]', 'a.example.', '', 'a'.repeat(64) + '.example'])('rejects invalid domain %s', domain => {
    const form = valid(); form.domains[0].domain = domain
    expect(validateManagedSite(form)).not.toBe(true)
  })
  it('requires exactly one primary and distinct normalized domains', () => {
    const form = valid()
    form.domains[0].primary = false
    expect(validateManagedSite(form)).not.toBe(true)
    form.domains[0].primary = true
    form.domains.push({ domain: 'other.example', primary: true })
    expect(validateManagedSite(form)).not.toBe(true)
    form.domains[1] = { domain: 'expo.example', primary: false }
    expect(validateManagedSite(form)).toBe('绑定域名不能重复')
    form.domains = []
    expect(validateManagedSite(form)).not.toBe(true)
  })
  it('enforces name, code and domain count limits', () => {
    for (const code of ['', 'UPPER', '_start', 'has space', 'a'.repeat(51)]) {
      expect(validateManagedSite({ ...valid(), code })).not.toBe(true)
    }
    expect(validateManagedSite({ ...valid(), name: ' '.repeat(4) })).not.toBe(true)
    expect(validateManagedSite({ ...valid(), name: '字'.repeat(201) })).not.toBe(true)
    const form = valid()
    form.domains = Array.from({ length: 51 }, (_, i) => ({ domain: 'a' + i + '.example', primary: i === 0 }))
    expect(validateManagedSite(form)).not.toBe(true)
  })
})

