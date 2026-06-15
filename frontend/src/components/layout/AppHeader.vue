<script setup lang="ts">
import { Sparkles } from 'lucide-vue-next'
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const initial = computed(() => auth.user?.username?.[0]?.toUpperCase() ?? '?')
</script>

<template>
  <header class="sticky top-0 z-30 border-b border-border/60 bg-canvas/70 backdrop-blur-xl">
    <div class="container flex h-16 items-center justify-between">
      <RouterLink to="/" class="flex items-center gap-2 transition hover:opacity-90">
        <Sparkles class="h-5 w-5 text-accent" />
        <span class="font-mono text-sm tracking-tight text-foreground">
          Lingo<span class="text-accent">.</span>Quest
        </span>
      </RouterLink>

      <nav class="hidden items-center gap-6 text-sm text-muted md:flex">
        <RouterLink class="transition hover:text-foreground" to="/">Home</RouterLink>
        <template v-if="auth.isAuthed">
          <RouterLink class="transition hover:text-foreground" to="/dashboard">Dashboard</RouterLink>
          <RouterLink class="transition hover:text-foreground" to="/game">Game</RouterLink>
          <RouterLink class="transition hover:text-foreground" to="/tutor">AI Tutor</RouterLink>
          <RouterLink class="transition hover:text-foreground" to="/ranking">Leaderboard</RouterLink>
          <RouterLink class="transition hover:text-foreground" to="/achievements">Achievements</RouterLink>
          <RouterLink class="transition hover:text-foreground" to="/report">Report</RouterLink>
        </template>
      </nav>

      <div class="flex items-center gap-3">
        <template v-if="auth.isAuthed">
          <div
            class="flex h-9 w-9 items-center justify-center rounded-full bg-gradient-brand text-sm font-semibold text-white"
            :title="auth.user?.username"
          >
            {{ initial }}
          </div>
          <button class="text-sm text-muted transition hover:text-foreground" @click="auth.logout()">
            Sign out
          </button>
        </template>
        <template v-else>
          <RouterLink
            class="text-sm text-muted transition hover:text-foreground"
            to="/login"
          >
            Sign in
          </RouterLink>
          <RouterLink
            class="inline-flex h-9 items-center rounded-md bg-gradient-brand bg-[length:200%_200%] px-4 text-sm font-medium text-white shadow-glow transition-[background-position] duration-700 hover:bg-[position:100%_50%]"
            to="/register"
          >
            Get started
          </RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>
