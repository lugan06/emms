// The Vue wrapper ships declarations but its export map omits them.
declare module '@wangeditor/editor-for-vue' {
  import type { DefineComponent } from 'vue'
  import type { IDomEditor, IEditorConfig, IToolbarConfig } from '@wangeditor/editor'
  export const Editor: DefineComponent<{
    modelValue: string
    defaultConfig?: Partial<IEditorConfig>
    mode?: string
  }>
  export const Toolbar: DefineComponent<{
    editor: IDomEditor
    defaultConfig?: Partial<IToolbarConfig>
    mode?: string
  }>
}
