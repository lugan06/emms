import { describe, expect, it } from 'vitest'
import { createFooterInfo, parseFooterInfo, serializeFooterInfo, validateFooterInfo } from './footer'

describe('footer configuration', () => {
  it('round trips configured fields, trims values and clears an empty footer', () => {
    const form = { ...createFooterInfo(), organizer: ' 主办方甲 ', contractor: '承办方乙', phone: '13700000000',
      contactName: '联系人丙', email: ' contact@example.test ', copyrightOwner: '版权方丁', extraText: '其他说明' }
    expect(parseFooterInfo(serializeFooterInfo(form))).toEqual({ ...form, organizer: '主办方甲', email: 'contact@example.test' })
    expect(serializeFooterInfo(createFooterInfo())).toBe('')
    expect(parseFooterInfo(null)).toEqual(createFooterInfo())
  })

  it('migrates existing labeled text and preserves unknown text without HTML execution', () => {
    const legacy = '主办单位：中国设备管理协会 | 承办单位：上海励鑫展览有限公司\n定展热线：13764379748 ｜ 联系人：卢先生 | 邮箱：769285441@qq.com\n其他说明 <script>alert(1)</script>'
    const form = parseFooterInfo(legacy)
    expect(form).toEqual({ organizer: '中国设备管理协会', contractor: '上海励鑫展览有限公司', phone: '13764379748',
      contactName: '卢先生', email: '769285441@qq.com', copyrightOwner: '', extraText: '其他说明 <script>alert(1)</script>' })
    expect(parseFooterInfo(serializeFooterInfo(form))).toEqual(form)
  })

  it('preserves plain or malformed text and ignores non-string structured fields', () => {
    for (const value of ['自由页脚文字', '{malformed', '{"version":2,"organizer":"future"}']) {
      expect(parseFooterInfo(value).extraText).toBe(value)
    }
    expect(parseFooterInfo('{"version":1,"phone":123,"email":null,"organizer":"甲"}'))
      .toEqual({ ...createFooterInfo(), organizer: '甲' })
  })

  it('validates optional email', () => {
    expect(validateFooterInfo(createFooterInfo())).toBe(true)
    expect(validateFooterInfo({ ...createFooterInfo(), email: 'incorrect' })).not.toBe(true)
    expect(validateFooterInfo({ ...createFooterInfo(), email: ' contact@example.test ' })).toBe(true)
  })
})
