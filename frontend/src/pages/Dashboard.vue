<script setup lang="ts">
import { onMounted } from 'vue'
import { Trophy, Coins, TrendingUp, Sparkles, Dice5 } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

onMounted(() => {
  auth.refreshMe().catch(() => {})
})
</script>

<template>
  <AppLayout>
    <section class="container py-12">
      <div class="mx-auto max-w-5xl">
        <div class="flex items-end justify-between">
          <div>
            <div class="text-xs uppercase tracking-wider text-muted">Welcome back</div>
            <h1 class="mt-1 text-3xl font-semibold tracking-tight">
              {{ auth.user?.username || 'Scholar' }}
            </h1>
          </div>
          <div class="inline-flex items-center gap-2 rounded-full border border-border bg-surface/60 px-3 py-1 text-xs text-muted backdrop-blur">
            <Sparkles class="h-3.5 w-3.5 text-accent" />
            Lv.{{ auth.user?.level ?? 1 }}
          </div>
        </div>

        <div class="mt-8 grid gap-4 md:grid-cols-3">
          <article class="card-surface p-5">
            <TrendingUp class="h-5 w-5 text-accent" />
            <div class="mt-3 text-xs uppercase tracking-wider text-muted">Experience</div>
            <div class="mt-1 text-2xl font-semibold">{{ auth.user?.experience ?? 0 }}</div>
          </article>
          <article class="card-surface p-5">
            <Coins class="h-5 w-5 text-accent" />
            <div class="mt-3 text-xs uppercase tracking-wider text-muted">Coins</div>
            <div class="mt-1 text-2xl font-semibold">{{ auth.user?.coins ?? 0 }}</div>
          </article>
          <article class="card-surface p-5">
            <Trophy class="h-5 w-5 text-accent" />
            <div class="mt-3 text-xs uppercase tracking-wider text-muted">Achievements</div>
            <div class="mt-1 text-2xl font-semibold">0</div>
          </article>
        </div>

        <div class="mt-10 grid gap-4 md:grid-cols-2">
          <RouterLink
            to="/game"
            class="card-surface group relative flex items-center gap-4 overflow-hidden p-6 transition hover:border-accent/60 hover:shadow-glow"
          >
            <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-md bg-gradient-brand bg-[length:200%_200%] text-white shadow-glow">
              <Dice5 class="h-7 w-7" />
            </div>
            <div class="flex-1">
              <div class="font-medium">Start a quest</div>
              <div class="mt-1 text-sm text-muted">Roll the dice, answer the tile, earn XP.</div>
            </div>
          </RouterLink>
          <RouterLink
            to="/tutor"
            class="card-surface group relative flex items-center gap-4 overflow-hidden p-6 transition hover:border-accent/60 hover:shadow-glow"
          >
            <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-md border border-border bg-surface-1 text-accent">
              <Sparkles class="h-7 w-7" />
            </div>
            <div class="flex-1">
              <div class="font-medium">Ask the AI tutor</div>
              <div class="mt-1 text-sm text-muted">Grammar fixes &amp; vocabulary coaching, on demand.</div>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
