<script setup lang="ts">
import { computed, ref } from 'vue'
import { cn } from '@/lib/utils'

const props = withDefaults(
  defineProps<{
    variant?: 'primary' | 'ghost' | 'subtle'
    size?: 'sm' | 'md' | 'lg'
    type?: 'button' | 'submit'
    disabled?: boolean
    loading?: boolean
  }>(),
  { variant: 'primary', size: 'md', type: 'button' },
)

const emit = defineEmits<{ click: [MouseEvent] }>()
const internal = ref(false)
const isDisabled = computed(() => props.disabled || props.loading || internal.value)

const base =
  'inline-flex items-center justify-center gap-2 rounded-md font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent disabled:cursor-not-allowed disabled:opacity-60'
const sizes = { sm: 'h-8 px-3 text-xs', md: 'h-10 px-4 text-sm', lg: 'h-11 px-6 text-sm' }
const variants = {
  primary:
    'bg-gradient-brand bg-[length:200%_200%] text-white shadow-glow hover:bg-[position:100%_50%] active:scale-[0.98] transition-[background-position,transform] duration-700',
  ghost:
    'border border-border bg-surface/60 text-foreground hover:bg-surface-1 active:scale-[0.98]',
  subtle:
    'bg-surface-1 text-foreground hover:bg-surface-2 active:scale-[0.98]',
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
