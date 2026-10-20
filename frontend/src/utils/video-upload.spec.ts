import { describe, expect, it } from 'vitest'
import { MAX_VIDEO_SIZE, validateVideo } from './video-upload'

describe('hero video upload validation', () => {
  it('accepts an MP4 file under the configured limit', () => {
    expect(validateVideo(new File(['video'], 'hero.mp4', { type: 'video/mp4' }))).toBe(true)
  })

  it('rejects non-MP4 extensions and MIME types', () => {
    expect(validateVideo(new File(['video'], 'hero.mov', { type: 'video/mp4' }))).toContain('MP4')
    expect(validateVideo(new File(['video'], 'hero.mp4', { type: 'video/quicktime' }))).toContain('MIME')
  })

  it('rejects videos larger than 200MB', () => {
    const file = new File([], 'hero.mp4', { type: 'video/mp4' })
    Object.defineProperty(file, 'size', { value: MAX_VIDEO_SIZE + 1 })
    expect(validateVideo(file)).toBe('视频大小不能超过 200MB')
  })
})
