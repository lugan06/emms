import { describe, expect, it } from 'vitest'

import { isLoginFormValid } from './validation'

describe('login form validation', () => {
  it('requires a username and password', () => {
    expect(isLoginFormValid({ username: '', password: 'secret' })).toBe(false)
    expect(isLoginFormValid({ username: 'admin', password: '' })).toBe(false)
    expect(isLoginFormValid({ username: ' admin ', password: 'secret' })).toBe(true)
  })
})
