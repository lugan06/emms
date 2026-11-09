import type { AdminCategory, CategorySaveRequest, ContentSaveRequest } from '@/types/admin'

export const categoryModelOptions = [
  { value: 'PAGE', label: '单页' }, { value: 'ARTICLE', label: '文章' },
  { value: 'PRODUCT', label: '产品' }, { value: 'CASE', label: '案例' },
  { value: 'LINK', label: '链接' }, { value: 'DIRECTORY', label: '目录' },
] as const

export function flattenCategories(tree: AdminCategory[]): AdminCategory[] {
  return tree.flatMap(node => [node, ...flattenCategories(node.children)])
}

export interface ContentCategoryOption extends AdminCategory {
  disabled: boolean
  children: ContentCategoryOption[]
}

export function contentCategoryOptions(tree: AdminCategory[]): ContentCategoryOption[] {
  return tree.map(node => ({ ...node, disabled: node.children.length > 0, children: contentCategoryOptions(node.children) }))
}

export function validateContentCategory(tree: AdminCategory[], categoryId: number | null): true | string {
  if (!categoryId || categoryId < 1) return '请选择所属栏目'
  const category = flattenCategories(tree).find(node => node.id === categoryId)
  if (!category) return '栏目不存在，请重新选择所属栏目'
  return category.children.length ? '该栏目有子栏目，请选择没有子栏目的末级栏目' : true
}

export function forbiddenParentIds(tree: AdminCategory[], editingId?: number): Set<number> {
  const node = flattenCategories(tree).find(item => item.id === editingId)
  return new Set(node ? flattenCategories([node]).map(item => item.id) : [])
}

export function validateCategoryParent(tree: AdminCategory[], parentId: number, editingId?: number): true | string {
  if (forbiddenParentIds(tree, editingId).has(parentId)) return '不能选择自身或子孙栏目作为父栏目'
  if (parentId !== 0 && !flattenCategories(tree).some(item => item.id === parentId)) return '请选择有效的父栏目'
  return true
}

export function createCategoryForm(parentId = 0): CategorySaveRequest {
  return { parentId, name: '', code: '', urlName: null, modelCode: 'ARTICLE', listTemplate: null,
    detailTemplate: null, sortOrder: 0, isEnabled: 1, showInNav: 1, linkUrl: null }
}

export function createContentForm(): ContentSaveRequest {
  return { categoryId: null, exhibitionId: null, title: '', slug: null, coverUrl: null, summary: null,
    body: '', author: null, source: null, sortOrder: 0, isTop: 0, isRecommend: 0, extraData: null }
}

export function validateContentForm(form: ContentSaveRequest, tree?: AdminCategory[]): true | string {
  if (!form.categoryId || form.categoryId < 1) return '请选择所属栏目'
  if (tree) {
    const valid = validateContentCategory(tree, form.categoryId)
    if (valid !== true) return valid
  }
  if (!form.title.trim()) return '请输入内容标题'
  if (form.title.length > 200) return '标题不能超过 200 字符'
  if (!Number.isInteger(form.sortOrder) || form.sortOrder < 0) return '排序值必须为非负整数'
  if (form.extraData?.trim()) {
    try { JSON.parse(form.extraData) } catch { return '扩展属性必须是有效的 JSON' }
  }
  return true
}
