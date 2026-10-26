<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Sparkles, Loader2, TrendingUp, Target, Check, AlertCircle } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import { reportApi, type WeeklyReport } from '@/api/report'

const report = ref<WeeklyReport | null>(null)
const loading = ref(false)
const error = ref<string | null>(null)

async function load() {
  loading.value = true
  error.value = null
  try {
    report.value = await reportApi.weekly()
  } catch (e: any) {
    error.value = e?.message ?? '生成报告失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)

const radius = 56
const circumference = 2 * Math.PI * radius
const dashOffset = computed(() => {
  const acc = report.value?.stats.accuracy ?? 0
  return circumference - (acc / 100) * circumference
})
</script>

<template>
  <AppLayout>
    <section class="container py-8 md:py-12">
      <div class="mx-auto max-w-4xl">
        <header class="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div class="label-slash">
              <Sparkles class="mr-1.5 h-3.5 w-3.5" />
              AI 周报
            </div>
            <h1 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              这一周,一句话总结。
            </h1>
          </div>
          <Button variant="bone" size="sm" class="min-h-[40px] sm:min-h-0" :disabled="loading" @click="load">
            <Loader2 v-if="loading" class="h-3.5 w-3.5 animate-spin" />
            <Sparkles v-else class="h-3.5 w-3.5" />
            {{ loading ? '刷新中' : '重新生成' }}
          </Button>
        </header>

        <div v-if="error" class="mt-6 flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink">
          <AlertCircle class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
          <span>{{ error }}</span>
        </div>

        <div v-if="loading && !report" class="py-20 text-center">
          <Loader2 class="mx-auto mb-3 h-5 w-5 animate-spin text-ink-soft" />
          <p class="text-[14px] text-ink-soft">数据梳理中,AI 导师正在阅读…</p>
        </div>

        <template v-if="report">
          <!-- Gauge + counters -->
          <div class="mt-8 grid gap-[2px] overflow-hidden rounded-md border border-rule bg-rule md:mt-10 md:grid-cols-[280px_1fr]">
            <div class="flex items-center justify-center bg-paper p-5 md:p-6">
              <div class="relative">
                <svg width="160" height="160" viewBox="0 0 160 160">
                  <circle cx="80" cy="80" r="56" stroke="oklch(0.78 0.012 90)" stroke-width="10" fill="none" />
                  <circle
                    cx="80" cy="80" r="56"
                    stroke="oklch(0.18 0.010 80)"
                    stroke-width="10"
                    stroke-linecap="butt"
                    fill="none"
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="dashOffset"
                    transform="rotate(-90 80 80)"
                    class="transition-all duration-700 ease-out-expo"
                  />
                </svg>
                <div class="absolute inset-0 flex flex-col items-center justify-center">
                  <div class="text-[11.5px] text-ink-soft">正确率</div>
                  <div class="font-display text-[38px] font-black leading-none tracking-tight text-ink">
                    {{ report.stats.accuracy.toFixed(0) }}%
                  </div>
                </div>
              </div>
            </div>

            <div class="grid grid-cols-2 gap-[2px] bg-rule">
              <div class="bg-paper p-5">
                <div class="text-[12px] text-ink-soft">答题数</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight">{{ report.stats.totalAnswers }}</div>
              </div>
              <div class="bg-paper p-5">
                <div class="text-[12px] text-ink-soft">本周经验</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight">{{ report.stats.xpGained }}</div>
              </div>
              <div class="bg-paper p-5">
                <div class="text-[12px] text-success">答对</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight text-success">{{ report.stats.correctAnswers }}</div>
              </div>
              <div class="bg-paper p-5">
                <div class="text-[12px] text-danger">答错</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight text-danger">{{ report.stats.wrongAnswers }}</div>
              </div>
            </div>
          </div>

          <!-- By type -->
          <div v-if="report.by_type.length" class="mt-6 rounded-md border border-rule bg-paper p-5 md:p-6">
            <div class="label-slash">
              <Target class="mr-1.5 h-3.5 w-3.5" />
              按题型拆解
            </div>
            <div class="mt-5 space-y-4">
              <div v-for="b in report.by_type" :key="b.type">
                <div class="flex flex-wrap items-baseline justify-between gap-x-3 gap-y-1 text-[13.5px]">
                  <span class="font-medium">{{ b.type }}</span>
                  <span class="font-mono text-ink-soft">{{ b.correct }} / {{ b.total }} · {{ b.accuracy.toFixed(0) }}%</span>
                </div>
                <div class="mt-1.5 h-2 w-full overflow-hidden border border-rule bg-paper-2">
                  <div
                    class="h-full bg-ink transition-[width] duration-700 ease-out-expo"
                    :style="{ width: `${Math.min(100, b.accuracy)}%` }"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- AI summary -->
          <div class="relative mt-6 overflow-hidden rounded-md border border-rule bg-paper p-5 md:p-6">
            <div class="absolute right-0 top-0 grid grid-cols-3 gap-[2px]" aria-hidden="true">
              <span class="h-3 w-3 bg-pixel-green" />
              <span class="h-3 w-3 bg-pixel-amber" />
              <span class="h-3 w-3 bg-pixel-pink" />
              <span class="h-3 w-3 bg-pixel-blue" />
              <span class="h-3 w-3 bg-pixel-violet" />
              <span class="h-3 w-3 bg-paper-3" />
            </div>
            <div>
              <div class="label-slash">
                <Sparkles class="mr-1.5 h-3.5 w-3.5" />
                导师的话
              </div>
              <p class="mt-4 text-[15px] leading-[1.8] text-ink text-pretty">
                {{ report.ai_summary.praise_zh }}
              </p>

              <div v-if="report.ai_summary.weak_areas.length" class="mt-6">
                <div class="text-[12px] text-ink-soft">薄弱点</div>
                <div class="mt-2 flex flex-wrap gap-2">
                  <span
                    v-for="w in report.ai_summary.weak_areas"
                    :key="w"
                    class="rounded-sm border border-danger bg-pixel-red/15 px-2.5 py-0.5 text-[12px] text-ink"
                  >{{ w }}</span>
                </div>
              </div>

              <div class="mt-6">
                <div class="text-[12px] text-ink-soft">建议</div>
                <ul class="mt-3 space-y-2.5">
                  <li v-for="(s, i) in report.ai_summary.suggestions_zh" :key="i" class="flex gap-2.5 text-[14.5px] text-ink">
                    <Check class="mt-0.5 h-4 w-4 shrink-0 text-success" />
                    <span>{{ s }}</span>
                  </li>
                </ul>
              </div>

              <div class="mt-6 inline-flex flex-wrap items-center gap-2 rounded-sm border border-ink bg-pixel-amber/40 px-3 py-2 text-[13px] text-ink">
                <TrendingUp class="h-3.5 w-3.5" />
                <span class="font-medium">下周聚焦</span>
                <span class="text-ink-faint">·</span>
                <span>{{ report.ai_summary.focus_next_week_zh }}</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </section>
  </AppLayout>
</template>
