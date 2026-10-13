import { adminApi } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'

export function validateImage(file: File): true | string {
  if (!file.type.startsWith('image/')) return '只能上传图片文件'
  if (file.size > 10 * 1024 * 1024) return '图片大小不能超过 10MB'
  return true
}

export async function uploadEditorImage(file: File): Promise<string> {
  const valid = validateImage(file)
  if (valid !== true) throw new Error(valid)
  const result = await adminApi.uploadFile(file)
  if (!result.fileUrl) throw new Error('上传接口未返回图片地址')
  return resolveAssetUrl(result.fileUrl)
}
