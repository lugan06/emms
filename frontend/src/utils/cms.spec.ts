import { describe, expect, it } from 'vitest'
import type { AdminCategory } from '@/types/admin'
import { createCategoryForm, createContentForm, forbiddenParentIds, validateCategoryParent, validateContentForm } from './cms'

function node(id: number, parentId: number, children: AdminCategory[] = []): AdminCategory {
  return { ...createCategoryForm(parentId), id, name: `栏目${id}`, createdAt: '', updatedAt: '', children }
}
describe('category cycle prevention', () => {
  const tree = [node(1, 0, [node(2, 1, [node(3, 2)])]), node(4, 0)]
  it('blocks self and every descendant while permitting top level and other branches', () => {
    expect([...forbiddenParentIds(tree, 1)]).toEqual([1, 2, 3])
    for (const id of [1, 2, 3]) expect(validateCategoryParent(tree, id, 1)).not.toBe(true)
    expect(validateCategoryParent(tree, 0, 1)).toBe(true)
    expect(validateCategoryParent(tree, 4, 1)).toBe(true)
    expect(validateCategoryParent(tree, 99, 1)).not.toBe(true)
  })
  it('does not block descendants of an unrelated branch', () => {
    expect([...forbiddenParentIds(tree, 2)]).toEqual([2, 3])
    expect(validateCategoryParent(tree, 1, 2)).toBe(true)
  })
})
describe('content form validation', () => {
  it('requires category and non-whitespace title', () => {
    const form = createContentForm()
    expect(validateContentForm(form)).toBe('请选择所属栏目')
    form.categoryId = 2
    form.title = '  '
    expect(validateContentForm(form)).toBe('请输入内容标题')
    form.title = '真实内容'
    expect(validateContentForm(form)).toBe(true)
  })
  it('rejects invalid sort values and JSON without losing valid optional data', () => {
    const form = { ...createContentForm(), categoryId: 2, title: '文章' }
    form.sortOrder = -1
    expect(validateContentForm(form)).not.toBe(true)
    form.sortOrder = 0
    form.extraData = '{'
    expect(validateContentForm(form)).toBe('扩展属性必须是有效的 JSON')
    form.extraData = '{"key":"value"}'
    expect(validateContentForm(form)).toBe(true)
  })
})
