<template>
  <div class="public-cover">
    <el-image v-if="resolvedSrc" :src="resolvedSrc" :alt="alt" :fit="fit" :lazy="lazy" class="cover-image">
      <template #error>
        <div class="cover-placeholder" role="img" :aria-label="alt">EEMS</div>
      </template>
    </el-image>
    <div v-else class="cover-placeholder" role="img" :aria-label="alt">EEMS</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { resolveAssetUrl } from '@/utils/asset'

const props = withDefaults(defineProps<{
  src?: string | null
  alt: string
  fit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down'
  lazy?: boolean
}>(), { fit: 'cover', lazy: false })

const resolvedSrc = computed(() => resolveAssetUrl(props.src))
</script>

<style scoped>
.public-cover,
.cover-image {
  display: block;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.cover-image {
  min-height: inherit;
  max-height: inherit;
}

.cover-placeholder {
  display: grid;
  width: 100%;
  height: 100%;
  min-height: inherit;
  color: var(--cover-placeholder-color, #ffffff);
  font-size: var(--cover-placeholder-size, 24px);
  font-weight: 700;
  background: var(--cover-placeholder-background, #334155);
  place-items: center;
}
</style>
