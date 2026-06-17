<script setup lang="ts">
import { cn } from '@/lib/utils'
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    variant?: 'ink' | 'pop' | 'bone' | 'ghost'
    size?: 'sm' | 'md' | 'lg'
    type?: 'button' | 'submit'
    disabled?: boolean
    loading?: boolean
  }>(),
  { variant: 'ink', size: 'md', type: 'button' },
)

const emit = defineEmits<{ click: [MouseEvent] }>()
const isDisabled = computed(() => props.disabled || props.loading)

const base =
  'inline-flex items-center justify-center gap-2 rounded-md font-medium transition-colors duration-300 ease-out-expo focus-ring disabled:cursor-not-allowed disabled:opacity-55'

const sizes = {
  sm: 'h-8 px-3 text-[13px]',
  md: 'h-10 px-4 text-sm',
  lg: 'h-12 px-6 text-[15px]',
}

const variants = {
  ink:   'bg-ink text-paper hover:bg-ink-soft',
  pop:   'bg-pixel-green text-ink hover:bg-pixel-mint',
  bone:  'border border-rule bg-paper text-ink hover:bg-paper-3',
  ghost: 'text-ink-soft hover:text-ink hover:bg-paper-3',
}
</script>

<template>
  <button
    :type="type"
    :disabled="isDisabled"
    :class="cn(base, sizes[size], variants[variant])"
    @click="(e) => emit('click', e)"
  >
    <slot />
  </button>
</template>
