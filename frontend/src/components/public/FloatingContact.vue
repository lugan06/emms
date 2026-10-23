<template>
  <div
    ref="contactRoot"
    class="floating-contact"
    :class="{ 'is-open': isOpen }"
    @mouseenter="openPanel"
    @mouseleave="closePanel"
  >
    <button
      class="contact-toggle"
      type="button"
      aria-controls="floating-contact-panel"
      :aria-expanded="isOpen"
      aria-label="打开联系我们"
      @click.stop="togglePanel"
    >
      <el-icon><ChatDotRound /></el-icon>
      <span>联系我们</span>
    </button>

    <aside
      id="floating-contact-panel"
      class="contact-panel"
      aria-label="联系我们"
      @click.stop
    >
      <div class="contact-panel-header">
        <div>
          <p class="contact-eyebrow">CONTACT</p>
          <h2>联系我们</h2>
        </div>
        <button class="panel-close" type="button" aria-label="关闭联系我们" @click="closePanel">
          <el-icon><Close /></el-icon>
        </button>
      </div>

      <el-skeleton v-if="publicStore.companyLoading" :rows="4" animated />
      <template v-else-if="publicStore.company">
        <p v-if="publicStore.company.companyName" class="company-name">{{ publicStore.company.companyName }}</p>

        <dl class="contact-list">
          <div v-if="publicStore.company.telephone" class="contact-item">
            <dt><el-icon><Phone /></el-icon><span>电话</span></dt>
            <dd><a :href="`tel:${publicStore.company.telephone}`">{{ publicStore.company.telephone }}</a></dd>
          </div>
          <div v-if="publicStore.company.mobile" class="contact-item">
            <dt><el-icon><Phone /></el-icon><span>手机号</span></dt>
            <dd><a :href="`tel:${publicStore.company.mobile}`">{{ publicStore.company.mobile }}</a></dd>
          </div>
          <div v-if="publicStore.company.email" class="contact-item">
            <dt><el-icon><Message /></el-icon><span>邮箱</span></dt>
            <dd><a :href="`mailto:${publicStore.company.email}`">{{ publicStore.company.email }}</a></dd>
          </div>
          <div v-if="publicStore.company.address" class="contact-item">
            <dt><el-icon><Location /></el-icon><span>地址</span></dt>
            <dd>{{ publicStore.company.address }}</dd>
          </div>
        </dl>

        <el-image
          v-if="publicStore.company.wechatImageUrl"
          :src="resolveAssetUrl(publicStore.company.wechatImageUrl)"
          alt="联系微信二维码"
          class="wechat-image"
          fit="contain"
        />
      </template>
      <el-empty v-else :image-size="64" description="暂无联系信息" />

      <RouterLink class="message-link" to="/contact" @click="closePanel">
        <span>在线留言</span>
        <el-icon><ArrowRight /></el-icon>
      </RouterLink>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowRight, ChatDotRound, Close, Location, Message, Phone } from '@element-plus/icons-vue'

import { usePublicStore } from '@/stores/public'
import { resolveAssetUrl } from '@/utils/asset'

const publicStore = usePublicStore()
const contactRoot = ref<HTMLElement | null>(null)
const isOpen = ref(false)

function openPanel(): void {
  isOpen.value = true
}

function closePanel(): void {
  isOpen.value = false
}

function togglePanel(): void {
  isOpen.value = !isOpen.value
}

function handleDocumentClick(event: MouseEvent): void {
  const target = event.target
  if (target instanceof Node && !contactRoot.value?.contains(target)) {
    closePanel()
  }
}

onMounted(() => {
  void publicStore.loadCompany()
  document.addEventListener('click', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleDocumentClick)
})
</script>

<style scoped>
.floating-contact {
  position: fixed;
  top: 54%;
  right: 0;
  z-index: 30;
  width: 0;
  height: 0;
}

