<template>
  <header class="page-heading">
    <div>
      <p class="eyebrow">{{ eyebrow }}</p>
      <h1>{{ title }}</h1>
      <p class="description">{{ description }}</p>
    </div>
  </header>
</template>

<script setup lang="ts">
defineProps<{ eyebrow: string; title: string; description: string }>()
</script>

<style scoped>
h1 {
  margin: 0;
  color: #111827;
  font-size: 36px;
}

.description {
  margin: 12px 0 0;
  color: #6b7280;
}

.eyebrow {
  margin: 0 0 8px;
  color: #2563eb;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

@media (max-width: 620px) {
  h1 { font-size: 30px; }
}
</style>
