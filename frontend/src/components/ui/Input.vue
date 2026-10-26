<script setup lang="ts">
import { cn } from '@/lib/utils'

defineProps<{
  modelValue?: string
  type?: string
  placeholder?: string
  autocomplete?: string
  name?: string
  id?: string
  disabled?: boolean
}>()
defineEmits<{ 'update:modelValue': [string] }>()
</script>

<template>
  <input
    :id="id"
    :type="type ?? 'text'"
    :value="modelValue"
    :placeholder="placeholder"
    :autocomplete="autocomplete"
    :name="name"
    :disabled="disabled"
    :class="cn(
      'flex h-11 w-full rounded-md border border-rule bg-paper px-3.5 text-[16px] text-ink placeholder:text-ink-faint sm:text-[15px]',
      'transition-colors duration-200 focus:bg-paper focus:outline-none focus:ring-2 focus:ring-ink focus:ring-offset-2 focus:ring-offset-paper',
      'disabled:cursor-not-allowed disabled:opacity-55',
    )"
    @input="(e) => $emit('update:modelValue', (e.target as HTMLInputElement).value)"
  />
</template>
