<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import * as Icons from 'lucide-vue-next'
import { Award, Trophy } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import { achievementApi, type AchievementCatalog, type UnlockedAchievement } from '@/api/achievement'

const catalog = ref<AchievementCatalog[]>([])
const mine = ref<UnlockedAchievement[]>([])
const loading = ref(false)

const unlockedSet = computed(() => new Set(mine.value.map((a) => a.id)))
const progress = computed(() => {
  const total = catalog.value.length || 1
  return Math.round((mine.value.length / total) * 100)
})

const swatches = ['bg-pixel-green', 'bg-pixel-blue', 'bg-pixel-amber', 'bg-pixel-violet', 'bg-pixel-pink', 'bg-pixel-mint', 'bg-pixel-orange', 'bg-pixel-red']

onMounted(async () => {
  loading.value = true
  try {
    const [c, m] = await Promise.all([achievementApi.catalog(), achievementApi.mine()])
    catalog.value = c
    mine.value = m
  } finally {
    loading.value = false
  }
})

function resolveIcon(name: string) {
  return (Icons as any)[name] ?? Award
}

function unlockedAt(id: number) {
  return mine.value.find((m) => m.id === id)?.unlockedAt ?? null
}
</script>

<template>
  <AppLayout>
    <section class="container py-12">
      <div class="mx-auto max-w-4xl">
        <header class="flex flex-wrap items-end justify-between gap-6">
          <div>
            <div class="label-slash">
              <Trophy class="mr-1.5 h-3.5 w-3.5" />
              徽章图鉴
            </div>
            <h1 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              你已经收集到的徽章。
            </h1>
          </div>
          <div class="min-w-[200px]">
            <div class="flex items-baseline justify-between text-[12px] text-ink-soft">
              <span>已解锁</span>
              <span class="font-mono text-ink">{{ mine.length }} / {{ catalog.length }}</span>
            </div>
            <div class="mt-2 h-2 w-full overflow-hidden border border-rule bg-paper-2">
              <div class="h-full bg-pixel-green transition-[width] duration-700 ease-out-expo" :style="{ width: progress + '%' }" />
            </div>
          </div>
        </header>

        <div v-if="loading" class="py-16 text-center text-[14px] text-ink-soft">加载中…</div>

        <div v-else class="mt-10 grid gap-[2px] overflow-hidden rounded-md border border-rule bg-rule md:grid-cols-2">
          <article
            v-for="(a, i) in catalog"
            :key="a.id"
            class="relative bg-paper p-6 transition-colors duration-300 hover:bg-paper-2"
            :class="unlockedSet.has(a.id) ? '' : 'opacity-55'"
          >
            <div class="flex items-start gap-4">
              <div
                class="flex h-12 w-12 items-center justify-center rounded-sm"
                :class="unlockedSet.has(a.id) ? swatches[i % swatches.length] + ' text-ink' : 'border border-rule bg-paper-3 text-ink-soft'"
              >
                <component :is="resolveIcon(a.icon)" class="h-6 w-6" />
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-2 font-display text-[18px] font-black leading-tight tracking-tight">
                  {{ a.title }}
                  <span
                    v-if="unlockedSet.has(a.id)"
                    class="rounded-sm bg-pixel-green/30 px-1.5 py-0.5 text-[11px] text-ink"
                  >
                    已解锁
                  </span>
                </div>
                <p class="mt-2 text-[14px] leading-relaxed text-ink-soft">{{ a.description }}</p>
                <div class="mt-4 flex items-center gap-4 font-mono text-[11px] uppercase tracking-[0.16em] text-ink-soft">
                  <span>+{{ a.rewardExp }} xp</span>
                  <span>+{{ a.rewardCoin }} 金币</span>
                  <span v-if="unlockedAt(a.id)" class="text-ink">
                    {{ String(unlockedAt(a.id)).slice(0, 10) }}
                  </span>
                </div>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
