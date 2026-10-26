<script setup lang="ts">
import { computed } from 'vue'
import { useMediaQuery } from '@vueuse/core'
import { cn } from '@/lib/utils'
import {
  BookOpen,
  Pencil,
  MessageSquareText,
  Sparkles,
  AlertTriangle,
  Crown,
  Flag,
  Swords,
  Loader2,
  MoveRight,
} from 'lucide-vue-next'
import type { TileView } from '@/api/game'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const props = defineProps<{
  tiles: TileView[]
  position: number
  rolling: boolean
}>()

const boardThemeClass = computed(() => {
  switch (auth.equipped.boardHue) {
    case 'ocean':  return 'bg-pixel-blue/8'
    case 'sakura': return 'bg-pixel-pink/8'
    default:       return ''
  }
})

// 4 columns on phones, 6 from md up. The snake path recomputes from this.
const isCompact = useMediaQuery('(max-width: 767px)')
const COLS = computed(() => (isCompact.value ? 4 : 6))
const rowHeight = computed(() => (isCompact.value ? 78 : 108))

const icons: Record<TileView['tileType'], any> = {
  START: Flag,
  END: Crown,
  VOCAB: BookOpen,
  GRAMMAR: Pencil,
  SPEAKING: MessageSquareText,
  LUCKY: Sparkles,
  TRAP: AlertTriangle,
  CHALLENGE: Swords,
}

const tileLabel: Record<TileView['tileType'], string> = {
  START: '起点',
  END: '终点',
  VOCAB: '单词',
  GRAMMAR: '语法',
  SPEAKING: '口语',
  LUCKY: '幸运',
  TRAP: '陷阱',
  CHALLENGE: '挑战',
}

// Each tile type gets a saturated pixel-color square as background,
// and the text/icon flip to ink-on-color
const tileBg: Record<TileView['tileType'], string> = {
  START:     'bg-tile-start',
  END:       'bg-tile-end',
  VOCAB:     'bg-tile-vocab',
  GRAMMAR:   'bg-tile-grammar',
  SPEAKING:  'bg-tile-speaking',
  LUCKY:     'bg-tile-lucky',
  TRAP:      'bg-tile-trap',
  CHALLENGE: 'bg-tile-challenge',
}

// Foreground (text/icon) — most tiles take ink. Dark ones take paper.
const tileFg: Record<TileView['tileType'], string> = {
  START:     'text-ink',
  END:       'text-ink',
  VOCAB:     'text-paper',
  GRAMMAR:   'text-paper',
  SPEAKING:  'text-ink',
  LUCKY:     'text-ink',
  TRAP:      'text-paper',
  CHALLENGE: 'text-ink',
}

const ordered = computed(() => [...props.tiles].sort((a, b) => a.position - b.position))

interface CellInfo {
  tile: TileView
  row: number
  col: number
  ltr: boolean
}

const cells = computed<CellInfo[]>(() => {
  const list = ordered.value
  if (list.length === 0) return []
  const cols = COLS.value
  const rows = Math.ceil(list.length / cols)
  return list.map((tile, i) => {
    const rowFromBottom = Math.floor(i / cols)
    const ltr = rowFromBottom % 2 === 0
    const inRow = i % cols
    const col = ltr ? inRow : cols - 1 - inRow
    const row = rows - 1 - rowFromBottom
    return { tile, row, col, ltr }
  })
})

const rowsCount = computed(() => {
  if (cells.value.length === 0) return 1
  return Math.max(...cells.value.map((c) => c.row)) + 1
})

const pathD = computed(() => {
  if (cells.value.length === 0) return ''
  const sorted = [...cells.value].sort((a, b) => a.tile.position - b.tile.position)
  const cellW = 100 / COLS.value
  const cellH = 100 / rowsCount.value
  return sorted
    .map((c, i) => {
      const x = c.col * cellW + cellW / 2
      const y = c.row * cellH + cellH / 2
      return `${i === 0 ? 'M' : 'L'} ${x.toFixed(2)} ${y.toFixed(2)}`
    })
    .join(' ')
})
</script>

