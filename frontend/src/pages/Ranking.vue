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
  if (rank === 1) return { icon: Crown, bg: 'bg-pixel-amber', fg: 'text-ink' }
  if (rank === 2) return { icon: Medal, bg: 'bg-paper-3', fg: 'text-ink' }
  if (rank === 3) return { icon: Medal, bg: 'bg-pixel-orange', fg: 'text-ink' }
  return null
}
</script>

<template>
  <AppLayout>
    <section class="container py-12">
      <div class="mx-auto max-w-3xl">
        <header class="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div class="label-slash">
              <Trophy class="mr-1.5 h-3.5 w-3.5" />
              排行榜
            </div>
            <h1 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              本周顶尖玩家。
            </h1>
          </div>

          <div class="inline-flex rounded-md border border-rule bg-paper p-1">
            <button
              class="inline-flex h-9 items-center gap-2 rounded-sm px-3.5 text-[13.5px] transition-colors duration-200 focus-ring"
              :class="tab === 'weekly' ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
              @click="tab = 'weekly'"
            >
              <Calendar class="h-3.5 w-3.5" />
              本周
            </button>
            <button
              class="inline-flex h-9 items-center gap-2 rounded-sm px-3.5 text-[13.5px] transition-colors duration-200 focus-ring"
              :class="tab === 'all' ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
              @click="tab = 'all'"
            >
              <InfinityIcon class="h-3.5 w-3.5" />
              历史总榜
            </button>
          </div>
        </header>

        <div class="mt-8 overflow-hidden rounded-md border border-rule bg-paper">
          <div class="grid grid-cols-[64px_1fr_72px_92px] gap-2 border-b border-rule bg-paper-2 px-4 py-2.5 font-mono text-[10.5px] uppercase tracking-[0.18em] text-ink-soft">
            <div>排名</div>
            <div>玩家</div>
            <div class="text-right">等级</div>
            <div class="text-right">{{ tab === 'weekly' ? '本周 xp' : '总 xp' }}</div>
          </div>

          <div v-if="loading" class="px-4 py-12 text-center text-[13.5px] text-ink-soft">加载中…</div>
          <div v-else-if="rows.length === 0" class="px-4 py-14 text-center">
            <Trophy class="mx-auto mb-3 h-6 w-6 text-ink-faint" />
            <div class="text-[14px] text-ink-soft">还没有人上榜 —— 你可以是第一个。</div>
          </div>
          <ul v-else class="divide-y divide-rule-soft">
            <li
              v-for="row in rows"
              :key="row.userId"
              class="grid grid-cols-[64px_1fr_72px_92px] items-center gap-2 px-4 py-3 transition-colors hover:bg-paper-2"
            >
              <div class="flex items-center gap-2 font-mono text-[13px]">
                <span
                  v-if="medal(row.rank)"
                  :class="['inline-flex h-5 w-5 items-center justify-center rounded-sm', medal(row.rank)!.bg, medal(row.rank)!.fg]"
                >
                  <component :is="medal(row.rank)!.icon" class="h-3 w-3" />
                </span>
                <span class="text-ink">#{{ row.rank.toString().padStart(2, '0') }}</span>
              </div>
              <div class="flex items-center gap-3">
                <div class="flex h-8 w-8 items-center justify-center rounded-sm border border-rule bg-paper-3 font-mono text-[12px] font-semibold text-ink">
                  {{ row.username[0].toUpperCase() }}
                </div>
                <span class="text-[14px]">{{ row.username }}</span>
              </div>
              <div class="text-right font-mono text-[13px] text-ink-soft">Lv {{ row.level }}</div>
              <div class="text-right font-mono text-[13px] text-ink">{{ row.score ?? row.experience ?? 0 }}</div>
            </li>
          </ul>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
