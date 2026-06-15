<script setup lang="ts">
import { ref } from 'vue'
import {
  Sparkles,
  Wand2,
  BookOpen,
  AlertCircle,
  Loader2,
  Check,
  ArrowRight,
  Quote,
} from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import { aiApi, type GrammarCheck, type VocabCoach } from '@/api/ai'

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
    grammarErr.value = e?.response?.data?.message || e?.message || 'Request failed'
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
    vocabErr.value = e?.response?.data?.message || e?.message || 'Request failed'
  } finally {
    vocabLoading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <section class="container py-16">
      <div class="mx-auto max-w-3xl text-center">
        <div class="mx-auto mb-4 inline-flex items-center gap-2 rounded-full border border-border bg-surface/60 px-3 py-1 text-xs text-muted backdrop-blur">
          <Sparkles class="h-3.5 w-3.5 text-accent" />
          AI Tutor · powered by DeepSeek
        </div>
        <h1 class="text-4xl font-semibold tracking-tight md:text-5xl">
          Your <span class="text-gradient-brand animate-gradient-x">private English tutor</span>
        </h1>
        <p class="mt-4 text-balance text-muted">
          Pick a tool, type your input, and get instant feedback.
        </p>
      </div>

      <div class="mx-auto mt-10 max-w-3xl">
        <div class="inline-flex rounded-md border border-border bg-surface/60 p-1 backdrop-blur">
          <button
            class="inline-flex h-9 items-center gap-2 rounded-[6px] px-4 text-sm transition"
            :class="tab === 'grammar' ? 'bg-gradient-brand bg-[length:200%_200%] text-white shadow-glow' : 'text-muted hover:text-foreground'"
            @click="tab = 'grammar'"
          >
            <Wand2 class="h-4 w-4" />
            Grammar Fix
          </button>
          <button
            class="inline-flex h-9 items-center gap-2 rounded-[6px] px-4 text-sm transition"
            :class="tab === 'vocab' ? 'bg-gradient-brand bg-[length:200%_200%] text-white shadow-glow' : 'text-muted hover:text-foreground'"
            @click="tab = 'vocab'"
          >
            <BookOpen class="h-4 w-4" />
            Vocab Coach
          </button>
        </div>
      </div>

      <!-- Grammar tab -->
      <div v-if="tab === 'grammar'" class="mx-auto mt-6 max-w-3xl">
        <div class="card-surface p-6">
          <label class="text-xs font-medium uppercase tracking-wider text-muted">
            English sentence
          </label>
          <Input v-model="sentence" class="mt-2" placeholder="He go to school every day." />
          <div class="mt-4 flex items-center justify-end">
            <Button :disabled="grammarLoading || !sentence" @click="checkGrammar">
              <Loader2 v-if="grammarLoading" class="h-4 w-4 animate-spin" />
              <Wand2 v-else class="h-4 w-4" />
              {{ grammarLoading ? 'Thinking…' : 'Correct grammar' }}
            </Button>
          </div>

          <div
            v-if="grammarErr"
            class="mt-4 flex items-start gap-2 rounded-md border border-danger/40 bg-danger/10 p-3 text-xs text-danger"
          >
            <AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
            <span>{{ grammarErr }}</span>
          </div>

          <div v-if="grammarResult" class="mt-6 space-y-4">
            <div class="rounded-md border border-border bg-surface-1/60 p-4">
              <div class="flex items-center gap-2 text-xs uppercase tracking-wider text-muted">
                <Check class="h-3.5 w-3.5 text-success" />
                Corrected
              </div>
              <p class="mt-2 font-mono text-sm text-foreground">{{ grammarResult.corrected }}</p>
            </div>
            <div class="rounded-md border border-border bg-surface-1/60 p-4">
              <div class="flex items-center gap-2 text-xs uppercase tracking-wider text-muted">
                <Quote class="h-3.5 w-3.5 text-accent" />
                解析
              </div>
              <p class="mt-2 text-sm leading-relaxed text-foreground">{{ grammarResult.explanation_zh }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Vocab tab -->
      <div v-else class="mx-auto mt-6 max-w-3xl">
        <div class="card-surface p-6">
          <label class="text-xs font-medium uppercase tracking-wider text-muted">
            English word
          </label>
          <Input v-model="word" class="mt-2" placeholder="abandon" />
          <div class="mt-4 flex items-center justify-end">
            <Button :disabled="vocabLoading || !word" @click="coachVocab">
              <Loader2 v-if="vocabLoading" class="h-4 w-4 animate-spin" />
              <BookOpen v-else class="h-4 w-4" />
              {{ vocabLoading ? 'Looking up…' : 'Get coaching' }}
            </Button>
          </div>

          <div
            v-if="vocabErr"
            class="mt-4 flex items-start gap-2 rounded-md border border-danger/40 bg-danger/10 p-3 text-xs text-danger"
          >
            <AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
            <span>{{ vocabErr }}</span>
          </div>

          <div v-if="vocabResult" class="mt-6 grid gap-4 md:grid-cols-2">
            <div class="rounded-md border border-border bg-surface-1/60 p-4 md:col-span-2">
              <div class="flex items-baseline justify-between">
                <span class="text-2xl font-semibold tracking-tight">{{ vocabResult.word }}</span>
                <span class="font-mono text-xs text-muted">{{ vocabResult.phonetic }} · {{ vocabResult.part_of_speech }}</span>
              </div>
              <p class="mt-3 text-sm text-foreground">{{ vocabResult.meaning_zh }}</p>
              <p class="mt-1 text-xs italic text-muted">{{ vocabResult.meaning_en }}</p>
            </div>
            <div class="rounded-md border border-border bg-surface-1/60 p-4">
              <div class="text-xs uppercase tracking-wider text-muted">Examples</div>
              <ul class="mt-2 space-y-2 text-sm">
                <li v-for="ex in vocabResult.examples" :key="ex" class="flex gap-2">
                  <ArrowRight class="mt-0.5 h-3.5 w-3.5 shrink-0 text-accent" />
                  <span>{{ ex }}</span>
                </li>
              </ul>
            </div>
            <div class="rounded-md border border-border bg-surface-1/60 p-4">
              <div class="text-xs uppercase tracking-wider text-muted">Synonyms</div>
              <div class="mt-2 flex flex-wrap gap-2">
                <span
                  v-for="s in vocabResult.synonyms"
                  :key="s"
                  class="rounded-full border border-border bg-surface-2/60 px-2.5 py-0.5 text-xs"
                >{{ s }}</span>
              </div>
              <div class="mt-4 text-xs uppercase tracking-wider text-muted">Memory tip</div>
              <p class="mt-2 text-sm leading-relaxed text-foreground">{{ vocabResult.memory_tip }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
