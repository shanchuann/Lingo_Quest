<script setup lang="ts">
import {
  Sparkles,
  Dice5,
  BookOpen,
  Trophy,
  ArrowRight,
  Github,
} from 'lucide-vue-next'
</script>

<template>
  <main class="relative min-h-screen overflow-hidden">
    <!-- Top navigation -->
    <header
      class="sticky top-0 z-30 border-b border-border/60 bg-canvas/70 backdrop-blur-xl"
    >
      <div class="container flex h-16 items-center justify-between">
        <div class="flex items-center gap-2">
          <Sparkles class="h-5 w-5 text-accent" />
          <span class="font-mono text-sm tracking-tight text-foreground">
            Lingo<span class="text-accent">.</span>Quest
          </span>
        </div>
        <nav class="hidden items-center gap-6 text-sm text-muted md:flex">
          <a class="transition hover:text-foreground" href="#features">Features</a>
          <a class="transition hover:text-foreground" href="#game">Game</a>
          <a class="transition hover:text-foreground" href="#ai">AI Tutor</a>
        </nav>
        <button
          class="inline-flex h-9 items-center gap-2 rounded-md bg-gradient-brand bg-[length:200%_200%] px-4 text-sm font-medium text-white shadow-glow transition-[background-position] duration-700 hover:bg-[position:100%_50%]"
        >
          Sign in
          <ArrowRight class="h-4 w-4" />
        </button>
      </div>
    </header>

    <!-- Hero -->
    <section class="container relative pb-24 pt-28">
      <div
        class="pointer-events-none absolute inset-x-0 -top-24 mx-auto h-[480px] max-w-5xl bg-gradient-radial"
      />
      <div class="relative mx-auto max-w-3xl text-center">
        <div
          class="mx-auto mb-6 inline-flex items-center gap-2 rounded-full border border-border bg-surface/60 px-3 py-1 text-xs text-muted backdrop-blur"
        >
          <span class="h-1.5 w-1.5 rounded-full bg-success" />
          Powered by DeepSeek · Now in preview
        </div>
        <h1
          class="text-balance text-5xl font-semibold leading-[1.05] tracking-tight md:text-6xl"
        >
          Learn English by
          <span
            class="text-gradient-brand animate-gradient-x"
          >playing a board game.</span>
        </h1>
        <p class="mt-6 text-balance text-lg text-muted">
          Roll the dice, land on challenges, and let an AI tutor turn every move
          into a vocabulary, grammar, or speaking lesson.
        </p>
        <div class="mt-8 flex items-center justify-center gap-3">
          <button
            class="inline-flex h-11 items-center gap-2 rounded-md bg-gradient-brand bg-[length:200%_200%] px-6 text-sm font-medium text-white shadow-glow transition-[background-position] duration-700 hover:bg-[position:100%_50%]"
          >
            Start a quest
            <ArrowRight class="h-4 w-4" />
          </button>
          <button
            class="inline-flex h-11 items-center gap-2 rounded-md border border-border bg-surface/60 px-6 text-sm text-foreground transition hover:bg-surface-1"
          >
            <Github class="h-4 w-4" />
            View on GitHub
          </button>
        </div>
      </div>

      <!-- Feature cards -->
      <div
        id="features"
        class="relative mx-auto mt-24 grid max-w-5xl gap-4 md:grid-cols-3"
      >
        <article class="card-surface p-6">
          <Dice5 class="h-6 w-6 text-accent" />
          <h3 class="mt-4 text-lg font-medium">Board-game core</h3>
          <p class="mt-2 text-sm text-muted">
            A 30-tile map. Roll, move, trigger. Earn XP and level up from Beginner
            to Master.
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

      <!-- Status strip -->
      <div
        class="mx-auto mt-16 flex max-w-3xl items-center justify-between rounded-md border border-border bg-surface/60 px-4 py-3 text-xs text-muted backdrop-blur"
      >
        <span class="inline-flex items-center gap-2">
          <BookOpen class="h-4 w-4 text-accent" />
          v0.1.0 · D1 scaffolding complete
        </span>
        <span class="font-mono">build: <span class="text-foreground">dev</span></span>
      </div>
    </section>
  </main>
</template>
