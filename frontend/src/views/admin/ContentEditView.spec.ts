import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createApp, h, nextTick, type App } from 'vue'
import { createRouter, createMemoryHistory } from 'vue-router'
import ElementPlus from 'element-plus'
import ContentEditView from './ContentEditView.vue'
import { createCategoryForm, createContentForm } from '@/utils/cms'
import type { AdminCategory } from '@/types/admin'

const { categoryTree, content, createContent, updateContent, options } = vi.hoisted(() => ({
  categoryTree: vi.fn(), content: vi.fn(), createContent: vi.fn(), updateContent: vi.fn(), options: vi.fn(),
}))
vi.mock('@/api/cms', () => ({ cmsApi: { categoryTree, content, createContent, updateContent } }))
vi.mock('@/utils/admin-options', () => ({ loadExhibitionOptions: options }))
vi.mock('@/components/admin/CoverImageUpload.vue', () => ({ default: { template: '<div />' } }))
vi.mock('@/components/admin/ContentRichEditor.vue', () => ({ default: { template: '<div />' } }))

function node(id: number, children: AdminCategory[] = []): AdminCategory {
  return { ...createCategoryForm(), id, name: `栏目${id}`, createdAt: '', updatedAt: '', children }
}
let app: App | undefined
let container: HTMLDivElement
beforeEach(() => {
  vi.resetAllMocks()
  categoryTree.mockResolvedValue([node(1, [node(2, [node(3)])]), node(4)])
  options.mockResolvedValue([])
  content.mockResolvedValue({ ...createContentForm(), id: 7, categoryId: 1, title: '历史内容' })
})
afterEach(() => { app?.unmount(); app = undefined; container?.remove(); document.body.innerHTML = '' })

async function mount(editing: boolean) {
  const router = createRouter({ history: createMemoryHistory(), routes: [
    { path: '/admin/contents/new', component: { template: '<div />' } },
    { path: '/admin/contents/:id/edit', component: { template: '<div />' } },
    { path: '/admin/contents', name: 'admin-contents', component: { template: '<div />' } },
  ] })
  await router.push(editing ? '/admin/contents/7/edit' : '/admin/contents/new')
  await router.isReady()
  container = document.createElement('div'); document.body.append(container)
  app = createApp({ render: () => h(ContentEditView) })
  app.use(router).use(ElementPlus).mount(container)
  await vi.waitFor(() => expect(container.querySelector('form')).not.toBeNull())
}
async function submit() {
  container.querySelector('form')!.dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }))
  await nextTick()
}
async function treeNode(id: number) {
  await vi.waitFor(() => expect([...document.querySelectorAll('.el-tree-select__popper .el-select-dropdown__item')].some(el => el.textContent === `栏目${id}`)).toBe(true))
  return [...document.querySelectorAll<HTMLElement>('.el-tree-select__popper .el-select-dropdown__item')].find(el => el.textContent === `栏目${id}`)!
}
async function selectLeaf() {
  container.querySelector<HTMLElement>('.el-select__wrapper')!.click(); await nextTick()
  const root = await treeNode(1)
  expect(root.closest('.el-select-dropdown__item')?.classList.contains('is-disabled')).toBe(true)
  root.click(); await nextTick()
  const branch = await treeNode(2)
  expect(branch.closest('.el-select-dropdown__item')?.classList.contains('is-disabled')).toBe(true)
  branch.click(); await nextTick()
  const leaf = await treeNode(3)
  expect(leaf.closest('.el-select-dropdown__item')?.classList.contains('is-disabled')).toBe(false)
  leaf.click(); await nextTick()
}

describe('leaf-only content category selection', () => {
  it('expands disabled parents and creates content in a selectable descendant', async () => {
    await mount(false)
    await submit()
    await vi.waitFor(() => expect(container.textContent).toContain('请选择所属栏目'))
    expect(createContent).not.toHaveBeenCalled()
    await selectLeaf()
    const titleItem = [...container.querySelectorAll('.el-form-item')].find(el => el.querySelector('label')?.textContent === '标题')!
    const input = titleItem.querySelector('input')!
    input.value = '新增内容'; input.dispatchEvent(new Event('input', { bubbles: true })); await nextTick()
    await submit()
    await vi.waitFor(() => expect(createContent).toHaveBeenCalledWith(expect.objectContaining({ categoryId: 3, title: '新增内容' })))
  })

  it('blocks saving legacy parent assignments until a leaf is selected', async () => {
    await mount(true)
    expect(container.querySelector('.el-alert')?.textContent).toContain('该栏目有子栏目')
    await submit()
    await vi.waitFor(() => expect(container.querySelector('.el-form-item__error')?.textContent).toContain('该栏目有子栏目'))
    expect(updateContent).not.toHaveBeenCalled()
    await selectLeaf()
    await submit()
    await vi.waitFor(() => expect(updateContent).toHaveBeenCalledWith(7, expect.objectContaining({ categoryId: 3, title: '历史内容' })))
    expect(createContent).not.toHaveBeenCalled()
  })
})
