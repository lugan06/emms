<template>
  <main class="not-found">
    <h1>Page not found</h1>
    <RouterLink to="/">Back to home</RouterLink>
  </main>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<style scoped>
.not-found {
  display: grid;
  min-height: 100vh;
  place-content: center;
  gap: 16px;
  text-align: center;
}

h1 {
  margin: 0;
  color: #111827;
}

a {
  color: #2563eb;
}
</style>
