<template>
  <div class="public-rich-text" v-html="html" />
</template>

<script setup lang="ts">
defineProps<{ html: string }>()
</script>

<style scoped>
.public-rich-text {
  color: #374151;
  font-size: 16px;
  line-height: 1.9;
  overflow-wrap: anywhere;
}

.public-rich-text :deep(p) {
  margin: 0 0 16px;
}

.public-rich-text :deep(p:last-child) {
  margin-bottom: 0;
}

.public-rich-text :deep(h2),
.public-rich-text :deep(h3) {
  margin: 24px 0 12px;
  color: #111827;
  line-height: 1.4;
}

.public-rich-text :deep(img) {
  max-width: 100%;
  height: auto;
}

.public-rich-text :deep(ul),
.public-rich-text :deep(ol) {
  padding-left: 24px;
}

.public-rich-text :deep(pre) {
  white-space: pre-wrap;
}

.public-rich-text :deep(table) {
  display: block;
  max-width: 100%;
  overflow-x: auto;
  border-collapse: collapse;
}

.public-rich-text :deep(td),
.public-rich-text :deep(th) {
  padding: 8px 12px;
  border: 1px solid #e5e7eb;
}
</style>
