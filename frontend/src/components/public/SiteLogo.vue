<template>
  <img :src="source" :alt="title || 'EEMS'" @error="failed = true" />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import defaultLogo from '@/assets/default-logo.svg'
import { resolveAssetUrl } from '@/utils/asset'

const props = defineProps<{ logoUrl?: string | null; title?: string | null }>()
const failed = ref(false)
const source = computed(() => !failed.value && props.logoUrl?.trim() ? resolveAssetUrl(props.logoUrl) : defaultLogo)
watch(() => props.logoUrl, () => { failed.value = false })
</script>
