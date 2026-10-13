import { describe, expect, it } from 'vitest'

import { isLoginFormValid } from './validation'
import { validateExhibitionTime } from './exhibition'

describe('login form validation', () => {
  it('requires a username and password', () => {
    expect(isLoginFormValid({ username: '', password: 'secret' })).toBe(false)
    expect(isLoginFormValid({ username: 'admin', password: '' })).toBe(false)
    expect(isLoginFormValid({ username: ' admin ', password: 'secret' })).toBe(true)
  })

  it('rejects an exhibition end time before its start time', () => {
    expect(validateExhibitionTime('2027-05-12 09:00:00', '2027-05-12 16:00:00')).toBe(true)
    expect(validateExhibitionTime('2027-05-13 09:00:00', '2027-05-12 16:00:00')).toBe('结束时间不能早于开始时间')
  })
})
