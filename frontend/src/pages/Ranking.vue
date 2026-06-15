<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Trophy, Crown, Medal, Calendar, Infinity as InfinityIcon } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import { rankingApi, type RankRow } from '@/api/ranking'

const tab = ref<'weekly' | 'all'>('weekly')
const weekly = ref<RankRow[]>([])
const allTime = ref<RankRow[]>([])
const loading = ref(false)

const rows = computed(() => (tab.value === 'weekly' ? weekly.value : allTime.value))

async function load() {
  loading.value = true
  try {
    const [w, a] = await Promise.all([rankingApi.weekly(20), rankingApi.allTime(20)])
    weekly.value = w
    allTime.value = a
  } finally {
    loading.value = false
  }
}

onMounted(load)

function medal(rank: number) {
  if (rank === 1) return { icon: Crown, cls: 'text-amber-300' }
  if (rank === 2) return { icon: Medal, cls: 'text-slate-200' }
  if (rank === 3) return { icon: Medal, cls: 'text-orange-400' }
  return null
}
</script>

<template>
  <AppLayout>
    <section class="container py-10">
      <div class="mx-auto max-w-3xl">
        <div class="flex items-end justify-between">
          <div>
            <div class="text-xs uppercase tracking-wider text-muted">Leaderboard</div>
            <h1 class="mt-1 text-2xl font-semibold tracking-tight">Top scholars</h1>
          </div>
          <div class="inline-flex rounded-md border border-border bg-surface/60 p-1 backdrop-blur">
            <button
              class="inline-flex h-8 items-center gap-1.5 rounded-[6px] px-3 text-xs transition"
              :class="tab === 'weekly' ? 'bg-gradient-brand bg-[length:200%_200%] text-white shadow-glow' : 'text-muted hover:text-foreground'"
              @click="tab = 'weekly'"
            >
              <Calendar class="h-3.5 w-3.5" />
              This week
            </button>
            <button
              class="inline-flex h-8 items-center gap-1.5 rounded-[6px] px-3 text-xs transition"
              :class="tab === 'all' ? 'bg-gradient-brand bg-[length:200%_200%] text-white shadow-glow' : 'text-muted hover:text-foreground'"
              @click="tab = 'all'"
            >
              <InfinityIcon class="h-3.5 w-3.5" />
              All-time
            </button>
          </div>
        </div>

        <div class="card-surface mt-6 overflow-hidden">
          <div class="grid grid-cols-[60px_1fr_80px_100px] gap-2 border-b border-border bg-surface-1/50 px-4 py-2 text-[10px] uppercase tracking-wider text-muted">
            <div>Rank</div>
            <div>Player</div>
            <div class="text-right">Level</div>
            <div class="text-right">{{ tab === 'weekly' ? 'Weekly XP' : 'Total XP' }}</div>
          </div>
          <div v-if="loading" class="px-4 py-10 text-center text-sm text-muted">Loading…</div>
          <div v-else-if="rows.length === 0" class="px-4 py-10 text-center text-sm text-muted">
            <Trophy class="mx-auto mb-2 h-6 w-6 opacity-40" />
            No entries yet — be the first.
          </div>
          <ul v-else>
            <li
              v-for="row in rows"
              :key="row.userId"
              class="grid grid-cols-[60px_1fr_80px_100px] items-center gap-2 px-4 py-3 transition odd:bg-surface-1/30 hover:bg-surface-1/60"
            >
              <div class="flex items-center gap-1.5 text-sm font-mono">
                <component
                  v-if="medal(row.rank)"
                  :is="medal(row.rank)!.icon"
                  :class="medal(row.rank)!.cls"
                  class="h-4 w-4"
                />
                <span>#{{ row.rank }}</span>
              </div>
              <div class="flex items-center gap-3">
                <div class="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-brand text-xs font-semibold text-white">
                  {{ row.username[0].toUpperCase() }}
                </div>
                <span class="text-sm">{{ row.username }}</span>
              </div>
              <div class="text-right text-sm">Lv.{{ row.level }}</div>
              <div class="text-right text-sm font-mono">{{ row.score ?? row.experience ?? 0 }}</div>
            </li>
          </ul>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
