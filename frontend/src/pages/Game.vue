<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import {
  Dice5,
  Dices,
  Play,
  Flag,
  Loader2,
  TrendingUp,
  Coins,
  Sparkles,
  Check,
  X,
  Compass,
  Zap,
} from 'lucide-vue-next'
import { shopApi, type InventoryEntry } from '@/api/shop'
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

const tileTypeCn: Record<string, string> = {
  START: '起点',
  END: '终点',
  VOCAB: '单词',
  GRAMMAR: '语法',
  SPEAKING: '口语',
  LUCKY: '幸运',
  TRAP: '陷阱',
  CHALLENGE: '挑战',
}

const inventory = ref<InventoryEntry[]>([])
const usingItem = ref<string | null>(null)
async function refreshInventory() {
  try { inventory.value = await shopApi.inventory() } catch { /* ignore */ }
}
const countOf = (effect: string) => inventory.value
  .filter((i) => i.effect === effect)
  .reduce((s, i) => s + i.quantity, 0)

const hasRetry = computed(() => countOf('RETRY') > 0)
// hasHint / hasSkip are read directly in QuestionModal props via countOf()

async function doRetry() {
  if (game.rolling) return
  usingItem.value = 'RETRY'
  try {
    await game.useRetry()
    await refreshInventory()
    await auth.refreshMe()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '重掷失败'
  } finally {
    usingItem.value = null
  }
}

onMounted(async () => {
  await game.loadBoard()
  await game.loadCurrent()
  await refreshInventory()
  window.addEventListener('keydown', onKey)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKey)
})

function onKey(e: KeyboardEvent) {
  if (e.key === 'Escape' && game.pendingQuestion) {
    game.pendingQuestion = null
  }
}

async function startNew() {
  error.value = null
  try {
    await game.createGame()
  } catch (e: any) {
    error.value = e?.message ?? '无法开始新游戏'
  }
}

async function roll() {
  error.value = null
  try {
    await game.rollDice()
    await auth.refreshMe()
  } catch (e: any) {
    error.value = e?.message ?? '掷骰失败'
  }
}

async function onUseHint() {
  try {
    const h = await game.useHint()
    await refreshInventory()
    window.dispatchEvent(new CustomEvent('lingo:hint', { detail: { hint: h } }))
  } catch (e: any) {
    error.value = e?.response?.data?.message || '提示获取失败'
    window.dispatchEvent(new CustomEvent('lingo:hint', { detail: { hint: null } }))
  }
}

async function onUseSkip() {
  try {
    await game.useSkip()
    await refreshInventory()
    await auth.refreshMe()
  } catch (e: any) {
    error.value = e?.response?.data?.message || '跳过失败'
  }
}

async function submitAnswer(answer: string) {
  answering.value = true
  try {
    await game.submitAnswer(answer)
    await auth.refreshMe()
    await refreshInventory()
  } catch (e: any) {
    error.value = e?.message ?? '提交答案失败'
  } finally {
    answering.value = false
  }
}

function dismissToast() {
  if (toastTimer) {
    window.clearTimeout(toastTimer)
    toastTimer = null
  }
  game.clearAnswerToast()
}

// Auto-dismiss the answer toast after 5 seconds (4.5s for a correct,
// a bit longer for a wrong answer so the user can read the correct answer).
let toastTimer: number | null = null
watch(
  () => game.lastAnswer,
  (v) => {
    if (toastTimer) {
      window.clearTimeout(toastTimer)
      toastTimer = null
    }
    if (v) {
      const ms = v.correct ? 4500 : 6000
      toastTimer = window.setTimeout(() => {
        game.clearAnswerToast()
        toastTimer = null
      }, ms)
    }
  },
)

// Land toast: when the player rolls onto LUCKY / TRAP / START / END
// (a tile that doesn't trigger a question), surface the reward/penalty.
const landToast = ref<{ type: string; name: string; exp: number; coin: number; finished: boolean } | null>(null)
let landTimer: number | null = null

