<script setup lang="ts">
import { ref, watch } from 'vue'

const props = defineProps<{ value: number | null; rolling: boolean }>()

const displayValue = ref<number>(1)
const animKey = ref(0)

watch(
  () => [props.rolling, props.value] as const,
  ([rolling, value]) => {
    if (rolling) {
      const handle = window.setInterval(() => {
        displayValue.value = Math.floor(Math.random() * 6) + 1
      }, 60)
      const stop = setTimeout(() => {
        clearInterval(handle)
        if (value) {
          displayValue.value = value
          animKey.value += 1
        }
      }, 700)
      return () => {
        clearInterval(handle)
        clearTimeout(stop)
      }
    }
    if (value) {
      displayValue.value = value
      animKey.value += 1
    }
  },
)

const pipsByValue: Record<number, number[]> = {
  1: [4],
  2: [0, 8],
  3: [0, 4, 8],
  4: [0, 2, 6, 8],
  5: [0, 2, 4, 6, 8],
  6: [0, 2, 3, 5, 6, 8],
}
</script>

<template>
  <div
    :key="animKey"
    class="relative grid h-24 w-24 grid-cols-3 grid-rows-3 gap-1 rounded-2xl border border-border bg-gradient-to-br from-surface-1 to-surface p-3 shadow-glow"
    :class="rolling ? 'animate-bounce' : 'transition-transform'"
    style="perspective: 600px"
  >
    <div
      v-for="i in 9"
      :key="i"
      class="flex items-center justify-center"
    >
      <div
        v-if="pipsByValue[displayValue]?.includes(i - 1)"
        class="h-3 w-3 rounded-full bg-gradient-brand bg-[length:200%_200%] shadow-glow"
      />
    </div>
  </div>
</template>
