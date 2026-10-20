<template>
  <div class="hero-video-field">
    <div class="upload-row">
      <el-upload
        accept=".mp4,video/mp4"
        :show-file-list="false"
        :disabled="uploading || disabled"
        :before-upload="beforeUpload"
        :http-request="upload"
      >
        <el-button type="primary" :icon="Upload" :loading="uploading" :disabled="disabled">
          上传宣传视频
        </el-button>
      </el-upload>
      <el-button v-if="modelValue" text type="danger" :disabled="disabled || uploading" @click="clearVideo">
        清空视频
      </el-button>
      <span class="hint">仅支持 MP4，大小不超过 200MB</span>
    </div>
    <el-progress v-if="uploading" :percentage="progress" />
    <div v-if="modelValue" class="video-preview-row">
      <video :src="previewUrl" controls preload="metadata" class="video-preview" />
      <span class="hint video-url">{{ modelValue }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, type UploadRawFile, type UploadRequestOptions } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'
import { validateVideo } from '@/utils/video-upload'

const props = defineProps<{ modelValue: string | null; disabled?: boolean }>()
const emit = defineEmits<{
  'update:modelValue': [value: string | null]
  'uploading-change': [value: boolean]
}>()

const uploading = ref(false)
const progress = ref(0)
const previewUrl = computed(() => resolveAssetUrl(props.modelValue))

function beforeUpload(file: UploadRawFile): boolean {
  const result = validateVideo(file)
  if (result !== true) {
    ElMessage.error(result)
    return false
  }
  return true
}

async function upload(options: UploadRequestOptions): Promise<void> {
  uploading.value = true
  progress.value = 0
  emit('uploading-change', true)
  try {
    const result = await adminApi.uploadFile(options.file, value => { progress.value = value })
    if (!result.fileUrl) throw new Error('上传接口未返回视频地址')
    progress.value = 100
    emit('update:modelValue', result.fileUrl)
    ElMessage.success('首页宣传视频上传成功')
  } catch {
    ElMessage.error('首页宣传视频上传失败，请重试')
  } finally {
    uploading.value = false
    emit('uploading-change', false)
  }
}

function clearVideo(): void {
  emit('update:modelValue', null)
}
</script>

<style scoped>
.hero-video-field { display: grid; gap: 12px; width: 100%; }
.upload-row { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; }
.hint { color: #9ca3af; font-size: 12px; }
.video-preview-row { display: grid; gap: 8px; max-width: 560px; }
.video-preview { width: min(100%, 560px); max-height: 280px; background: #111827; border: 1px solid #e5e7eb; border-radius: 4px; }
.video-url { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
