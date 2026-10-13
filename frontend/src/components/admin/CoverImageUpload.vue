<template>
  <div class="cover-field">
    <div class="upload-row">
      <el-upload accept="image/*" :show-file-list="false" :disabled="uploading || disabled" :before-upload="beforeUpload" :http-request="upload">
        <el-button type="primary" :icon="Upload" :loading="uploading" :disabled="disabled">上传图片</el-button>
      </el-upload>
      <span class="hint">支持图片格式，大小不超过 10MB</span>
    </div>
    <el-image v-if="modelValue" :src="previewUrl" :preview-src-list="[previewUrl]" fit="cover" class="preview">
      <template #error><span class="hint">图片无法加载</span></template>
    </el-image>
    <el-input :model-value="modelValue || ''" :disabled="disabled || uploading" maxlength="500" placeholder="也可以手动输入图片 URL" @update:model-value="emit('update:modelValue', $event || null)" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage, type UploadRawFile, type UploadRequestOptions } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin'
import { resolveAssetUrl } from '@/utils/asset'
import { validateImage } from '@/utils/image-upload'

const props = defineProps<{ modelValue: string | null; disabled?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [value: string | null]; 'uploading-change': [value: boolean] }>()
const uploading = ref(false)
const previewUrl = computed(() => resolveAssetUrl(props.modelValue))
function beforeUpload(file: UploadRawFile): boolean {
  const result = validateImage(file)
  if (result !== true) { ElMessage.error(result); return false }
  return true
}
async function upload(options: UploadRequestOptions): Promise<void> {
  uploading.value = true
  emit('uploading-change', true)
  try {
    const result = await adminApi.uploadFile(options.file)
    if (!result.fileUrl) throw new Error('上传接口未返回图片地址')
    emit('update:modelValue', result.fileUrl)
    ElMessage.success('封面图上传成功')
  } catch {
    ElMessage.error('封面图上传失败，请重试')
  } finally {
    uploading.value = false
    emit('uploading-change', false)
  }
}
</script>

<style scoped>
.cover-field { display: grid; gap: 12px; width: 100%; }
.upload-row { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; }
.hint { color: #9ca3af; font-size: 12px; }
.preview { width: 220px; height: 120px; border: 1px solid #e5e7eb; border-radius: 4px; }
</style>
