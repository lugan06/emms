import { afterEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, reactive, type App } from 'vue'
import ElementPlus from 'element-plus'
import CoverImageUpload from './CoverImageUpload.vue'

let app: App | undefined
let container: HTMLDivElement | undefined

function mount(modelValue: string | null, disabled = false) {
  const state = reactive({ modelValue })
  const update = vi.fn((value: string | null) => { state.modelValue = value })
  container = document.createElement('div')
  document.body.append(container)
  app = createApp({
    render: () => h(CoverImageUpload, {
      modelValue: state.modelValue,
      disabled,
      'onUpdate:modelValue': update,
    }),
  })
  app.use(ElementPlus).mount(container)
  return { state, update }
}

afterEach(() => {
  app?.unmount()
  app = undefined
  container?.remove()
  container = undefined
  document.body.innerHTML = ''
  vi.restoreAllMocks()
})

describe('cover image upload clearing', () => {
  it('clears the bound image URL and preview', async () => {
    const { state, update } = mount('/uploads/company-qr.png')
    const clearButton = [...container!.querySelectorAll('button')]
      .find(button => button.textContent?.trim() === '清空图片')

    expect(clearButton).toBeDefined()
    expect(container!.querySelector('.el-image')).not.toBeNull()
    clearButton!.click()
    await nextTick()

    expect(update).toHaveBeenCalledWith(null)
    expect(state.modelValue).toBeNull()
    expect(container!.querySelector('.el-image')).toBeNull()
    expect(container!.textContent).not.toContain('清空图片')
  })

  it('hides and disables clearing when there is no editable image', () => {
    mount(null)
    expect(container!.textContent).not.toContain('清空图片')

    app!.unmount()
    app = undefined
    container!.remove()
    mount('/uploads/site-logo.png', true)
    const clearButton = [...container!.querySelectorAll('button')]
      .find(button => button.textContent?.trim() === '清空图片')
    expect(clearButton).toBeDefined()
    expect(clearButton!.disabled).toBe(true)
  })
})
