<script setup lang="ts">
import { Dice5, BookOpen, Trophy, ArrowRight, Github, Sparkles } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
</script>

<template>
  <AppLayout>
    <section class="container relative pb-24 pt-24">
      <div class="pointer-events-none absolute inset-x-0 -top-24 mx-auto h-[480px] max-w-5xl bg-gradient-radial" />

      <div class="relative mx-auto max-w-3xl text-center">
        <div class="mx-auto mb-6 inline-flex items-center gap-2 rounded-full border border-border bg-surface/60 px-3 py-1 text-xs text-muted backdrop-blur">
          <span class="h-1.5 w-1.5 rounded-full bg-success" />
          Powered by DeepSeek · Now in preview
        </div>
        <h1 class="text-balance text-5xl font-semibold leading-[1.05] tracking-tight md:text-6xl">
          Learn English by
          <span class="text-gradient-brand animate-gradient-x">playing a board game.</span>
        </h1>
        <p class="mt-6 text-balance text-lg text-muted">
          Roll the dice, land on challenges, and let an AI tutor turn every move
          into a vocabulary, grammar, or speaking lesson.
        </p>
        <div class="mt-8 flex items-center justify-center gap-3">
          <RouterLink
            to="/register"
            class="inline-flex h-11 items-center gap-2 rounded-md bg-gradient-brand bg-[length:200%_200%] px-6 text-sm font-medium text-white shadow-glow transition-[background-position] duration-700 hover:bg-[position:100%_50%]"
          >
            Start a quest
            <ArrowRight class="h-4 w-4" />
          </RouterLink>
          <RouterLink
            to="/tutor"
            class="inline-flex h-11 items-center gap-2 rounded-md border border-border bg-surface/60 px-6 text-sm text-foreground transition hover:bg-surface-1"
          >
            <Sparkles class="h-4 w-4 text-accent" />
            Try AI Tutor
          </RouterLink>
        </div>
        <div class="mt-4 text-xs text-muted">
          Open source on
          <a
            href="https://github.com/shanchuann/Lingo_Quest"
            target="_blank"
            class="inline-flex items-center gap-1 text-foreground underline decoration-border underline-offset-4 transition hover:decoration-accent"
          >
            <Github class="h-3.5 w-3.5" />
            GitHub
          </a>
        </div>
      </div>

      <div class="relative mx-auto mt-24 grid max-w-5xl gap-4 md:grid-cols-3">
        <article class="card-surface p-6">
          <Dice5 class="h-6 w-6 text-accent" />
          <h3 class="mt-4 text-lg font-medium">Board-game core</h3>
          <p class="mt-2 text-sm text-muted">
            A 30-tile map. Roll, move, trigger. Earn XP and level up from
            Beginner to Master.
          </p>
        </article>
        <article class="card-surface p-6">
          <Sparkles class="h-6 w-6 text-accent" />
          <h3 class="mt-4 text-lg font-medium">AI Tutor on every tile</h3>
          <p class="mt-2 text-sm text-muted">
            Grammar correction, vocabulary coaching, and role-play conversations
            powered by DeepSeek.
          </p>
        </article>
        <article class="card-surface p-6">
          <Trophy class="h-6 w-6 text-accent" />
          <h3 class="mt-4 text-lg font-medium">Compete &amp; track</h3>
          <p class="mt-2 text-sm text-muted">
            Weekly leaderboard, achievements, and a personalized progress report.
          </p>
        </article>
      </div>

      <div class="mx-auto mt-16 flex max-w-3xl items-center justify-between rounded-md border border-border bg-surface/60 px-4 py-3 text-xs text-muted backdrop-blur">
        <span class="inline-flex items-center gap-2">
          <BookOpen class="h-4 w-4 text-accent" />
          v0.1.0 · auth + AI tutor live
        </span>
        <span class="font-mono">build: <span class="text-foreground">dev</span></span>
      </div>
    </section>
  </AppLayout>
</template>
