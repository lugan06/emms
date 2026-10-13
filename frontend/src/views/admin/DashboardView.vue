<template>
  <div class="cms-page">
    <header class="cms-heading"><div><h1>控制台</h1><p>查看当前业务数据，快速进入管理模块。</p></div><el-button :loading="loading" @click="load">刷新统计</el-button></header>
    <div class="stats-grid">
      <section v-for="(card, index) in cards" :key="card.path" class="cms-panel stat-card">
        <el-skeleton v-if="loading" :rows="2" animated />
        <el-statistic v-else-if="counts[index] !== null" :title="card.title" :value="counts[index] ?? 0" />
        <div v-else><p>{{ card.title }}</p><el-text type="danger">加载失败</el-text></div>
        <el-button link type="primary" @click="router.push(card.path)">进入{{ card.linkLabel }} →</el-button>
      </section>
    </div>
    <el-alert v-if="!loading && counts.some(value => value === null)" title="部分统计加载失败，请刷新重试" type="warning" :closable="false" />
    <section class="cms-panel"><h2 class="cms-section-title">快捷入口</h2><div class="shortcuts"><el-button @click="router.push('/admin/categories')">栏目管理</el-button><el-button @click="router.push('/admin/site')">站点配置</el-button><el-button @click="router.push('/admin/company')">公司信息</el-button><el-button v-if="isSuperAdmin(user.user)" @click="router.push('/admin/logs')">日志管理</el-button></div></section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loadDashboardStats } from '@/api/dashboard'
import { useUserStore } from '@/stores/user'
import { isSuperAdmin } from '@/utils/system'

const router = useRouter(), user = useUserStore()
const loading = ref(true)
const counts = ref<Array<number | null>>([null, null, null])
const cards = [
  { title: '展会总数', path: '/admin/exhibitions', linkLabel: '展会管理' },
  { title: '已发布内容', path: '/admin/contents', linkLabel: '内容管理' },
  { title: '未读留言', path: '/admin/guestbooks', linkLabel: '留言管理' },
]
async function load(): Promise<void> { loading.value = true; try { counts.value = await loadDashboardStats() } finally { loading.value = false } }
onMounted(load)
</script>

<style scoped>
@import '@/styles/admin-cms.css';
.stats-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 20px; }
.stat-card { display: grid; gap: 24px; }
.stat-card .el-button { justify-self: start; margin: 0; }
.shortcuts { display: flex; gap: 12px; flex-wrap: wrap; }
.shortcuts .el-button { margin: 0; }
@media (max-width: 900px) { .stats-grid { grid-template-columns: 1fr; } }
</style>
