import { afterEach, describe, expect, it } from 'vitest'
import { createApp, h, nextTick, reactive, type App } from 'vue'
import ElementPlus from 'element-plus'
import SiteGrantSelect from './SiteGrantSelect.vue'

let app: App | undefined
let container: HTMLDivElement
afterEach(() => { app?.unmount(); container?.remove(); document.body.innerHTML = '' })

describe('site grant multi-select', () => {
  it('selects multiple sites, labels disabled sites and preserves IDs when options refresh', async () => {
    const state = reactive({ selected: [] as number[], options: [
      { id: 1, name: 'Site A', code: 'a', status: 'ENABLED' as const },
      { id: 2, name: 'Site B', code: 'b', status: 'DISABLED' as const },
    ], disabled: false })
    container = document.createElement('div'); document.body.append(container)
    app = createApp({ render: () => h(SiteGrantSelect, { modelValue: state.selected, options: state.options, disabled: state.disabled,
      'onUpdate:modelValue': (ids: number[]) => { state.selected = ids } }) })
    app.use(ElementPlus).mount(container)
    const trigger = container.querySelector<HTMLElement>('.el-select__wrapper')!
    trigger.click(); await nextTick()
    const items = [...document.querySelectorAll<HTMLElement>('.el-select-dropdown__item')]
    expect(items).toHaveLength(2)
    expect(items[1].textContent).toContain('停用')
    items[0].click(); await nextTick()
    items[1].click(); await nextTick()
    expect(state.selected).toEqual([1, 2])
    state.options[0].name = 'Renamed'
    await nextTick()
    expect(state.selected).toEqual([1, 2])
    expect(container.textContent).toContain('Renamed')
    state.disabled = true; await nextTick()
    expect(container.querySelector('.el-select__wrapper')?.classList.contains('is-disabled')).toBe(true)
  })
})

