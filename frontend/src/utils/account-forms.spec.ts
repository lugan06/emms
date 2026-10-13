import { describe, expect, it } from 'vitest'
import { createGuestbookForm, guestbookErrors, guestbookPayload, passwordErrors } from './account-forms'

describe('guestbook validation', () => {
  const valid = { ...createGuestbookForm(), name: '访客', message: '咨询展会信息' }

  it('rejects whitespace-only required fields and keeps optional fields optional', () => {
    expect(guestbookErrors({ ...valid, name: '  ', message: '\n' })).toEqual({ name: '请输入姓名', message: '请输入留言内容' })
    expect(guestbookErrors(valid)).toEqual({})
    expect(guestbookPayload({ ...valid, name: ' 访客 ', phone: ' ', email: ' ', message: ' 咨询展会信息 ' })).toEqual({
      name: '访客', message: '咨询展会信息', phone: null, email: null, companyName: null,
    })
  })

  it.each(['wrong-email', '@example.com', 'a b@example.com', 'a@@example.com', 'a@example'])('rejects invalid email %s', email => {
    expect(guestbookErrors({ ...valid, email }).email).toBeTruthy()
  })

  it('accepts email and formatted phone while enforcing backend length limits', () => {
    expect(guestbookErrors({ ...valid, email: 'guest@example.com', phone: '+86 (010) 12345678' })).toEqual({})
    expect(guestbookErrors({ ...valid, phone: 'abc123' }).phone).toBeTruthy()
    expect(guestbookErrors({ ...valid, name: '字'.repeat(101) }).name).toBeTruthy()
    expect(guestbookErrors({ ...valid, message: '字'.repeat(2001) }).message).toBeTruthy()
    expect(guestbookErrors({ ...valid, companyName: '字'.repeat(201) }).companyName).toBeTruthy()
  })
})

describe('password validation', () => {
  it.each([7, 101])('rejects a new password of length %s', length => {
    const password = 'a'.repeat(length)
    expect(passwordErrors({ oldPassword: 'old', newPassword: password, confirmPassword: password }).newPassword).toBeTruthy()
  })

  it.each([8, 100])('accepts the boundary length %s', length => {
    const password = 'a'.repeat(length)
    expect(passwordErrors({ oldPassword: 'old', newPassword: password, confirmPassword: password })).toEqual({})
  })

  it('requires the old password and exact confirmation without trimming passwords', () => {
    expect(passwordErrors({ oldPassword: ' ', newPassword: '        ', confirmPassword: '' })).toHaveProperty('oldPassword')
    expect(passwordErrors({ oldPassword: 'old', newPassword: ' abcdefgh ', confirmPassword: 'abcdefgh' }).confirmPassword).toBeTruthy()
    expect(passwordErrors({ oldPassword: ' old ', newPassword: ' abcdefgh ', confirmPassword: ' abcdefgh ' })).toEqual({})
  })
})
