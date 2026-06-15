<script setup lang="ts">
import { ref, watch } from 'vue'
import { Check, X, ArrowRight, Loader2 } from 'lucide-vue-next'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import type { QuestionView } from '@/api/game'

const props = defineProps<{ question: QuestionView | null; loading: boolean }>()
const emit = defineEmits<{ submit: [string]; close: [] }>()

const selected = ref<string>('')
const free = ref<string>('')

watch(
  () => props.question?.id,
  () => {
    selected.value = ''
    free.value = ''
  },
)

function submit() {
  const value = props.question?.options?.length ? selected.value : free.value.trim()
  if (!value) return
  emit('submit', value)
}
</script>

<template>
  <Transition
    enter-active-class="transition-opacity duration-200"
    enter-from-class="opacity-0"
    leave-active-class="transition-opacity duration-150"
    leave-to-class="opacity-0"
  >
    <div
      v-if="question"
      class="fixed inset-0 z-50 flex items-center justify-center bg-canvas/80 px-4 backdrop-blur-md"
    >
      <div class="card-surface w-full max-w-lg p-6 shadow-glow-lg">
        <div class="flex items-center justify-between text-xs text-muted">
          <span class="font-mono uppercase tracking-wider">{{ question.type }} · Lv {{ question.difficulty }}</span>
          <button class="text-muted transition hover:text-foreground" @click="emit('close')">
            <X class="h-4 w-4" />
          </button>
        </div>

        <h2 class="mt-3 text-lg font-medium leading-snug">
          {{ question.content }}
        </h2>

        <div v-if="question.options?.length" class="mt-6 space-y-2">
          <label
            v-for="opt in question.options"
            :key="opt"
            class="flex cursor-pointer items-center gap-3 rounded-md border border-border bg-surface-1/60 p-3 text-sm transition hover:bg-surface-2/80"
            :class="selected === opt ? 'border-accent shadow-glow' : ''"
          >
            <input v-model="selected" type="radio" :value="opt" class="sr-only" />
            <span
              class="flex h-4 w-4 shrink-0 items-center justify-center rounded-full border border-border"
              :class="selected === opt ? 'border-accent bg-gradient-brand' : ''"
            >
              <Check v-if="selected === opt" class="h-3 w-3 text-white" />
            </span>
            <span>{{ opt }}</span>
          </label>
        </div>
        <div v-else class="mt-6 space-y-2">
          <label class="text-xs uppercase tracking-wider text-muted">Type your answer</label>
          <Input v-model="free" placeholder="Your answer" />
        </div>

        <div class="mt-6 flex items-center justify-end gap-2">
          <Button variant="ghost" :disabled="loading" @click="emit('close')">Skip</Button>
          <Button :disabled="loading || (question.options?.length ? !selected : !free)" @click="submit">
            <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
            <ArrowRight v-else class="h-4 w-4" />
            {{ loading ? 'Submitting…' : 'Submit' }}
          </Button>
        </div>
      </div>
    </div>
  </Transition>
</template>
