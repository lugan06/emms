<template>
  <div class="rich-editor">
    <Toolbar v-if="editor" :editor="editor" :default-config="toolbarConfig" mode="default" class="toolbar" />
    <div class="editor">
      <Editor :model-value="modelValue || ''" :default-config="editorConfig" mode="default" @update:model-value="emit('update:modelValue', $event)" @on-created="onCreated" />
    </div>
    <p v-if="uploadCount > 0" class="upload-hint">正文图片上传中，请稍候…</p>
  </div>
</template>

<script setup lang="ts">
import '@wangeditor/editor/dist/css/style.css'
import { onBeforeUnmount, shallowRef, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
import { uploadEditorImage } from '@/utils/image-upload'

defineProps<{ modelValue: string | null }>()
const emit = defineEmits<{ 'update:modelValue': [value: string]; 'uploading-change': [value: boolean] }>()
const editor = shallowRef<IDomEditor>()
const uploadCount = ref(0)
const toolbarConfig: Partial<IToolbarConfig> = {
  toolbarKeys: ['headerSelect', 'bold', 'italic', 'underline', 'color', 'bgColor', '|',
    'bulletedList', 'numberedList', 'blockquote', 'justifyLeft', 'justifyCenter', 'justifyRight', '|',
    'insertLink', 'uploadImage', 'insertTable', '|', 'undo', 'redo'],
}
const editorConfig: Partial<IEditorConfig> = {
  placeholder: '请输入正文，可通过工具栏上传图片',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file: File, insertFn: (url: string, alt: string, href: string) => void) {
        uploadCount.value += 1
        emit('uploading-change', true)
        try {
          const url = await uploadEditorImage(file)
          if (editor.value && !editor.value.isDestroyed) insertFn(url, file.name, '')
        } catch (error) {
          ElMessage.error(error instanceof Error ? error.message : '正文图片上传失败，请重试')
        } finally {
          uploadCount.value -= 1
          emit('uploading-change', uploadCount.value > 0)
        }
      },
    },
  },
}
function onCreated(value: IDomEditor): void { editor.value = value }
onBeforeUnmount(() => { editor.value?.destroy(); editor.value = undefined })
</script>

<style scoped>
.rich-editor { width: 100%; border: 1px solid #dcdfe6; border-radius: 4px; overflow: hidden; }
.toolbar { border-bottom: 1px solid #dcdfe6; }
.editor { height: 400px; overflow-y: hidden; }
.upload-hint { margin: 0; padding: 8px 12px; color: #2563eb; background: #eff6ff; }
</style>