watch(
  () => game.lastRoll,
  (r) => {
    if (!r) return
    // Non-question tile (LUCKY/TRAP/START/END) — show feedback toast.
    if (!r.question) {
      const t = r.landed
      landToast.value = {
        type: t.tileType,
        name: t.tileName,
        exp: t.rewardExp,
        coin: t.rewardCoin,
        finished: !!r.finished,
      }
      if (landTimer) window.clearTimeout(landTimer)
      landTimer = window.setTimeout(() => {
        landToast.value = null
        landTimer = null
      }, r.finished ? 7000 : 4500)
    }
  },
)

function dismissLand() {
  if (landTimer) { window.clearTimeout(landTimer); landTimer = null }
  landToast.value = null
}

const landMeta = computed(() => {
  if (!landToast.value) return null
  const t = landToast.value
  // Explicit class names so the Tailwind JIT can see them at build time.
  switch (t.type) {
    case 'LUCKY':
      return { title: '幸运降临', border: 'border-pixel-green', chip: 'bg-pixel-green', desc: '你踩到了一格幸运。' }
    case 'TRAP':
      return { title: '踩到陷阱', border: 'border-pixel-red',   chip: 'bg-pixel-red',   desc: '小心,这一格扣了点经验。' }
    case 'END':
      return { title: '恭喜抵达终点', border: 'border-pixel-amber', chip: 'bg-pixel-amber', desc: '本局结束 — 战绩已记入档案。' }
    case 'START':
      return { title: '出发', border: 'border-pixel-mint',  chip: 'bg-pixel-mint',  desc: '一切刚刚开始。' }
    default:
      return { title: t.name, border: 'border-pixel-blue', chip: 'bg-pixel-blue', desc: null as string | null }
  }
})

onUnmounted(() => {
  if (toastTimer) window.clearTimeout(toastTimer)
  if (landTimer) window.clearTimeout(landTimer)
})
</script>

