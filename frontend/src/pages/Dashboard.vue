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

        <div class="mt-10 card-surface p-8 text-center">
          <Dice5 class="mx-auto h-10 w-10 text-accent" />
          <h2 class="mt-4 text-xl font-medium">The board is being assembled</h2>
          <p class="mt-2 text-sm text-muted">
            Game center launches in the next milestone. For now, sharpen your skills with the AI Tutor.
          </p>
          <RouterLink
            to="/tutor"
            class="mt-6 inline-flex h-10 items-center gap-2 rounded-md bg-gradient-brand bg-[length:200%_200%] px-5 text-sm font-medium text-white shadow-glow transition-[background-position] duration-700 hover:bg-[position:100%_50%]"
          >
            <Sparkles class="h-4 w-4" />
            Open AI Tutor
          </RouterLink>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
