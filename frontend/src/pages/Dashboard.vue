<script setup lang="ts">
import { onMounted, computed, ref } from 'vue'
import { Trophy, Coins, TrendingUp, Sparkles, Dice5, MoveRight } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'
import { achievementApi } from '@/api/achievement'
import { RouterLink } from 'vue-router'

const auth = useAuthStore()
const unlockedCount = ref(0)

onMounted(async () => {
  auth.refreshMe().catch(() => {})
  try {
    const mine = await achievementApi.mine()
    unlockedCount.value = mine.length
  } catch { /* non-fatal */ }
})

const xpForNext = computed(() => {
  const lv = auth.user?.level ?? 1
  const curve = [0, 100, 250, 500, 900, 1500]
  return curve[Math.min(lv, curve.length - 1)]
})

const progress = computed(() => {
  const xp = auth.user?.experience ?? 0
  const next = xpForNext.value || 1
  return Math.max(0, Math.min(100, Math.round((xp / next) * 100)))
})
</script>

<template>
  <AppLayout>
    <section class="container py-10 md:py-14">
      <div class="mx-auto max-w-5xl">
        <div class="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div class="label-slash">已登录</div>
            <h1 class="mt-4 break-words font-display text-display-lg font-black leading-[0.98] tracking-tight">
              欢迎回来,<span class="text-ink-faint">{{ auth.user?.username || '学者' }}。</span>
            </h1>
            <p class="mt-4 max-w-[40ch] text-[15px] leading-[1.7] text-ink-soft">
              下一步只差一掷骰子。下面是你最近几次对局留下的数字。
            </p>
          </div>
          <RouterLink
            to="/game"
            class="inline-flex h-12 items-center gap-2 rounded-md bg-pixel-green px-5 text-[14px] font-medium text-ink transition-colors hover:bg-pixel-mint focus-ring"
          >
            <Dice5 class="h-4 w-4" />
            继续棋盘<span class="font-mono text-ink/60">/</span>
            <MoveRight class="h-4 w-4" />
          </RouterLink>
        </div>

        <!-- Stats strip -->
        <div class="mt-8 grid gap-[2px] overflow-hidden rounded-md border border-rule bg-rule md:mt-12 md:grid-cols-3">
          <article class="bg-paper p-5 md:p-6">
            <div class="flex items-center justify-between text-[12px] text-ink-soft">
              <span class="inline-flex items-center gap-1.5 text-ink"><TrendingUp class="h-3.5 w-3.5" /> 等级</span>
              <span class="inline-block h-3 w-3 bg-pixel-green" />
            </div>
            <div class="mt-5 font-display text-[40px] md:text-[56px] font-black leading-none tracking-tight">
              Lv {{ auth.user?.level ?? 1 }}
            </div>
            <div class="mt-5 h-2 w-full overflow-hidden border border-rule bg-paper-2">
              <div class="h-full bg-ink transition-[width] duration-700 ease-out-expo" :style="{ width: progress + '%' }" />
            </div>
            <div class="mt-2 flex justify-between font-mono text-[11px] text-ink-soft">
              <span>{{ auth.user?.experience ?? 0 }} xp</span>
              <span>下一级 · {{ xpForNext }}</span>
            </div>
          </article>

          <article class="bg-paper p-5 md:p-6">
            <div class="flex items-center justify-between text-[12px] text-ink-soft">
              <span class="inline-flex items-center gap-1.5 text-ink"><Coins class="h-3.5 w-3.5" /> 金币</span>
              <span class="inline-block h-3 w-3 bg-pixel-amber" />
            </div>
            <div class="mt-5 font-display text-[40px] md:text-[56px] font-black leading-none tracking-tight">
              {{ auth.user?.coins ?? 0 }}
            </div>
            <p class="mt-5 text-[13px] text-ink-soft">
              在幸运格和完成任务时积累。
            </p>
          </article>

          <article class="bg-paper p-5 md:p-6">
            <div class="flex items-center justify-between text-[12px] text-ink-soft">
              <span class="inline-flex items-center gap-1.5 text-ink"><Trophy class="h-3.5 w-3.5" /> 徽章</span>
              <span class="inline-block h-3 w-3 bg-pixel-pink" />
            </div>
            <div class="mt-5 font-display text-[40px] md:text-[56px] font-black leading-none tracking-tight">{{ unlockedCount }}</div>
            <RouterLink
              to="/achievements"
              class="mt-5 inline-flex items-center gap-1 text-[13px] text-ink-soft underline decoration-rule-soft underline-offset-[6px] transition-colors hover:text-ink hover:decoration-ink"
            >
              查看徽章图鉴<span class="font-mono text-ink-faint">/</span> <MoveRight class="h-3 w-3" />
            </RouterLink>
          </article>
        </div>

        <!-- Quick links -->
        <div class="mt-8 grid gap-4 md:mt-12 md:grid-cols-2">
          <RouterLink
            to="/game"
            class="group relative overflow-hidden rounded-md border border-rule bg-paper p-5 transition-colors md:p-6 hover:bg-paper-2"
          >
            <div class="flex items-start gap-4">
              <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-sm bg-pixel-green text-ink">
                <Dice5 class="h-6 w-6" />
              </div>
              <div class="flex-1">
                <div class="font-display text-[20px] font-black leading-tight tracking-tight">继续你的旅程</div>
                <p class="mt-2 text-[14px] text-ink-soft">
                  从上次掷骰的地方接着走。你的玩家令牌还在棋盘上。
                </p>
              </div>
              <MoveRight class="h-4 w-4 text-ink-soft transition-transform duration-300 group-hover:translate-x-1" />
            </div>
          </RouterLink>

          <RouterLink
            to="/tutor"
            class="group relative overflow-hidden rounded-md border border-rule bg-paper p-5 transition-colors md:p-6 hover:bg-paper-2"
          >
            <div class="flex items-start gap-4">
              <div class="flex h-12 w-12 shrink-0 items-center justify-center rounded-sm bg-pixel-blue text-paper">
                <Sparkles class="h-6 w-6" />
              </div>
              <div class="flex-1">
                <div class="font-display text-[20px] font-black leading-tight tracking-tight">问问 AI 导师</div>
                <p class="mt-2 text-[14px] text-ink-soft">
                  随时来一次语法纠错或单词辅导。
                </p>
              </div>
              <MoveRight class="h-4 w-4 text-ink-soft transition-transform duration-300 group-hover:translate-x-1" />
            </div>
          </RouterLink>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