<template>
  <AppLayout>
    <section class="container py-12">
      <!-- Page header -->
      <header class="mb-8 flex flex-wrap items-end justify-between gap-4">
        <div>
          <div class="label-slash">
            <Compass class="mr-1.5 h-3.5 w-3.5" />
            本局
          </div>
          <h1 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
            棋盘已经备好。
          </h1>
        </div>
        <div class="flex items-center gap-2">
          <Button v-if="!hasGame" @click="startNew">
            <Play class="h-4 w-4" />
            开始新一局
          </Button>
          <Button v-else variant="bone" @click="game.abandon()">
            <Flag class="h-4 w-4" />
            放弃本局
          </Button>
        </div>
      </header>

      <div class="grid items-stretch gap-6 lg:grid-cols-[1fr_340px]">
        <!-- Board column -->
        <div class="flex flex-col">
          <Board
            class="flex-1"
            :tiles="game.board"
            :position="game.game?.currentPosition ?? 0"
            :rolling="game.rolling"
          />

          <div
            v-if="error"
            class="mt-3 flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink"
          >
            <X class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
            <span>{{ error }}</span>
          </div>
        </div>

        <!-- HUD: equal-height with the board column via flex-col + flex-1 on a child -->
        <aside class="flex flex-col gap-4">
          <!-- Dice card -->
          <div class="rounded-md border border-rule bg-paper p-5">
            <div class="flex items-center gap-5">
              <Dice :value="game.diceValue" :rolling="game.rolling" />
              <div class="flex-1">
                <div class="label-slash">骰子</div>
                <div class="mt-1 flex items-baseline gap-2 font-display text-[44px] font-black leading-none tracking-tight text-ink">
                  {{ game.diceValue ?? '—' }}
                  <span class="font-mono text-[11px] uppercase tracking-[0.18em] text-ink-faint">/ 6</span>
                </div>
              </div>
            </div>
            <Button
              size="lg"
              class="mt-5 w-full"
              :disabled="!hasGame || game.rolling || !!game.pendingQuestion"
              @click="roll"
            >
              <Loader2 v-if="game.rolling" class="h-4 w-4 animate-spin" />
              <Dice5 v-else class="h-4 w-4" />
              {{ game.rolling ? '掷骰中…' : '掷骰子' }}
            </Button>
            <Button
              v-if="hasRetry && game.diceValue != null && !game.pendingQuestion"
              size="sm"
              variant="bone"
              class="mt-2 w-full"
              :disabled="usingItem === 'RETRY' || game.rolling"
              @click="doRetry"
            >
              <Loader2 v-if="usingItem === 'RETRY'" class="h-3.5 w-3.5 animate-spin" />
              <Dices v-else class="h-3.5 w-3.5" />
              用一张重掷令(剩 {{ countOf('RETRY') }} 张)
            </Button>
            <div
              v-if="auth.user?.xpDoublePending === 1"
              class="mt-3 inline-flex w-full items-center gap-2 rounded-md border-2 border-ink bg-pixel-amber/40 px-3 py-2 text-[12.5px] text-ink"
            >
              <Zap class="h-3.5 w-3.5" />
              下题答对获双倍经验
            </div>
          </div>

          <!-- Current tile -->
          <div class="rounded-md border border-rule bg-paper p-5">
            <div class="label-slash">当前所在</div>
            <template v-if="tileNow">
              <div class="mt-2 flex items-baseline justify-between gap-3">
                <div class="font-display text-[20px] font-black leading-tight tracking-tight">
                  {{ tileNow.tileName }}
                </div>
                <span class="font-mono text-[11px] uppercase tracking-[0.18em] text-ink-faint">
                  #{{ tileNow.position.toString().padStart(2, '0') }}
                </span>
              </div>
              <div class="mt-3 inline-flex items-center gap-2 bg-paper-3 px-2 py-1 text-[11.5px] text-ink">
                <span class="inline-block h-2 w-2 bg-tile-vocab" />
                {{ tileTypeCn[tileNow.tileType] ?? tileNow.tileType }}
              </div>
            </template>
            <div v-else class="mt-2 text-[13.5px] text-ink-soft">棋盘加载中…</div>
          </div>

          <!-- Player stats (flex-1 so the column stretches to the board's height) -->
          <div class="flex flex-1 flex-col rounded-md border border-rule bg-paper p-5">
            <div class="label-slash">玩家</div>
            <dl class="mt-3 space-y-2.5 text-[14px]">
              <div class="flex items-center justify-between">
                <dt class="inline-flex items-center gap-2 text-ink-soft"><TrendingUp class="h-4 w-4" /> 等级</dt>
                <dd class="font-mono text-ink">Lv {{ auth.user?.level ?? 1 }}</dd>
              </div>
              <div class="flex items-center justify-between">
                <dt class="inline-flex items-center gap-2 text-ink-soft"><Sparkles class="h-4 w-4" /> 经验</dt>
                <dd class="font-mono text-ink">{{ auth.user?.experience ?? 0 }}</dd>
              </div>
              <div class="flex items-center justify-between">
                <dt class="inline-flex items-center gap-2 text-ink-soft"><Coins class="h-4 w-4" /> 金币</dt>
                <dd class="font-mono text-ink">{{ auth.user?.coins ?? 0 }}</dd>
              </div>
            </dl>
            <div v-if="game.game" class="mt-auto flex items-center justify-between border-t border-rule-soft pt-3 font-mono text-[11px] uppercase tracking-[0.18em] text-ink-soft">
              <span>回合 {{ game.game.roundNum }}</span>
              <span>得分 {{ game.game.totalScore }}</span>
            </div>
          </div>
        </aside>
      </div>
    </section>

    <!-- Question modal -->
    <QuestionModal
      :question="game.pendingQuestion"
      :loading="answering"
      :hint-count="countOf('HINT')"
      :skip-count="countOf('SKIP')"
      @submit="submitAnswer"
      @close="game.pendingQuestion = null"
      @use-hint="onUseHint"
      @use-skip="onUseSkip"
    />

    <!-- Toast for last answer -->
    <Transition
      enter-active-class="transition duration-300 ease-out-expo"
      enter-from-class="opacity-0 translate-y-3"
      leave-active-class="transition duration-200"
      leave-to-class="opacity-0"
    >
      <div
        v-if="game.lastAnswer"
        class="fixed bottom-6 right-6 z-40 w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-md border-2 bg-paper shadow-page"
        :class="game.lastAnswer.correct ? 'border-pixel-green' : 'border-pixel-red'"
      >
        <div class="flex items-start gap-3 p-4">
          <span
            class="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-sm"
            :class="game.lastAnswer.correct ? 'bg-pixel-green text-ink' : 'bg-pixel-red text-paper'"
          >
            <Check v-if="game.lastAnswer.correct" class="h-4 w-4" />
            <X v-else class="h-4 w-4" />
          </span>
          <div class="flex-1">
            <div class="font-display text-[18px] font-black leading-tight tracking-tight">
              {{ game.lastAnswer.correct ? '答对了。' : '差一点。' }}
            </div>
            <div class="mt-1 text-[12.5px] text-ink-soft">
              正确答案:<span class="font-mono text-ink">{{ game.lastAnswer.correctAnswer }}</span>
            </div>
            <div class="mt-1 font-mono text-[11px] uppercase tracking-[0.16em] text-ink-soft">
              +{{ game.lastAnswer.gainedExp }} xp · 共 {{ game.lastAnswer.totalExp }}
              <span v-if="game.lastAnswer.levelUp" class="ml-1 text-ink">· 升级!</span>
            </div>
          </div>
          <button class="rounded-sm p-1 text-ink-soft transition-colors hover:text-ink focus-ring" @click="dismissToast" aria-label="关闭">
            <X class="h-4 w-4" />
          </button>
        </div>
      </div>
    </Transition>

    <!-- Toast for landing on LUCKY / TRAP / END / START -->
    <Transition
      enter-active-class="transition duration-300 ease-out-expo"
      enter-from-class="opacity-0 translate-y-3"
      leave-active-class="transition duration-200"
      leave-to-class="opacity-0"
    >
      <div
        v-if="landToast && landMeta"
        class="fixed bottom-6 left-6 z-40 w-[360px] max-w-[calc(100vw-2rem)] overflow-hidden rounded-md border-2 bg-paper shadow-page"
        :class="landMeta.border"
      >
        <div class="flex items-start gap-3 p-4">
          <span
            class="inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-sm text-ink"
            :class="landMeta.chip"
          >
            <Sparkles class="h-4 w-4" />
          </span>
          <div class="flex-1">
            <div class="font-display text-[18px] font-black leading-tight tracking-tight">
              {{ landMeta.title }}
            </div>
            <div class="mt-1 text-[12.5px] text-ink-soft">
              {{ landToast.name }} <span v-if="landMeta.desc" class="ml-1">· {{ landMeta.desc }}</span>
            </div>
            <div class="mt-1 font-mono text-[11px] uppercase tracking-[0.16em] text-ink-soft">
              <span v-if="landToast.exp !== 0">{{ landToast.exp > 0 ? '+' : '' }}{{ landToast.exp }} xp</span>
              <span v-if="landToast.coin !== 0" class="ml-2">{{ landToast.coin > 0 ? '+' : '' }}{{ landToast.coin }} coin</span>
              <span v-if="landToast.finished" class="ml-2 text-ink">· 本局结束</span>
            </div>
          </div>
          <button class="rounded-sm p-1 text-ink-soft transition-colors hover:text-ink focus-ring" @click="dismissLand" aria-label="关闭">
            <X class="h-4 w-4" />
          </button>
        </div>
      </div>
    </Transition>
  </AppLayout>
</template>
