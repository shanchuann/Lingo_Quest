<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import {
  ArrowLeft,
  Check,
  CircleDot,
  Loader2,
  Sparkles,
  BookOpen,
  Bookmark,
  Filter,
} from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import { wordbookApi, type WordBookSummary, type WordEntry, type WordState } from '@/api/wordbook'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const bookId = computed(() => Number(route.params.id))

const book = ref<WordBookSummary | null>(null)
const words = ref<WordEntry[]>([])
const loading = ref(false)
const filter = ref<'ALL' | WordState>('ALL')
const marking = ref<number | null>(null)

const stateMap: Record<WordState, { label: string; color: string; icon: any }> = {
  NEW:       { label: '未开始',  color: 'text-ink-faint',           icon: CircleDot },
  LEARNING:  { label: '学习中',  color: 'text-warning',             icon: Sparkles },
  MASTERED:  { label: '已掌握',  color: 'text-success',             icon: Check },
}

const filtered = computed(() => {
  if (filter.value === 'ALL') return words.value
  return words.value.filter((w) => w.state === filter.value)
})

const counters = computed(() => {
  const n = words.value
  return {
    all: n.length,
    NEW:      n.filter((w) => w.state === 'NEW').length,
    LEARNING: n.filter((w) => w.state === 'LEARNING').length,
    MASTERED: n.filter((w) => w.state === 'MASTERED').length,
  }
})

const swatchFor = (color: string) => `bg-pixel-${color ?? 'green'}`
const progressPct = computed(() => {
  if (!book.value?.totalWords) return 0
  return Math.round(((book.value.masteredCount ?? 0) / book.value.totalWords) * 100)
})

onMounted(load)
watch(bookId, load)

async function load() {
  loading.value = true
  try {
    const [allBooks, ws] = await Promise.all([
      wordbookApi.books(),
      wordbookApi.words(bookId.value),
    ])
    book.value = allBooks.find((b) => b.id === bookId.value) ?? null
    words.value = ws
  } finally {
    loading.value = false
  }
}

async function mark(w: WordEntry, state: WordState) {
  marking.value = w.id
  try {
    await wordbookApi.mark(w.id, state)
    const prev = w.state
    w.state = state
    if (book.value) {
      if (prev !== 'MASTERED' && state === 'MASTERED') book.value.masteredCount = (book.value.masteredCount ?? 0) + 1
      else if (prev === 'MASTERED' && state !== 'MASTERED') book.value.masteredCount = Math.max(0, (book.value.masteredCount ?? 0) - 1)
    }
  } finally {
    marking.value = null
  }
}

async function setCurrent() {
  if (!book.value) return
  await wordbookApi.select(book.value.id)
  if (auth.user) auth.user.currentBookId = book.value.id
}
</script>

