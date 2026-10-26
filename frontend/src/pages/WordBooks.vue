<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Library, MoveRight, Loader2, BookOpen, Check, Bookmark } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import { wordbookApi, type WordBookSummary } from '@/api/wordbook'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const books = ref<WordBookSummary[]>([])
const loading = ref(false)
const selecting = ref<number | null>(null)
const flash = ref<{ kind: 'ok' | 'err'; text: string } | null>(null)

const levelGroupOrder = ['PRIMARY', 'MIDDLE', 'HIGH', 'CET4', 'CET6', 'POSTGRAD', 'IELTS', 'TOEFL']
const levelLabel: Record<string, string> = {
  PRIMARY: '小学', MIDDLE: '初中', HIGH: '高中',
  CET4: '大学四级', CET6: '大学六级',
  POSTGRAD: '考研', IELTS: '雅思', TOEFL: '托福',
}

const grouped = computed(() => {
  const groups: Record<string, WordBookSummary[]> = {}
  for (const b of books.value) {
    ;(groups[b.level] ??= []).push(b)
  }
  return levelGroupOrder
    .filter((l) => groups[l]?.length)
    .map((l) => ({ level: l, label: levelLabel[l] ?? l, items: groups[l] }))
})

const swatchFor = (color: string) => `bg-pixel-${color}`

onMounted(load)

async function load() {
  loading.value = true
  try {
    books.value = await wordbookApi.books()
  } finally {
    loading.value = false
  }
}

async function selectAsCurrent(b: WordBookSummary) {
  selecting.value = b.id
  try {
    await wordbookApi.select(b.id)
    if (auth.user) auth.user.currentBookId = b.id
    flash.value = { kind: 'ok', text: `已选定 · ${b.name}` }
  } catch (e: any) {
    flash.value = { kind: 'err', text: e?.response?.data?.message || '设置失败' }
  } finally {
    selecting.value = null
    window.setTimeout(() => (flash.value = null), 2500)
  }
}

function progressOf(b: WordBookSummary) {
  if (!b.totalWords) return 0
  return Math.round(((b.masteredCount ?? 0) / b.totalWords) * 100)
}
</script>

<template>
  <AppLayout>
    <section class="container py-8 md:py-12">
      <div class="mx-auto max-w-5xl">
        <header class="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div class="label-slash">
              <Library class="mr-1.5 h-3.5 w-3.5" />
              词书馆
            </div>
            <h1 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              从小学到雅思,<span class="text-ink-faint">一本一本翻过去。</span>
            </h1>
            <p class="mt-3 max-w-[44ch] text-[14.5px] text-ink-soft">
              选择一本词书作为「当前词书」,棋盘上的单词格会优先从你正在背的这本里出题。
            </p>
          </div>
        </header>

        <Transition
          enter-active-class="transition duration-300 ease-out"
          enter-from-class="opacity-0 -translate-y-2"
          leave-active-class="transition duration-200"
          leave-to-class="opacity-0"
        >
          <div
            v-if="flash"
            class="mt-4 flex items-start gap-2 rounded-md border-2 p-3 text-[13px] text-ink"
            :class="flash.kind === 'ok' ? 'border-pixel-green bg-pixel-green/15' : 'border-danger bg-pixel-red/15'"
          >
            <Check v-if="flash.kind === 'ok'" class="mt-0.5 h-4 w-4 shrink-0 text-success" />
            <span>{{ flash.text }}</span>
          </div>
        </Transition>

        <div v-if="loading" class="mt-8 rounded-md border border-rule bg-paper p-8 text-center text-[13.5px] text-ink-soft md:mt-10 md:p-10">
          <Loader2 class="mx-auto mb-2 h-5 w-5 animate-spin" />
          加载词书目录中…
        </div>

        <div v-else class="mt-8 space-y-8 md:mt-10 md:space-y-12">
          <section v-for="g in grouped" :key="g.level">
            <div class="mb-5 flex items-baseline justify-between">
              <h2 class="font-display text-[22px] font-black leading-tight tracking-tight">
                {{ g.label }}
              </h2>
              <span class="font-mono text-[11px] uppercase tracking-[0.16em] text-ink-soft">
                {{ g.items.length }} 本
              </span>
            </div>
            <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <article
                v-for="b in g.items"
                :key="b.id"
                class="relative flex flex-col gap-4 rounded-md border border-rule bg-paper p-5 transition-colors hover:bg-paper-2"
              >
                <!-- Cover -->
                <div class="relative overflow-hidden rounded-sm border border-rule" :class="swatchFor(b.coverColor)">
                  <div class="aspect-[5/2] p-4 sm:aspect-[3/1]">
                    <div class="font-mono text-[10.5px] uppercase tracking-[0.18em] text-ink/70">
                      {{ b.code }}
                    </div>
                    <div class="mt-1 font-display text-[20px] font-black leading-tight tracking-tight text-ink">
                      {{ b.name }}
                    </div>
                  </div>
                  <span v-if="auth.user?.currentBookId === b.id"
                        class="absolute right-2 top-2 inline-flex items-center gap-1 rounded-sm bg-ink px-2 py-0.5 font-mono text-[10px] uppercase tracking-[0.18em] text-paper">
                    <Bookmark class="h-3 w-3" />
                    当前
                  </span>
                </div>

                <p class="text-[13.5px] leading-relaxed text-ink-soft">{{ b.description }}</p>

                <div>
                  <div class="flex items-baseline justify-between font-mono text-[11px] text-ink-soft">
                    <span>掌握 {{ b.masteredCount ?? 0 }} / {{ b.totalWords }}</span>
                    <span>{{ progressOf(b) }}%</span>
                  </div>
                  <div class="mt-1.5 h-2 w-full overflow-hidden border border-rule bg-paper-2">
                    <div class="h-full bg-ink transition-[width] duration-700 ease-out-expo" :style="{ width: progressOf(b) + '%' }" />
                  </div>
                </div>

                <div class="flex items-center gap-2">
                  <RouterLink
                    :to="`/wordbooks/${b.id}`"
                    class="inline-flex h-9 min-h-[40px] flex-1 items-center justify-center gap-2 rounded-md border border-rule bg-paper text-[13px] font-medium text-ink transition-colors hover:bg-paper-3 focus-ring sm:min-h-0"
                  >
                    <BookOpen class="h-3.5 w-3.5" />
                    翻阅
                    <MoveRight class="h-3.5 w-3.5" />
                  </RouterLink>
                  <Button
                    size="sm"
                    class="min-h-[40px] sm:min-h-0"
                    :variant="auth.user?.currentBookId === b.id ? 'bone' : 'pop'"
                    :disabled="selecting === b.id || auth.user?.currentBookId === b.id"
                    @click="selectAsCurrent(b)"
                  >
                    <Loader2 v-if="selecting === b.id" class="h-3.5 w-3.5 animate-spin" />
                    <Check v-else-if="auth.user?.currentBookId === b.id" class="h-3.5 w-3.5" />
                    <Bookmark v-else class="h-3.5 w-3.5" />
                    {{ auth.user?.currentBookId === b.id ? '已选定' : '设为当前' }}
                  </Button>
                </div>
              </article>
            </div>
          </section>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
