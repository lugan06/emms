<template>
  <el-select v-model="selected" multiple filterable clearable collapse-tags collapse-tags-tooltip
    :loading="loading" :disabled="disabled || loading" placeholder="选择授权站点，可留空"
    aria-label="授权站点" class="site-grant-select">
    <el-option v-for="site in options" :key="site.id" :value="site.id"
      :label="site.name + ' (' + site.code + ')' + (site.status === 'DISABLED' ? ' · 停用' : '')" />
  </el-select>
</template>

<script setup lang="ts">
import type { SiteOption } from '@/types/sites'
const selected = defineModel<number[]>({ required: true })
defineProps<{ options: SiteOption[]; loading?: boolean; disabled?: boolean }>()
</script>

<style scoped>
.site-grant-select { width: 100%; }
</style>

