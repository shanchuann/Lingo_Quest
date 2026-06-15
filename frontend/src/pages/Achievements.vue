<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import * as Icons from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import { achievementApi, type AchievementCatalog, type UnlockedAchievement } from '@/api/achievement'

const catalog = ref<AchievementCatalog[]>([])
const mine = ref<UnlockedAchievement[]>([])
const loading = ref(false)

const unlockedSet = computed(() => new Set(mine.value.map((a) => a.id)))

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
  return (Icons as any)[name] ?? Icons.Award
}

function unlockedAt(id: number) {
  return mine.value.find((m) => m.id === id)?.unlockedAt ?? null
}
</script>

<template>
  <AppLayout>
    <section class="container py-10">
      <div class="mx-auto max-w-4xl">
        <div class="flex items-end justify-between">
          <div>
            <div class="text-xs uppercase tracking-wider text-muted">Achievements</div>
            <h1 class="mt-1 text-2xl font-semibold tracking-tight">Your badge collection</h1>
          </div>
          <div class="text-xs text-muted">
            Unlocked
            <span class="font-mono text-foreground">{{ mine.length }}</span>
            /
            <span class="font-mono text-foreground">{{ catalog.length }}</span>
          </div>
        </div>

        <div v-if="loading" class="py-16 text-center text-sm text-muted">Loading…</div>

        <div v-else class="mt-6 grid gap-4 md:grid-cols-2">
          <article
            v-for="a in catalog"
            :key="a.id"
            class="card-surface group relative overflow-hidden p-5 transition"
            :class="unlockedSet.has(a.id) ? '' : 'opacity-60 grayscale'"
          >
            <div class="flex items-start gap-4">
              <div
                class="flex h-12 w-12 items-center justify-center rounded-md border"
                :class="unlockedSet.has(a.id)
                  ? 'border-accent/50 bg-gradient-brand bg-[length:200%_200%] text-white shadow-glow'
                  : 'border-border bg-surface-1 text-muted'"
              >
                <component :is="resolveIcon(a.icon)" class="h-6 w-6" />
              </div>
              <div class="flex-1">
                <div class="flex items-center gap-2 font-medium">
                  {{ a.title }}
                  <span
                    v-if="unlockedSet.has(a.id)"
                    class="rounded-full bg-success/20 px-2 py-0.5 text-[10px] uppercase tracking-wider text-success"
                  >
                    Unlocked
                  </span>
                </div>
                <p class="mt-1 text-sm text-muted">{{ a.description }}</p>
                <div class="mt-3 flex items-center gap-3 text-xs text-muted">
                  <span class="font-mono">+{{ a.rewardExp }} XP</span>
                  <span class="font-mono">+{{ a.rewardCoin }} coins</span>
                  <span v-if="unlockedAt(a.id)" class="font-mono">
                    @ {{ String(unlockedAt(a.id)).slice(0, 10) }}
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
