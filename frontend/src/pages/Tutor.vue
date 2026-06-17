<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  Sparkles,
  Wand2,
  BookOpen,
  AlertCircle,
  Loader2,
  Check,
  MoveRight,
  Quote,
  Library,
  ChevronDown,
} from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import { aiApi, type GrammarCheck, type VocabCoach } from '@/api/ai'
import { wordbookApi, type WordBookSummary } from '@/api/wordbook'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const pickingFromBook = ref(false)
const books = ref<WordBookSummary[]>([])
const showBookPicker = ref(false)

const currentBook = () => books.value.find((b) => b.id === auth.user?.currentBookId) || null

onMounted(async () => {
  try { books.value = await wordbookApi.books() } catch { /* non-fatal */ }
})

async function pickBook(book: WordBookSummary) {
  showBookPicker.value = false
  pickingFromBook.value = true
  try {
    await wordbookApi.select(book.id)
    await auth.refreshMe()
    const w = await wordbookApi.random(book.id)
    if (w) {
      tab.value = 'vocab'
      word.value = w.word
      await coachVocab()
    } else {
      vocabErr.value = '这本词书是空的。'
    }
  } catch (e: any) {
    vocabErr.value = e?.response?.data?.message || '加载词书失败'
  } finally {
    pickingFromBook.value = false
  }
}

type Tab = 'grammar' | 'vocab'
const tab = ref<Tab>('grammar')

const sentence = ref('He go to school every day.')
const grammarLoading = ref(false)
const grammarErr = ref<string | null>(null)
const grammarResult = ref<GrammarCheck | null>(null)

const word = ref('abandon')
const vocabLoading = ref(false)
const vocabErr = ref<string | null>(null)
const vocabResult = ref<VocabCoach | null>(null)

async function checkGrammar() {
  grammarErr.value = null
  grammarLoading.value = true
  try {
    grammarResult.value = await aiApi.grammar(sentence.value)
  } catch (e: any) {
    grammarErr.value = e?.response?.data?.message || e?.message || '请求失败'
  } finally {
    grammarLoading.value = false
  }
}

