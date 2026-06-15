<script setup lang="ts">
import { computed } from 'vue'
import { cn } from '@/lib/utils'
import {
  BookOpen,
  Pencil,
  MessageSquare,
  Sparkles,
  AlertTriangle,
  Crown,
  Flag,
  Swords,
  Loader2,
} from 'lucide-vue-next'
import type { TileView } from '@/api/game'

const props = defineProps<{
  tiles: TileView[]
  position: number
  rolling: boolean
}>()

const ordered = computed(() => [...props.tiles].sort((a, b) => a.position - b.position))

const icons: Record<TileView['tileType'], any> = {
  START: Flag,
  END: Crown,
  VOCAB: BookOpen,
  GRAMMAR: Pencil,
  SPEAKING: MessageSquare,
  LUCKY: Sparkles,
  TRAP: AlertTriangle,
  CHALLENGE: Swords,
}

const tileTone: Record<TileView['tileType'], string> = {
  START:     'from-emerald-500/20 to-emerald-500/5 border-emerald-500/30 text-emerald-300',
  END:       'from-amber-500/30 to-amber-500/5 border-amber-500/40 text-amber-300',
  VOCAB:     'from-sky-500/20 to-sky-500/5 border-sky-500/30 text-sky-300',
  GRAMMAR:   'from-violet-500/20 to-violet-500/5 border-violet-500/30 text-violet-300',
  SPEAKING:  'from-fuchsia-500/20 to-fuchsia-500/5 border-fuchsia-500/30 text-fuchsia-300',
  LUCKY:     'from-yellow-500/20 to-yellow-500/5 border-yellow-500/30 text-yellow-300',
  TRAP:      'from-rose-500/20 to-rose-500/5 border-rose-500/30 text-rose-300',
  CHALLENGE: 'from-orange-500/20 to-orange-500/5 border-orange-500/30 text-orange-300',
}
</script>

<template>
  <div class="card-surface relative overflow-hidden p-4">
    <div class="grid grid-cols-6 gap-2">
      <div
        v-for="t in ordered"
        :key="t.position"
        :class="cn(
          'group relative aspect-square overflow-hidden rounded-md border bg-gradient-to-br p-2 transition',
          tileTone[t.tileType],
          t.position === position ? 'ring-2 ring-accent shadow-glow scale-[1.02]' : 'hover:scale-[1.02]',
        )"
      >
        <div class="flex items-center justify-between text-[10px] font-mono opacity-70">
          <span>#{{ t.position }}</span>
          <span v-if="t.rewardExp" class="font-semibold">{{ t.rewardExp > 0 ? '+' : '' }}{{ t.rewardExp }}</span>
        </div>
        <div class="mt-1 flex items-center gap-1">
          <component :is="icons[t.tileType]" class="h-4 w-4" />
          <span class="text-[10px] font-medium uppercase tracking-wider opacity-80">{{ t.tileType }}</span>
        </div>
        <div class="absolute inset-x-1 bottom-1 truncate text-[10px] text-foreground/80">{{ t.tileName }}</div>

        <!-- Player token -->
        <div
          v-if="t.position === position"
          class="absolute right-1 top-1 flex h-5 w-5 items-center justify-center rounded-full bg-gradient-brand bg-[length:200%_200%] text-[10px] font-bold text-white shadow-glow"
        >
          <Loader2 v-if="rolling" class="h-3 w-3 animate-spin" />
          <span v-else>P</span>
        </div>
      </div>
    </div>
  </div>
</template>