.contact-toggle {
  position: absolute;
  top: 0;
  right: 0;
  display: inline-flex;
  align-items: center;
  gap: 7px;
  min-width: 112px;
  height: 42px;
  padding: 0 16px 0 13px;
  color: #ffffff;
  font-size: 13px;
  white-space: nowrap;
  background: rgb(37 99 235 / 92%);
  border: 1px solid rgb(255 255 255 / 38%);
  border-right: 0;
  border-radius: 22px 0 0 22px;
  box-shadow: 0 8px 22px rgb(15 23 42 / 16%);
  cursor: pointer;
  transform: translateY(-50%);
  transition: background-color 300ms ease, box-shadow 300ms ease, transform 300ms ease;
}

.contact-toggle:hover,
.contact-toggle:focus-visible {
  background: #1d4ed8;
  box-shadow: 0 10px 26px rgb(37 99 235 / 30%);
}

.contact-toggle .el-icon {
  font-size: 17px;
}

.contact-panel {
  position: absolute;
  top: 50%;
  right: 0;
  display: flex;
  width: min(310px, calc(100vw - 32px));
  max-height: calc(100vh - 32px);
  min-height: min(320px, calc(100vh - 32px));
  padding: 24px;
  flex-direction: column;
  overflow-y: auto;
  color: #1f2937;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  box-shadow: 0 20px 48px rgb(15 23 42 / 18%);
  opacity: 0;
  pointer-events: none;
  transform: translateX(calc(100% + 16px)) translateY(-50%);
  transition: all 0.3s ease;
}

.floating-contact.is-open .contact-panel {
  opacity: 1;
  pointer-events: auto;
  transform: translateX(0) translateY(-50%);
}

.contact-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid #eef2f7;
}

.contact-eyebrow {
  margin: 0 0 6px;
  color: #2563eb;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

h2 {
  margin: 0;
  color: #111827;
  font-size: 22px;
}

.panel-close {
  display: grid;
  width: 30px;
  height: 30px;
  color: #6b7280;
  background: #f8fafc;
  border: 1px solid #e5e7eb;
  border-radius: 50%;
  cursor: pointer;
  place-items: center;
  transition: color 180ms ease, background-color 180ms ease;
}

.panel-close:hover,
.panel-close:focus-visible {
  color: #1d4ed8;
  background: #eff6ff;
}

.company-name {
  margin: 18px 0 0;
  color: #374151;
  font-size: 14px;
  font-weight: 650;
}

.contact-list {
  display: grid;
  gap: 15px;
  margin: 18px 0 0;
}

.contact-item {
  display: grid;
  gap: 6px;
}

.contact-item dt {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #9ca3af;
  font-size: 12px;
}

.contact-item dt .el-icon {
  color: #2563eb;
  font-size: 15px;
}

.contact-item dd {
  margin: 0;
  color: #374151;
  font-size: 13px;
  line-height: 1.55;
  overflow-wrap: anywhere;
}

.contact-item a {
  color: inherit;
  text-decoration: none;
}

.contact-item a:hover {
  color: #2563eb;
}

.wechat-image {
  width: 104px;
  height: 104px;
  margin: 18px auto 0;
}

.message-link {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
  padding: 11px 14px;
  color: #ffffff;
  font-size: 13px;
  font-weight: 650;
  background: #2563eb;
  border-radius: 6px;
  box-shadow: 0 8px 18px rgb(37 99 235 / 18%);
  text-decoration: none;
  transition: background-color 180ms ease, transform 180ms ease, box-shadow 180ms ease;
}

.message-link:hover,
.message-link:focus-visible {
  color: #ffffff;
  background: #1d4ed8;
  box-shadow: 0 10px 22px rgb(37 99 235 / 26%);
  transform: translateY(-1px);
}

@media (max-width: 760px) {
  .floating-contact {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .contact-toggle,
  .contact-panel,
  .panel-close,
  .message-link {
    transition: none;
  }
}
</style>
