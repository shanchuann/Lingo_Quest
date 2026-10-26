<script setup lang="ts">
import { ref, watch } from 'vue'
import { Check, X, MoveRight, Loader2, Lightbulb, SkipForward } from 'lucide-vue-next'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import type { QuestionView } from '@/api/game'

const props = defineProps<{
  question: QuestionView | null
  loading: boolean
  hintCount?: number
  skipCount?: number
}>()
const emit = defineEmits<{
  submit: [string]
  close: [];
  useHint: []
  useSkip: []
}>()

const hint = ref<string | null>(null)
const requestingHint = ref(false)
const requestingSkip = ref(false)

const selected = ref<string>('')
const free = ref<string>('')

watch(
  () => props.question?.id,
  () => {
    selected.value = ''
    free.value = ''
    hint.value = null
  },
)

async function onHint() {
  requestingHint.value = true
  try {
    const evt = new Promise<string | null>((resolve) => {
      const listener = (e: any) => {
        window.removeEventListener('lingo:hint', listener)
        resolve(e.detail?.hint ?? null)
      }
      window.addEventListener('lingo:hint', listener as any, { once: true })
      emit('useHint')
      // safety timeout
      window.setTimeout(() => resolve(null), 4000)
    })
    const h = await evt
    if (h) hint.value = h
  } finally {
    requestingHint.value = false
  }
}

async function onSkip() {
  requestingSkip.value = true
  try {
    emit('useSkip')
  } finally {
    requestingSkip.value = false
  }
}

function submit() {
  const value = props.question?.options?.length ? selected.value : free.value.trim()
  if (!value) return
  emit('submit', value)
}

const letters = ['A', 'B', 'C', 'D', 'E', 'F']
const typeMap: Record<string, string> = {
  VOCAB: '单词',
  GRAMMAR: '语法',
  SPEAKING: '口语',
  CHALLENGE: '挑战',
}
</script>

<template>
  <Transition
    enter-active-class="transition-opacity duration-200 ease-out"
    enter-from-class="opacity-0"
    leave-active-class="transition-opacity duration-150 ease-out"
    leave-to-class="opacity-0"
  >
    <div
      v-if="question"
      class="fixed inset-0 z-50 flex items-end justify-center bg-ink/30 sm:items-center sm:px-4"
      @click.self="emit('close')"
    >
      <div class="max-h-[92dvh] w-full max-w-xl overflow-y-auto rounded-t-md border-2 border-ink bg-paper shadow-page sm:my-8 sm:rounded-md">
        <!-- ribbon strip -->
        <div class="flex items-center justify-between border-b-2 border-ink px-4 py-3 text-[12px] sm:px-6">
          <span class="text-ink-soft">
            <span class="text-ink font-medium">{{ typeMap[question.type] ?? question.type }}</span>
            <span class="mx-2 text-ink-faint">·</span>
            <span class="font-mono">难度 Lv {{ question.difficulty }}</span>
          </span>
          <button
            class="rounded-sm p-1 text-ink-soft transition-colors hover:text-ink focus-ring"
            @click="emit('close')"
            aria-label="关闭"
          >
            <X class="h-4 w-4" />
          </button>
        </div>

        <div class="px-4 pb-5 pt-4 sm:px-6 sm:pb-6 sm:pt-5">
          <div class="label-slash mb-3">题面</div>
          <h2 class="font-display text-[22px] font-black leading-[1.15] tracking-tight text-balance text-ink sm:text-[26px]">
            {{ question.content }}
          </h2>

          <div v-if="question.options?.length" class="mt-7 space-y-2">
            <label
              v-for="(opt, i) in question.options"
              :key="opt"
              class="grid cursor-pointer grid-cols-[24px_1fr_16px] items-center gap-2.5 rounded-sm border bg-paper-2 px-3 py-2.5 text-[14px] text-ink transition-colors duration-200 hover:bg-paper-3 sm:grid-cols-[28px_1fr_20px] sm:gap-3 sm:px-3.5 sm:py-3 sm:text-[15px]"
              :class="selected === opt ? 'border-2 border-ink bg-pixel-green/35' : 'border-rule-soft'"
            >
              <span
                class="inline-flex h-7 w-7 items-center justify-center rounded-sm border font-mono text-[11px]"
                :class="selected === opt ? 'border-ink bg-ink text-paper' : 'border-rule-soft bg-paper text-ink-soft'"
              >{{ letters[i] }}</span>
              <span>{{ opt }}</span>
              <input v-model="selected" type="radio" :value="opt" class="sr-only" />
              <Check
                v-if="selected === opt"
                class="h-4 w-4 text-ink"
              />
            </label>
          </div>
          <div v-else class="mt-7 space-y-2">
            <label class="label-slash">输入你的答案</label>
            <Input v-model="free" placeholder="在此输入…" />
          </div>

          <div class="mt-6 flex flex-col gap-3 sm:mt-7 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex flex-wrap items-center gap-2">
              <Button
                v-if="(hintCount ?? 0) > 0 && !hint"
                size="sm"
                variant="bone"
                :disabled="requestingHint || loading"
                @click="onHint"
              >
                <Loader2 v-if="requestingHint" class="h-3.5 w-3.5 animate-spin" />
                <Lightbulb v-else class="h-3.5 w-3.5" />
                提示({{ hintCount }})
              </Button>
              <Button
                v-if="(skipCount ?? 0) > 0"
                size="sm"
                variant="bone"
                :disabled="requestingSkip || loading"
                @click="onSkip"
              >
                <Loader2 v-if="requestingSkip" class="h-3.5 w-3.5 animate-spin" />
                <SkipForward v-else class="h-3.5 w-3.5" />
                越格符({{ skipCount }})
              </Button>
            </div>
            <div class="flex w-full items-center gap-2 sm:w-auto">
              <Button variant="ghost" :disabled="loading" @click="emit('close')">关闭</Button>
              <Button class="flex-1 sm:flex-none" :disabled="loading || (question.options?.length ? !selected : !free)" @click="submit">
                <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
                <MoveRight v-else class="h-4 w-4" />
                {{ loading ? '提交中…' : '提交答案' }}
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>
