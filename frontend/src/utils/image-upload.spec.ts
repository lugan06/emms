import { afterEach, describe, expect, it, vi } from 'vitest'
const { uploadFile } = vi.hoisted(() => ({ uploadFile: vi.fn() }))
vi.mock('@/api/admin', () => ({ adminApi: { uploadFile } }))
import { uploadEditorImage, validateImage } from './image-upload'
afterEach(() => { vi.clearAllMocks(); vi.unstubAllEnvs() })
describe('editor image uploads', () => {
  it('inserts an accessible backend URL from a relative upload response', async () => {
    vi.stubEnv('VITE_API_BASE_URL', 'http://localhost:8080')
    const file = new File(['image'], 'image.png', { type: 'image/png' })
    uploadFile.mockResolvedValueOnce({ fileUrl: '/uploads/image.png' })
    await expect(uploadEditorImage(file)).resolves.toBe('http://localhost:8080/uploads/image.png')
    expect(uploadFile).toHaveBeenCalledWith(file)
  })
  it('rejects non-images and oversized files before sending a request', async () => {
    await expect(uploadEditorImage(new File(['text'], 'file.txt', { type: 'text/plain' }))).rejects.toThrow('只能上传图片')
    const large = new File([], 'large.png', { type: 'image/png' })
    Object.defineProperty(large, 'size', { value: 10 * 1024 * 1024 + 1 })
    expect(validateImage(large)).toBe('图片大小不能超过 10MB')
    expect(uploadFile).not.toHaveBeenCalled()
  })
  it('does not insert a URL on failed or malformed uploads', async () => {
    const file = new File([], 'image.png', { type: 'image/png' })
    uploadFile.mockRejectedValueOnce(new Error('上传失败'))
    await expect(uploadEditorImage(file)).rejects.toThrow('上传失败')
    uploadFile.mockResolvedValueOnce({ fileUrl: '' })
    await expect(uploadEditorImage(file)).rejects.toThrow('未返回图片地址')
  })
})