<template>
  <AppLayout>
    <section class="container py-12">
      <div class="mx-auto max-w-4xl">
        <div class="mb-6">
          <button
            class="inline-flex items-center gap-2 text-[13px] text-ink-soft underline decoration-rule-soft underline-offset-[6px] transition-colors hover:text-ink hover:decoration-ink focus-ring rounded-sm"
            @click="router.push('/wordbooks')"
          >
            <ArrowLeft class="h-3.5 w-3.5" />
            返回词书馆
          </button>
        </div>

        <div v-if="loading || !book" class="rounded-md border border-rule bg-paper p-12 text-center text-[13.5px] text-ink-soft">
          <Loader2 class="mx-auto mb-3 h-5 w-5 animate-spin" />
          翻开中…
        </div>

        <template v-else>
          <!-- Book header -->
          <header class="mb-8 grid gap-6 lg:grid-cols-[280px_1fr]">
            <div class="relative overflow-hidden rounded-md border border-rule" :class="swatchFor(book.coverColor)">
              <div class="flex aspect-[3/4] flex-col justify-between p-6">
                <div class="font-mono text-[10.5px] uppercase tracking-[0.18em] text-ink/70">
                  {{ book.code }} · {{ book.totalWords }} 词
                </div>
                <div>
                  <div class="font-display text-[36px] font-black leading-[0.95] tracking-tight text-ink">
                    {{ book.name }}
                  </div>
                  <div class="mt-3 text-[13px] leading-relaxed text-ink/90">{{ book.description }}</div>
                </div>
              </div>
            </div>

            <div class="flex flex-col gap-6">
              <div>
                <div class="label-slash">学习进度</div>
                <div class="mt-3 flex items-baseline gap-2 font-display text-[44px] font-black leading-none tracking-tight">
                  {{ progressPct }}%
                  <span class="font-mono text-[12px] text-ink-soft">
                    {{ book.masteredCount ?? 0 }} / {{ book.totalWords }}
                  </span>
                </div>
                <div class="mt-3 h-2 w-full overflow-hidden border border-rule bg-paper-2">
                  <div class="h-full bg-ink transition-[width] duration-700 ease-out-expo" :style="{ width: progressPct + '%' }" />
                </div>
              </div>
              <div class="flex flex-wrap gap-2">
                <Button v-if="auth.user?.currentBookId !== book.id" @click="setCurrent">
                  <Bookmark class="h-3.5 w-3.5" />
                  设为当前词书
                </Button>
                <span v-else class="inline-flex items-center gap-2 rounded-md border-2 border-ink bg-pixel-green/40 px-3 py-2 font-mono text-[12px] text-ink">
                  <Bookmark class="h-3.5 w-3.5" />
                  当前词书
                </span>
                <RouterLink
                  to="/tutor"
                  class="inline-flex items-center gap-2 rounded-md border border-rule bg-paper px-4 text-[13px] text-ink transition-colors hover:bg-paper-3 focus-ring h-10"
                >
                  <BookOpen class="h-3.5 w-3.5" />
                  在 AI 导师中讨论
                </RouterLink>
              </div>
            </div>
          </header>

          <!-- Filter -->
          <div class="mb-4 flex flex-wrap items-center gap-1 rounded-md border border-rule bg-paper p-1">
            <Filter class="ml-2 h-3.5 w-3.5 text-ink-soft" />
            <button
              class="inline-flex h-8 items-center gap-1.5 rounded-sm px-3 text-[12.5px] transition-colors duration-200 focus-ring"
              :class="filter === 'ALL' ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
              @click="filter = 'ALL'"
            >
              全部 <span class="font-mono">{{ counters.all }}</span>
            </button>
            <button
              v-for="(meta, st) in stateMap"
              :key="st"
              class="inline-flex h-8 items-center gap-1.5 rounded-sm px-3 text-[12.5px] transition-colors duration-200 focus-ring"
              :class="filter === st ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
              @click="filter = st as WordState"
            >
              <component :is="meta.icon" class="h-3 w-3" />
              {{ meta.label }} <span class="font-mono">{{ counters[st] }}</span>
            </button>
          </div>

          <!-- Words list -->
          <div class="grid gap-[2px] overflow-hidden rounded-md border border-rule bg-rule">
            <article
              v-for="w in filtered"
              :key="w.id"
              class="bg-paper p-5"
            >
              <div class="grid gap-4 lg:grid-cols-[1fr_auto]">
                <div>
                  <div class="flex items-baseline gap-3">
                    <span class="font-display text-[26px] font-black leading-none tracking-tight">{{ w.word }}</span>
                    <span class="font-mono text-[12px] text-ink-soft">{{ w.phonetic }} · {{ w.partOfSpeech }}</span>
                  </div>
                  <p class="mt-3 text-[15px] text-ink">{{ w.meaningZh }}</p>
                  <p v-if="w.meaningEn" class="mt-1 text-[12.5px] italic text-ink-soft">{{ w.meaningEn }}</p>
                  <p v-if="w.exampleEn" class="mt-3 rounded-sm border-l-0 border border-rule-soft bg-paper-2 p-2.5 text-[13.5px] text-ink">
                    <span class="block">{{ w.exampleEn }}</span>
                    <span v-if="w.exampleZh" class="mt-1 block text-ink-soft">{{ w.exampleZh }}</span>
                  </p>
                </div>
                <div class="flex flex-row items-start gap-2 lg:flex-col lg:items-end">
                  <div class="inline-flex items-center gap-1.5 rounded-sm bg-paper-3 px-2 py-1 font-mono text-[11px]" :class="stateMap[w.state].color">
                    <component :is="stateMap[w.state].icon" class="h-3 w-3" />
                    {{ stateMap[w.state].label }}
                  </div>
                  <div class="inline-flex rounded-md border border-rule bg-paper p-0.5">
                    <button
                      class="inline-flex h-7 items-center gap-1 rounded-sm px-2 text-[11.5px] transition-colors focus-ring"
                      :class="w.state === 'LEARNING' ? 'bg-warning/20 text-ink font-medium' : 'text-ink-soft hover:text-ink'"
                      :disabled="marking === w.id"
                      @click="mark(w, 'LEARNING')"
                    >
                      <Sparkles class="h-3 w-3" /> 学习
                    </button>
                    <button
                      class="inline-flex h-7 items-center gap-1 rounded-sm px-2 text-[11.5px] transition-colors focus-ring"
                      :class="w.state === 'MASTERED' ? 'bg-pixel-green/40 text-ink font-medium' : 'text-ink-soft hover:text-ink'"
                      :disabled="marking === w.id"
                      @click="mark(w, 'MASTERED')"
                    >
                      <Loader2 v-if="marking === w.id" class="h-3 w-3 animate-spin" />
                      <Check v-else class="h-3 w-3" />
                      掌握
                    </button>
                  </div>
                </div>
              </div>
            </article>
            <div v-if="filtered.length === 0" class="bg-paper p-10 text-center text-[13.5px] text-ink-soft">
              该状态下暂无单词。
            </div>
          </div>
        </template>
      </div>
    </section>
  </AppLayout>
</template>
