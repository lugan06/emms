import { describe, expect, it, vi } from 'vitest'

import { resolveAssetUrl } from './asset'

describe('resolveAssetUrl', () => {
  it('prefixes relative resource paths with the API base URL', () => {
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080')

    expect(resolveAssetUrl('/uploads/cover.jpg')).toBe('http://localhost:8080/uploads/cover.jpg')
    expect(resolveAssetUrl('uploads/cover.jpg')).toBe('http://localhost:8080/uploads/cover.jpg')
  })

  it('keeps absolute and browser-generated URLs unchanged', () => {
    expect(resolveAssetUrl('https://cdn.example.com/cover.jpg')).toBe('https://cdn.example.com/cover.jpg')
    expect(resolveAssetUrl('//cdn.example.com/cover.jpg')).toBe('//cdn.example.com/cover.jpg')
    expect(resolveAssetUrl('data:image/png;base64,abc')).toBe('data:image/png;base64,abc')
  })
})
