<script setup lang="ts">
import { cn } from '@/lib/utils'

defineProps<{
  modelValue?: string
  type?: string
  placeholder?: string
  autocomplete?: string
  name?: string
  disabled?: boolean
}>()
defineEmits<{ 'update:modelValue': [string] }>()
</script>

<template>
  <input
    :type="type ?? 'text'"
    :value="modelValue"
    :placeholder="placeholder"
    :autocomplete="autocomplete"
    :name="name"
    :disabled="disabled"
    :class="cn(
      'flex h-10 w-full rounded-md border border-border bg-surface/80 px-3 py-2 text-sm text-foreground placeholder:text-muted',
      'transition focus:border-accent focus:outline-none focus:ring-2 focus:ring-accent/40',
      'disabled:cursor-not-allowed disabled:opacity-60',
    )"
    @input="(e) => $emit('update:modelValue', (e.target as HTMLInputElement).value)"
  />
</template>
