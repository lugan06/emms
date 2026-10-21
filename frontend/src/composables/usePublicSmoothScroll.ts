import { onBeforeUnmount, onMounted } from 'vue'
import Lenis from 'lenis'
import 'lenis/dist/lenis.css'

export function usePublicSmoothScroll(): void {
  let lenis: Lenis | undefined
  let reducedMotion: MediaQueryList | undefined

  function updatePreference(): void {
    if (reducedMotion?.matches) {
      lenis?.destroy()
      lenis = undefined
      return
    }

    lenis ??= new Lenis({
      autoRaf: true,
      smoothWheel: true,
      syncTouch: false,
      lerp: 0.1,
      anchors: true,
      prevent: node => document.body.classList.contains('el-popup-parent--hidden')
        || Boolean(node.closest('textarea, .el-overlay, .el-scrollbar__wrap, [data-lenis-prevent]')),
    })
  }

  onMounted(() => {
    document.documentElement.classList.add('public-scroll')
    reducedMotion = window.matchMedia?.('(prefers-reduced-motion: reduce)')
    updatePreference()
    if (reducedMotion?.addEventListener) {
      reducedMotion.addEventListener('change', updatePreference)
    } else {
      reducedMotion?.addListener(updatePreference)
    }
  })

  onBeforeUnmount(() => {
    if (reducedMotion?.removeEventListener) {
      reducedMotion.removeEventListener('change', updatePreference)
    } else {
      reducedMotion?.removeListener(updatePreference)
    }
    lenis?.destroy()
    lenis = undefined
    document.documentElement.classList.remove('public-scroll')
  })
}
