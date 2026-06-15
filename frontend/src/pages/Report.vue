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
    error.value = e?.message ?? 'Failed to generate report'
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
    <section class="container py-10">
      <div class="mx-auto max-w-4xl">
        <div class="flex items-end justify-between">
          <div>
            <div class="text-xs uppercase tracking-wider text-muted">AI Weekly Report</div>
            <h1 class="mt-1 text-2xl font-semibold tracking-tight">This week, in summary</h1>
          </div>
          <Button variant="ghost" size="sm" :disabled="loading" @click="load">
            <Loader2 v-if="loading" class="h-3.5 w-3.5 animate-spin" />
            <Sparkles v-else class="h-3.5 w-3.5" />
            Refresh
          </Button>
        </div>

        <div v-if="error" class="mt-6 rounded-md border border-danger/40 bg-danger/10 p-3 text-xs text-danger">
          <AlertCircle class="mr-1 inline h-4 w-4" />
          {{ error }}
        </div>

        <div v-if="loading && !report" class="py-16 text-center text-sm text-muted">
          <Loader2 class="mx-auto mb-2 h-5 w-5 animate-spin" />
          Crunching numbers and asking the AI tutor…
        </div>

        <template v-if="report">
          <!-- Top: gauge + stats -->
          <div class="mt-6 grid gap-4 md:grid-cols-[260px_1fr]">
            <div class="card-surface flex items-center justify-center p-6">
              <div class="relative">
                <svg width="160" height="160" viewBox="0 0 160 160">
                  <defs>
                    <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
                      <stop offset="0%" stop-color="#2563eb" />
                      <stop offset="50%" stop-color="#7c3aed" />
                      <stop offset="100%" stop-color="#d946ef" />
                    </linearGradient>
                  </defs>
                  <circle cx="80" cy="80" r="56" stroke="#30363d" stroke-width="10" fill="none" />
                  <circle
                    cx="80"
                    cy="80"
                    r="56"
                    stroke="url(#g)"
                    stroke-width="10"
                    stroke-linecap="round"
                    fill="none"
                    :stroke-dasharray="circumference"
                    :stroke-dashoffset="dashOffset"
                    transform="rotate(-90 80 80)"
                    class="transition-all duration-700"
                  />
                </svg>
                <div class="absolute inset-0 flex flex-col items-center justify-center">
                  <div class="text-xs uppercase tracking-wider text-muted">Accuracy</div>
                  <div class="text-3xl font-semibold text-gradient-brand">
                    {{ report.stats.accuracy.toFixed(1) }}%
                  </div>
                </div>
              </div>
            </div>

            <div class="card-surface grid grid-cols-2 gap-4 p-6">
              <div>
                <div class="text-xs uppercase tracking-wider text-muted">Answers</div>
                <div class="mt-1 text-2xl font-semibold">{{ report.stats.totalAnswers }}</div>
              </div>
              <div>
                <div class="text-xs uppercase tracking-wider text-muted">XP gained</div>
                <div class="mt-1 text-2xl font-semibold">{{ report.stats.xpGained }}</div>
              </div>
              <div>
                <div class="text-xs uppercase tracking-wider text-muted text-success">Correct</div>
                <div class="mt-1 text-2xl font-semibold text-success">{{ report.stats.correctAnswers }}</div>
              </div>
              <div>
                <div class="text-xs uppercase tracking-wider text-muted text-danger">Wrong</div>
                <div class="mt-1 text-2xl font-semibold text-danger">{{ report.stats.wrongAnswers }}</div>
              </div>
            </div>
          </div>

          <!-- By type -->
          <div v-if="report.by_type.length" class="card-surface mt-4 p-6">
            <div class="mb-4 flex items-center gap-2 text-xs uppercase tracking-wider text-muted">
              <Target class="h-3.5 w-3.5 text-accent" />
              By question type
            </div>
            <div class="space-y-3">
              <div v-for="b in report.by_type" :key="b.type">
                <div class="flex justify-between text-sm">
                  <span class="font-medium">{{ b.type }}</span>
                  <span class="font-mono text-muted">{{ b.correct }} / {{ b.total }} · {{ b.accuracy.toFixed(1) }}%</span>
                </div>
                <div class="mt-1 h-2 overflow-hidden rounded-full bg-surface-1">
                  <div
                    class="h-full bg-gradient-brand bg-[length:200%_200%]"
                    :style="{ width: `${Math.min(100, b.accuracy)}%` }"
                  />
                </div>
              </div>
            </div>
          </div>

          <!-- AI summary -->
          <div class="card-surface mt-4 overflow-hidden p-6">
            <div class="pointer-events-none absolute -top-20 right-0 h-44 w-72 bg-gradient-radial blur-2xl" />
            <div class="relative">
              <div class="mb-3 flex items-center gap-2 text-xs uppercase tracking-wider text-muted">
                <Sparkles class="h-3.5 w-3.5 text-accent" />
                AI tutor's note
              </div>
              <p class="text-sm leading-relaxed">{{ report.ai_summary.praise_zh }}</p>

              <div v-if="report.ai_summary.weak_areas.length" class="mt-4">
                <div class="text-xs uppercase tracking-wider text-muted">Weak spots</div>
                <div class="mt-2 flex flex-wrap gap-2">
                  <span
                    v-for="w in report.ai_summary.weak_areas"
                    :key="w"
                    class="rounded-full border border-danger/40 bg-danger/10 px-2.5 py-0.5 text-xs text-danger"
                  >{{ w }}</span>
                </div>
              </div>

              <div class="mt-4">
                <div class="text-xs uppercase tracking-wider text-muted">Suggestions</div>
                <ul class="mt-2 space-y-2">
                  <li v-for="(s, i) in report.ai_summary.suggestions_zh" :key="i" class="flex gap-2 text-sm">
                    <Check class="mt-0.5 h-4 w-4 shrink-0 text-success" />
                    <span>{{ s }}</span>
                  </li>
                </ul>
              </div>

              <div class="mt-4 inline-flex items-center gap-2 rounded-md border border-accent/40 bg-accent/10 px-3 py-1.5 text-xs text-accent-subtle">
                <TrendingUp class="h-3.5 w-3.5" />
                Focus next week: <span class="text-foreground">{{ report.ai_summary.focus_next_week_zh }}</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </section>
  </AppLayout>
</template>
