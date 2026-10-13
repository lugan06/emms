<template>
  <el-tag :type="exhibitionStatusTagType(status)" effect="light">
    {{ exhibitionStatusLabel(status) }}
  </el-tag>
</template>

<script setup lang="ts">
import type { ExhibitionStatus } from '@/types/admin'
import { exhibitionStatusLabel, exhibitionStatusTagType } from '@/utils/exhibition'

defineProps<{
  status: ExhibitionStatus
}>()
</script>
