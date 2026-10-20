const MAX_VIDEO_SIZE = 200 * 1024 * 1024

export function validateVideo(file: File): true | string {
  if (!/\.mp4$/i.test(file.name)) return '只能上传 MP4 视频文件'
  if (file.type && file.type.toLowerCase() !== 'video/mp4') return '视频 MIME 类型必须为 video/mp4'
  if (file.size > MAX_VIDEO_SIZE) return '视频大小不能超过 200MB'
  return true
}

export { MAX_VIDEO_SIZE }
