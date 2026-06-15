<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Dice5, Play, Flag, Loader2, TrendingUp, Coins, Sparkles, Check, X } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import Board from '@/components/game/Board.vue'
import Dice from '@/components/game/Dice.vue'
import QuestionModal from '@/components/game/QuestionModal.vue'
import { useGameStore } from '@/stores/game'
import { useAuthStore } from '@/stores/auth'

const game = useGameStore()
const auth = useAuthStore()
const answering = ref(false)
const error = ref<string | null>(null)

const hasGame = computed(() => game.game && game.game.status === 'IN_PROGRESS')
const tileNow = computed(() =>
  game.board.find((t) => t.position === (game.game?.currentPosition ?? 0)),
)

onMounted(async () => {
  await game.loadBoard()
  await game.loadCurrent()
})

async function startNew() {
  error.value = null
  try {
    await game.createGame()
  } catch (e: any) {
    error.value = e?.message ?? 'Failed to start a new game'
  }
}

async function roll() {
  error.value = null
  try {
    await game.rollDice()
    await auth.refreshMe()
  } catch (e: any) {
    error.value = e?.message ?? 'Roll failed'
  }
}

async function submitAnswer(answer: string) {
  answering.value = true
  try {
    await game.submitAnswer(answer)
    await auth.refreshMe()
  } catch (e: any) {
    error.value = e?.message ?? 'Answer failed'
  } finally {
    answering.value = false
  }
}

function dismissToast() {
  game.clearAnswerToast()
}
</script>

<template>
  <AppLayout>
    <section class="container py-10">
      <div class="grid gap-6 lg:grid-cols-[1fr_320px]">
        <!-- Board column -->
        <div>
          <div class="mb-4 flex items-center justify-between">
            <div>
              <div class="text-xs uppercase tracking-wider text-muted">Lingo Quest</div>
              <h1 class="text-2xl font-semibold tracking-tight">The board</h1>
            </div>
            <div class="flex items-center gap-2">
              <Button v-if="!hasGame" @click="startNew">
                <Play class="h-4 w-4" />
                Start a new game
              </Button>
              <Button v-else variant="ghost" @click="game.abandon()">
                <Flag class="h-4 w-4" />
                Abandon
              </Button>
            </div>
          </div>

          <Board
            :tiles="game.board"
            :position="game.game?.currentPosition ?? 0"
            :rolling="game.rolling"
          />

          <div v-if="error" class="mt-3 rounded-md border border-danger/40 bg-danger/10 p-3 text-xs text-danger">
            {{ error }}
          </div>
        </div>

        <!-- Side panel -->
        <aside class="space-y-4">
          <div class="card-surface flex items-center gap-4 p-4">
            <Dice :value="game.diceValue" :rolling="game.rolling" />
            <div class="flex-1">
              <div class="text-xs uppercase tracking-wider text-muted">Dice</div>
              <div class="text-2xl font-semibold">{{ game.diceValue ?? '—' }}</div>
              <Button
                size="sm"
                class="mt-2"
                :disabled="!hasGame || game.rolling || !!game.pendingQuestion"
                @click="roll"
              >
                <Loader2 v-if="game.rolling" class="h-3.5 w-3.5 animate-spin" />
                <Dice5 v-else class="h-3.5 w-3.5" />
                Roll
              </Button>
            </div>
          </div>

          <div class="card-surface p-4">
            <div class="text-xs uppercase tracking-wider text-muted">Current tile</div>
            <div v-if="tileNow" class="mt-2">
              <div class="font-medium">{{ tileNow.tileName }}</div>
              <div class="mt-1 text-xs text-muted">
                Type: <span class="font-mono">{{ tileNow.tileType }}</span>
              </div>
            </div>
            <div v-else class="mt-2 text-sm text-muted">Awaiting board…</div>
          </div>

          <div class="card-surface p-4">
            <div class="mb-3 text-xs uppercase tracking-wider text-muted">You</div>
            <div class="flex items-center justify-between text-sm">
              <span class="inline-flex items-center gap-1.5 text-muted"><TrendingUp class="h-4 w-4" /> Level</span>
              <span class="font-semibold">{{ auth.user?.level ?? 1 }}</span>
            </div>
            <div class="mt-2 flex items-center justify-between text-sm">
              <span class="inline-flex items-center gap-1.5 text-muted"><Sparkles class="h-4 w-4" /> XP</span>
              <span class="font-semibold">{{ auth.user?.experience ?? 0 }}</span>
            </div>
            <div class="mt-2 flex items-center justify-between text-sm">
              <span class="inline-flex items-center gap-1.5 text-muted"><Coins class="h-4 w-4" /> Coins</span>
              <span class="font-semibold">{{ auth.user?.coins ?? 0 }}</span>
            </div>
            <div v-if="game.game" class="mt-4 border-t border-border pt-3 text-xs text-muted">
              Round <span class="font-mono text-foreground">{{ game.game.roundNum }}</span> ·
              Score <span class="font-mono text-foreground">{{ game.game.totalScore }}</span>
            </div>
          </div>
        </aside>
      </div>
    </section>

    <!-- Question modal -->
    <QuestionModal
      :question="game.pendingQuestion"
      :loading="answering"
      @submit="submitAnswer"
      @close="game.pendingQuestion = null"
    />

    <!-- Toast for last answer -->
    <Transition enter-active-class="transition" enter-from-class="opacity-0 translate-y-2" leave-active-class="transition" leave-to-class="opacity-0">
      <div
        v-if="game.lastAnswer"
        class="fixed bottom-6 right-6 z-40 max-w-sm rounded-md border bg-surface/95 p-4 text-sm shadow-glow-lg backdrop-blur"
        :class="game.lastAnswer.correct ? 'border-success/40' : 'border-danger/40'"
      >
        <div class="flex items-start gap-3">
          <Check v-if="game.lastAnswer.correct" class="h-5 w-5 shrink-0 text-success" />
          <X v-else class="h-5 w-5 shrink-0 text-danger" />
          <div class="flex-1">
            <div class="font-medium">{{ game.lastAnswer.correct ? 'Correct!' : 'Not quite' }}</div>
            <div class="mt-1 text-xs text-muted">
              Answer: <span class="font-mono text-foreground">{{ game.lastAnswer.correctAnswer }}</span>
            </div>
            <div class="mt-1 text-xs text-muted">
              +{{ game.lastAnswer.gainedExp }} XP · Total {{ game.lastAnswer.totalExp }}
              <span v-if="game.lastAnswer.levelUp" class="ml-1 text-accent">· Level up!</span>
            </div>
          </div>
          <button class="text-muted transition hover:text-foreground" @click="dismissToast">
            <X class="h-4 w-4" />
          </button>
        </div>
      </div>
    </Transition>
  </AppLayout>
</template>
