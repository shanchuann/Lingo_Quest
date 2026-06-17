<script setup lang="ts">
import { ref, watch, computed, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const props = defineProps<{ value: number | null; rolling: boolean }>()

const skinClass = computed(() => {
  switch (auth.equipped.diceHue) {
    case 'amber':  return 'border-pixel-amber bg-pixel-amber/15'
    case 'violet': return 'border-pixel-violet bg-pixel-violet/15'
    default:       return 'border-ink bg-paper'
  }
})
const pipClass = computed(() => {
  switch (auth.equipped.diceHue) {
    case 'amber':  return 'bg-pixel-orange'
    case 'violet': return 'bg-pixel-violet'
    default:       return 'bg-ink'
  }
})

// Tumble face is only used while `rolling` is true (random flicker for animation).
// When idle, we render props.value directly via a computed — that guarantees the
// face always matches the prop, regardless of mount order or any timer leftovers.
const tumbleFace = ref<number>(1)
const tumbleKey = ref(0)

let tumbleInterval: number | null = null
let tumbleTimeout: number | null = null

function clearTumble() {
  if (tumbleInterval !== null) { window.clearInterval(tumbleInterval); tumbleInterval = null }
  if (tumbleTimeout !== null)  { window.clearTimeout(tumbleTimeout);  tumbleTimeout  = null }
}

watch(
  () => props.rolling,
  (rolling) => {
    clearTumble()
    if (!rolling) {
      if (props.value) tumbleKey.value += 1   // bump key so the box rotation replays once
      return
    }
    tumbleInterval = window.setInterval(() => {
      tumbleFace.value = Math.floor(Math.random() * 6) + 1
    }, 70)
    // Safety net: if the server is slow, stop tumbling after 1.5s anyway.
    tumbleTimeout = window.setTimeout(clearTumble, 1500)
  },
)

onUnmounted(clearTumble)

const pips: Record<number, number[]> = {
  1: [4],
  2: [0, 8],
  3: [0, 4, 8],
  4: [0, 2, 6, 8],
  5: [0, 2, 4, 6, 8],
  6: [0, 2, 3, 5, 6, 8],
}

// The face we actually paint: random while tumbling, otherwise the real prop.
const face = computed<number>(() => {
  if (props.rolling) return tumbleFace.value
  return props.value ?? 1
})

const cells = computed(() => Array.from({ length: 9 }, (_, i) => pips[face.value]?.includes(i)))
</script>

<template>
  <div
    :key="tumbleKey"
    class="relative grid h-20 w-20 grid-cols-3 grid-rows-3 gap-[3px] rounded-md border-2 p-2"
    :class="[skinClass, rolling ? 'animate-dice-tumble' : '']"
  >
    <div v-for="(on, i) in cells" :key="i" class="flex items-center justify-center">
      <span
        v-if="on"
        class="h-2.5 w-2.5"
        :class="pipClass"
      />
    </div>
  </div>
</template>
