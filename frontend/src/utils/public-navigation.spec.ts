import { describe, expect, it } from 'vitest'
import type { PublicCategory } from '@/types/public'
import { getCategoryInternalPath, isExternalCategoryLink } from './public-navigation'

const contact: PublicCategory = { id: 16, parentId: 0, name: '联系我们', code: 'contact', urlName: 'contact', modelCode: 'LINK', linkUrl: null, children: [] }
const pageExists = (path: string) => /^\/content\/\d+(?:\?|$)/.test(path)

describe('public category link destinations', () => {
  it('opens the designated contact detail even when the live configuration has not changed yet', () => {
    expect(getCategoryInternalPath(contact, pageExists)).toBe('/content/15')
    expect(getCategoryInternalPath({ ...contact, modelCode: 'PAGE' }, pageExists)).toBe('/content/15')
  })
  it('uses the configured internal LINK destination, including query parameters', () => {
    expect(getCategoryInternalPath({ ...contact, linkUrl: '/content/21?source=nav' }, pageExists)).toBe('/content/21?source=nav')
  })
  it('keeps unsupported registration links unavailable', () => {
    expect(getCategoryInternalPath({ ...contact, linkUrl: '/registration/exhibitor' }, pageExists)).toBeNull()
  })
  it('retains article list routing and safe external links', () => {
    expect(getCategoryInternalPath({ ...contact, code: 'news', urlName: 'news', modelCode: 'ARTICLE' }, pageExists)).toBe('/category/news')
    expect(getCategoryInternalPath({ ...contact, linkUrl: 'https://example.com' }, pageExists)).toBeNull()
    expect(isExternalCategoryLink('https://example.com')).toBe(true)
    expect(isExternalCategoryLink('javascript:alert(1)')).toBe(false)
  })
})
