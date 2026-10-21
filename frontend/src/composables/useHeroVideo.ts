import { computed, onBeforeUnmount, onMounted, ref, watch, type Ref } from 'vue'

interface NetworkInformationLike {
  saveData?: boolean
  effectiveType?: string
  addEventListener?: (event: 'change', listener: () => void) => void
  removeEventListener?: (event: 'change', listener: () => void) => void
}

export function useHeroVideo(source: Readonly<Ref<string>>) {
  const heroVideo = ref<HTMLVideoElement | null>(null)
  const videoReady = ref(false)
  const videoFailed = ref(false)
  const prefersStaticHero = ref(false)
  const mediaQueries = typeof window.matchMedia === 'function'
    ? [window.matchMedia('(prefers-reduced-motion: reduce)'), window.matchMedia('(max-width: 760px)')]
    : []
  const connection = (navigator as Navigator & { connection?: NetworkInformationLike }).connection

  function updatePreference(): void {
    const [reducedMotion, mobileViewport] = mediaQueries
    prefersStaticHero.value = Boolean(reducedMotion?.matches)
      || (mobileViewport?.matches ?? window.innerWidth <= 760)
      || connection?.saveData === true
      || connection?.effectiveType === 'slow-2g'
      || connection?.effectiveType === '2g'
    if (prefersStaticHero.value) {
      videoReady.value = false
      heroVideo.value?.pause()
    }
  }

  updatePreference()
  const videoAllowed = computed(() => Boolean(source.value) && !prefersStaticHero.value && !videoFailed.value)

  watch(source, () => {
    videoReady.value = false
    videoFailed.value = false
  })

  function handleVideoCanPlay(): void {
    videoReady.value = videoAllowed.value
  }

  function handleVideoError(): void {
    videoReady.value = false
    videoFailed.value = true
  }

  onMounted(() => {
    mediaQueries.forEach(query => {
      if (typeof query.addEventListener === 'function') {
        query.addEventListener('change', updatePreference)
      } else {
        query.addListener(updatePreference)
      }
    })
    connection?.addEventListener?.('change', updatePreference)
    window.addEventListener('resize', updatePreference)
  })

  onBeforeUnmount(() => {
    mediaQueries.forEach(query => {
      if (typeof query.removeEventListener === 'function') {
        query.removeEventListener('change', updatePreference)
      } else {
        query.removeListener(updatePreference)
      }
    })
    connection?.removeEventListener?.('change', updatePreference)
    window.removeEventListener('resize', updatePreference)
  })

  return { heroVideo, videoReady, videoAllowed, handleVideoCanPlay, handleVideoError }
}
