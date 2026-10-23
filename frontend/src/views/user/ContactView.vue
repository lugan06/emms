<template>
  <div class="contact-view">
    <section class="contact-panel">
      <h1>联系我们</h1>
      <el-skeleton v-if="loading" :rows="5" animated />
      <el-result v-else-if="error" icon="error" title="联系信息加载失败" sub-title="请稍后重试">
        <template #extra><el-button type="primary" @click="loadCompany">重新加载</el-button></template>
      </el-result>
      <template v-else-if="company">
        <h2>{{ company.companyName }}</h2>
        <dl class="contact-details">
          <div v-if="company.contactName"><dt>联系人</dt><dd>{{ company.contactName }}</dd></div>
          <div v-if="company.mobile"><dt>手机号</dt><dd><a :href="`tel:${company.mobile}`">{{ company.mobile }}</a></dd></div>
          <div v-if="company.telephone"><dt>联系电话</dt><dd>{{ company.telephone }}</dd></div>
          <div v-if="company.email"><dt>邮箱</dt><dd>{{ company.email }}</dd></div>
          <div v-if="company.address"><dt>地址</dt><dd>{{ company.address }}</dd></div>
          <div v-if="company.postalCode"><dt>邮编</dt><dd>{{ company.postalCode }}</dd></div>
        </dl>
        <el-image v-if="company.wechatImageUrl" :src="resolveAssetUrl(company.wechatImageUrl)" alt="联系微信二维码" class="wechat-image" fit="contain" />
      </template>
      <el-empty v-else description="暂无联系信息" />
    </section>
    <PublicGuestbookForm />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { publicApi } from '@/api/public'
import PublicGuestbookForm from '@/components/public/PublicGuestbookForm.vue'
import type { PublicCompany } from '@/types/public'
import { resolveAssetUrl } from '@/utils/asset'

const company = ref<PublicCompany | null>(null)
const loading = ref(true)
const error = ref(false)

async function loadCompany(): Promise<void> {
  loading.value = true
  error.value = false
  try {
    company.value = await publicApi.company()
  } catch {
    error.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => { void loadCompany() })
</script>

<style scoped>
.contact-view { display: grid; gap: 24px; }
.contact-panel { padding: 36px; background: #ffffff; border: 1px solid #e5e7eb; border-radius: 8px; }
h1 { margin: 0 0 24px; color: #111827; font-size: 28px; }
h2 { margin: 0 0 20px; color: #1f2937; font-size: 20px; }
.contact-details { display: grid; gap: 16px; margin: 0; line-height: 1.7; }
.contact-details div { display: flex; gap: 20px; }
dt { flex: 0 0 70px; color: #6b7280; }
dd { margin: 0; color: #374151; overflow-wrap: anywhere; }
dd a { color: inherit; text-decoration: none; }
dd a:hover { color: #2563eb; }
.wechat-image { width: 160px; height: 160px; margin-top: 24px; }
.contact-view :deep(.guestbook-section) { width: 100%; padding-bottom: 0; }
@media (max-width: 620px) { .contact-panel { padding: 24px 18px; } }
</style>
