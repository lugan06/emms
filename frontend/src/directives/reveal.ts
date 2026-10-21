import type { ObjectDirective } from 'vue'

const pending = new Set<HTMLElement>()
let observer: IntersectionObserver | undefined
let reducedMotion: MediaQueryList | undefined

function releaseObserver(): void {
  if (pending.size) return
  observer?.disconnect()
  observer = undefined
  reducedMotion?.removeEventListener?.('change', onPreferenceChange)
  reducedMotion = undefined
}

function reveal(element: HTMLElement): void {
  element.classList.add('is-revealed')
  element.removeEventListener('focusin', onFocus)
  observer?.unobserve(element)
  pending.delete(element)
  releaseObserver()
}

function onFocus(event: Event): void {
  reveal(event.currentTarget as HTMLElement)
}

function onPreferenceChange(): void {
  if (reducedMotion?.matches) [...pending].forEach(reveal)
}

// Share one observer, and stop observing each element after its first appearance.
export const vReveal: ObjectDirective<HTMLElement, number | undefined> = {
  mounted(element, binding) {
    if (typeof IntersectionObserver === 'undefined'
      || window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) return

    if (!observer) {
      reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)')
      reducedMotion?.addEventListener?.('change', onPreferenceChange)
      observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting && pending.has(entry.target as HTMLElement)) reveal(entry.target as HTMLElement)
        })
      }, { threshold: 0, rootMargin: '0px 0px -40px 0px' })
    }

    const delay = Math.min(240, Math.max(0, Number(binding.value) || 0))
    element.style.setProperty('--reveal-delay', `${delay}ms`)
    element.classList.add('scroll-reveal')
    element.addEventListener('focusin', onFocus)
    pending.add(element)
    observer.observe(element)
  },
  beforeUnmount(element) {
    observer?.unobserve(element)
    pending.delete(element)
    element.removeEventListener('focusin', onFocus)
    element.classList.remove('scroll-reveal', 'is-revealed')
    element.style.removeProperty('--reveal-delay')
    releaseObserver()
  },
}
