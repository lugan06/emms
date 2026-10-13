<template>
  <FrameworkPlaceholder
    label="ADMIN MODULE"
    :title="String(route.meta.module || route.meta.title || '管理模块')"
    description="该管理模块尚未开放，当前可先使用展会管理。"
  />
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'

import FrameworkPlaceholder from '@/components/FrameworkPlaceholder.vue'

const route = useRoute()
</script>
