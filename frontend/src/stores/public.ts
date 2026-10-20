import { ref } from 'vue'
import { defineStore } from 'pinia'

import { publicApi } from '@/api/public'
import type { PublicCategory, PublicCompany, PublicSite } from '@/types/public'

export const usePublicStore = defineStore('public', () => {
  const site = ref<PublicSite | null>(null)
  const company = ref<PublicCompany | null>(null)
  const categories = ref<PublicCategory[]>([])
  const navigationLoading = ref(false)
  const companyLoading = ref(false)
  const companyLoaded = ref(false)

  async function loadNavigation(): Promise<void> {
    if (categories.value.length > 0) {
      return
    }

    navigationLoading.value = true
    try {
      categories.value = await publicApi.categoryTree()
    } catch {
      categories.value = []
    } finally {
      navigationLoading.value = false
    }
  }

  async function loadCompany(): Promise<void> {
    if (company.value || companyLoaded.value || companyLoading.value) return

    companyLoading.value = true
    try {
      company.value = await publicApi.company()
      companyLoaded.value = true
    } catch {
      company.value = null
    } finally {
      companyLoading.value = false
    }
  }

  function setSite(siteValue: PublicSite, companyValue: PublicCompany): void {
    site.value = siteValue
    company.value = companyValue
    companyLoaded.value = true
  }

  return {
    site,
    company,
    categories,
    navigationLoading,
    companyLoading,
    loadNavigation,
    loadCompany,
    setSite,
  }
})