<template>
  <div class="rounded-md border border-rule bg-paper" :class="boardThemeClass">
    <!-- header strip -->
    <div class="flex items-center justify-between border-b border-rule px-4 py-3 font-mono text-[11px] uppercase tracking-[0.18em] text-ink-soft">
      <span class="inline-flex items-center gap-2 text-ink">
        <span class="h-2 w-2 bg-pixel-green animate-pixel-blink" />
        棋盘<span class="text-ink-faint">/</span>
      </span>
      <span>
        {{ ordered.length }} 格 · 蛇形路径 · 当前
        <span class="text-ink">{{ position.toString().padStart(2, '0') }}</span>
      </span>
    </div>

    <!-- board grid + path overlay -->
    <div class="relative p-4">
      <svg
        class="pointer-events-none absolute inset-4 z-0 h-[calc(100%-2rem)] w-[calc(100%-2rem)]"
        preserveAspectRatio="none"
        viewBox="0 0 100 100"
        aria-hidden="true"
      >
        <path
          :d="pathD"
          fill="none"
          stroke="oklch(0.18 0.010 80 / 0.35)"
          stroke-width="0.35"
          stroke-dasharray="0.9 1.4"
          stroke-linecap="round"
        />
      </svg>

      <div
        class="relative z-10 grid gap-[3px]"
        :style="{
          gridTemplateColumns: `repeat(${COLS}, minmax(0, 1fr))`,
          gridTemplateRows: `repeat(${rowsCount}, minmax(${rowHeight}px, 1fr))`,
        }"
      >
        <article
          v-for="c in cells"
          :key="c.tile.position"
          :style="{ gridColumnStart: c.col + 1, gridRowStart: c.row + 1 }"
          :class="cn(
            'group relative flex h-full flex-col justify-between overflow-hidden p-1.5 transition-transform duration-300 ease-out-expo md:p-2.5',
            tileBg[c.tile.tileType],
            tileFg[c.tile.tileType],
            c.tile.position === position
              ? 'outline outline-[3px] outline-ink -translate-y-1 z-20'
              : 'hover:-translate-y-0.5',
          )"
        >
          <!-- top row: index + reward -->
          <div class="flex items-center justify-between font-mono text-[9px] uppercase tracking-[0.12em] opacity-80 md:text-[10px] md:tracking-[0.16em]">
            <span>{{ c.tile.position.toString().padStart(2, '0') }}</span>
            <span v-if="c.tile.rewardExp">
              {{ c.tile.rewardExp > 0 ? '+' : '' }}{{ c.tile.rewardExp }}
            </span>
          </div>

          <!-- middle: tile name -->
          <div class="font-display text-[11.5px] font-black uppercase leading-[1.05] tracking-tight line-clamp-2 md:text-[15px] md:leading-[1.0]">
            {{ c.tile.tileName }}
          </div>

          <!-- bottom: type label + direction hint -->
          <div class="flex items-center justify-between">
            <span class="inline-flex items-center gap-1 text-[9.5px] md:gap-1.5 md:text-[11px]">
              <component :is="icons[c.tile.tileType]" class="h-3 w-3 md:h-3.5 md:w-3.5" />
              {{ tileLabel[c.tile.tileType] }}
            </span>
            <MoveRight
              class="h-3 w-3 opacity-60 transition-transform"
              :class="c.ltr ? '' : 'rotate-180'"
            />
          </div>

          <!-- Player token overlay -->
          <div
            v-if="c.tile.position === position"
            class="pointer-events-none absolute right-1.5 top-1.5 inline-flex h-6 w-6 items-center justify-center rounded-sm bg-ink font-mono text-[10px] font-bold text-paper"
          >
            <Loader2 v-if="rolling" class="h-3 w-3 animate-spin" />
            <span v-else>P</span>
          </div>
        </article>
      </div>
    </div>

    <!-- legend -->
    <div class="flex flex-wrap items-center gap-x-5 gap-y-2 border-t border-rule px-4 py-3 text-[11.5px] text-ink-soft">
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-start" /> 起点</span>
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-vocab" /> 单词</span>
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-grammar" /> 语法</span>
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-speaking" /> 口语</span>
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-lucky" /> 幸运</span>
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-trap" /> 陷阱</span>
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-challenge" /> 挑战</span>
      <span class="inline-flex items-center gap-1.5"><span class="h-2.5 w-2.5 bg-tile-end" /> 终点</span>
    </div>
  </div>
</template>
