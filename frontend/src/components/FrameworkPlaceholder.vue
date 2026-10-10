<template>
  <section class="placeholder-page">
    <p class="eyebrow">{{ label }}</p>
    <h1>{{ title }}</h1>
    <p>{{ description }}</p>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  title: string
  description: string
}>()
</script>

<style scoped>
.placeholder-page {
  padding: 48px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
}

.eyebrow {
  margin: 0 0 12px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

h1 {
  margin: 0;
  color: #111827;
  font-size: 26px;
}

p:last-child {
  margin: 16px 0 0;
  color: #6b7280;
}
</style>