async function coachVocab() {
  vocabErr.value = null
  vocabLoading.value = true
  try {
    vocabResult.value = await aiApi.vocab(word.value)
  } catch (e: any) {
    vocabErr.value = e?.response?.data?.message || e?.message || '请求失败'
  } finally {
    vocabLoading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <section class="container py-16">
      <div class="mx-auto max-w-3xl">
        <header>
          <div class="label-slash">
            <Sparkles class="mr-1.5 h-3.5 w-3.5" />
            AI 导师 · DeepSeek
          </div>
          <h1 class="mt-4 font-display text-display-xl font-black leading-[0.95] tracking-tight text-balance">
            一位用中文给你
            <span class="text-ink-faint">讲明白英语的小老师。</span>
          </h1>
          <p class="mt-5 max-w-[42ch] text-[16px] leading-[1.7] text-ink-soft">
            选一个工具,输入内容,得到一份结构化的反馈 —— 含例句、近义词和一个记忆钩子。
          </p>
        </header>

        <div class="mt-10 flex flex-wrap items-center gap-3">
          <div class="inline-flex rounded-md border border-rule bg-paper p-1">
            <button
              class="inline-flex h-10 items-center gap-2 rounded-sm px-4 text-[14px] transition-colors duration-200 focus-ring"
              :class="tab === 'grammar' ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
              @click="tab = 'grammar'"
            >
              <Wand2 class="h-4 w-4" />
              语法纠错
            </button>
            <button
              class="inline-flex h-10 items-center gap-2 rounded-sm px-4 text-[14px] transition-colors duration-200 focus-ring"
              :class="tab === 'vocab' ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
              @click="tab = 'vocab'"
            >
              <BookOpen class="h-4 w-4" />
              单词讲解
            </button>
          </div>
          <!-- 选择词书 — same h-10 footprint as the two tabs on its left -->
          <div class="relative">
            <button
              class="inline-flex h-10 items-center gap-2 rounded-sm bg-pixel-green px-4 text-[14px] font-medium text-ink transition-colors duration-200 hover:bg-pixel-mint focus-ring"
              :disabled="pickingFromBook"
              @click="showBookPicker = !showBookPicker"
            >
              <Loader2 v-if="pickingFromBook" class="h-4 w-4 animate-spin" />
              <Library v-else class="h-4 w-4" />
              <span class="max-w-[14ch] truncate">
                {{ currentBook()?.name || '选择词书' }}
              </span>
              <ChevronDown class="h-3.5 w-3.5 opacity-70 transition-transform" :class="showBookPicker ? 'rotate-180' : ''" />
            </button>

            <!-- Dropdown: full word-book catalog with mastered/total -->
            <Transition
              enter-active-class="transition duration-200 ease-out-expo"
              enter-from-class="opacity-0 -translate-y-1"
              leave-active-class="transition duration-150"
              leave-to-class="opacity-0"
            >
              <div
                v-if="showBookPicker"
                class="absolute right-0 top-full z-30 mt-2 w-[320px] overflow-hidden rounded-md border-2 border-ink bg-paper shadow-page"
              >
                <div class="flex items-center justify-between border-b border-rule-soft px-4 py-2.5">
                  <span class="label-slash">我的词书</span>
                  <span class="font-mono text-[11px] text-ink-faint">{{ books.length }} 本</span>
                </div>
                <ul class="max-h-[380px] overflow-y-auto py-1">
                  <li
                    v-for="b in books"
                    :key="b.id"
                    class="flex cursor-pointer items-center gap-3 px-4 py-2.5 transition-colors hover:bg-paper-3"
                    :class="b.id === auth.user?.currentBookId ? 'bg-paper-3' : ''"
                    @click="pickBook(b)"
                  >
                    <span
                      class="inline-block h-5 w-5 shrink-0 rounded-sm"
                      :class="`bg-pixel-${b.coverColor}`"
                    />
                    <div class="min-w-0 flex-1">
                      <div class="truncate text-[13.5px] text-ink">{{ b.name }}</div>
                      <div class="font-mono text-[11px] text-ink-faint">
                        {{ b.masteredCount }} / {{ b.totalWords }} · {{ b.level }}
                      </div>
                    </div>
                    <Check
                      v-if="b.id === auth.user?.currentBookId"
                      class="h-4 w-4 shrink-0 text-ink"
                    />
                  </li>
                </ul>
              </div>
            </Transition>
          </div>
        </div>

        <!-- Grammar -->
        <div v-if="tab === 'grammar'" class="mt-6">
          <div class="rounded-md border border-rule bg-paper p-6">
            <label class="label-slash">英文句子</label>
            <Input v-model="sentence" class="mt-3" placeholder="He go to school every day." />
            <div class="mt-5 flex items-center justify-end">
              <Button :disabled="grammarLoading || !sentence" @click="checkGrammar">
                <Loader2 v-if="grammarLoading" class="h-4 w-4 animate-spin" />
                <Wand2 v-else class="h-4 w-4" />
                {{ grammarLoading ? '思考中…' : '纠正语法' }}
              </Button>
            </div>

            <div v-if="grammarErr" class="mt-5 flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink">
              <AlertCircle class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
              <span>{{ grammarErr }}</span>
            </div>

            <div v-if="grammarResult" class="mt-7 grid gap-[2px] overflow-hidden rounded-md border border-rule bg-rule">
              <div class="bg-paper p-5">
                <div class="label-slash">
                  <Check class="mr-1.5 h-3.5 w-3.5 text-success" />
                  正确版本
                </div>
                <p class="mt-3 font-mono text-[14px] text-ink">{{ grammarResult.corrected }}</p>
              </div>
              <div class="bg-paper p-5">
                <div class="label-slash">
                  <Quote class="mr-1.5 h-3.5 w-3.5" />
                  讲解
                </div>
                <p class="mt-3 text-[14.5px] leading-relaxed text-ink">{{ grammarResult.explanation_zh }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Vocab -->
        <div v-else class="mt-6">
          <div class="rounded-md border border-rule bg-paper p-6">
            <label class="label-slash">英文单词</label>
            <Input v-model="word" class="mt-3" placeholder="abandon" />
            <div class="mt-5 flex items-center justify-end">
              <Button :disabled="vocabLoading || !word" @click="coachVocab">
                <Loader2 v-if="vocabLoading" class="h-4 w-4 animate-spin" />
                <BookOpen v-else class="h-4 w-4" />
                {{ vocabLoading ? '查询中…' : '开始讲解' }}
              </Button>
            </div>

            <div v-if="vocabErr" class="mt-5 flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink">
              <AlertCircle class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
              <span>{{ vocabErr }}</span>
            </div>

            <div v-if="vocabResult" class="mt-7 space-y-4">
              <div class="rounded-md border border-rule bg-paper-2 p-5">
                <div class="flex items-baseline justify-between gap-4">
                  <span class="font-display text-[40px] font-black leading-none tracking-tight">{{ vocabResult.word }}</span>
                  <span class="font-mono text-[12px] text-ink-soft">{{ vocabResult.phonetic }} · {{ vocabResult.part_of_speech }}</span>
                </div>
                <p class="mt-4 text-[15px] text-ink">{{ vocabResult.meaning_zh }}</p>
                <p class="mt-1.5 text-[13px] italic text-ink-soft">{{ vocabResult.meaning_en }}</p>
              </div>

              <div class="grid gap-4 md:grid-cols-2">
                <div class="rounded-md border border-rule bg-paper-2 p-5">
                  <div class="label-slash">例句</div>
                  <ul class="mt-3 space-y-2.5 text-[14px]">
                    <li v-for="ex in vocabResult.examples" :key="ex" class="flex gap-2">
                      <MoveRight class="mt-1 h-3.5 w-3.5 shrink-0 text-ink" />
                      <span>{{ ex }}</span>
                    </li>
                  </ul>
                </div>
                <div class="rounded-md border border-rule bg-paper-2 p-5">
                  <div class="label-slash">近义词</div>
                  <div class="mt-3 flex flex-wrap gap-1.5">
                    <span
                      v-for="s in vocabResult.synonyms"
                      :key="s"
                      class="rounded-sm border border-rule-soft bg-paper px-2.5 py-0.5 text-[12.5px] text-ink"
                    >{{ s }}</span>
                  </div>
                  <div class="mt-5 label-slash">记忆窍门</div>
                  <p class="mt-3 text-[14px] leading-relaxed text-ink">{{ vocabResult.memory_tip }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
