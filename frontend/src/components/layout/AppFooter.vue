<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <footer class="mt-12 border-t border-rule bg-paper md:mt-24">
    <!-- 仿 Universe 底部超大字标 -->
    <div class="container relative overflow-hidden pb-3 pt-8 md:pt-12">
      <h2
        aria-hidden="true"
        class="select-none break-words font-display text-[clamp(1.75rem,10vw,11rem)] font-black uppercase leading-[0.82] tracking-tight text-ink/95 md:text-mark"
        style="font-stretch: 75%;"
      >
        LingoQuest<span class="text-pixel-green">·</span><span class="text-ink-faint">26</span>
      </h2>
      <div class="mt-6 flex flex-col items-start justify-between gap-4 border-t border-rule pt-4 text-[12.5px] text-ink-soft sm:flex-row sm:flex-wrap sm:items-baseline">
        <span class="font-mono text-[10px] uppercase leading-relaxed tracking-[0.14em] sm:text-[11px] sm:tracking-[0.18em]">© 2026 LINGO QUEST · ALL RIGHTS RESERVED</span>
        <nav class="flex flex-wrap items-center gap-x-4 gap-y-2">
          <RouterLink to="/terms" class="transition-colors hover:text-ink">服务条款</RouterLink>
          <span class="text-ink-faint">/</span>
          <RouterLink to="/privacy" class="transition-colors hover:text-ink">隐私政策</RouterLink>
          <span class="text-ink-faint">/</span>
          <RouterLink to="/help" class="transition-colors hover:text-ink">帮助中心</RouterLink>
          <span class="text-ink-faint">/</span>
          <a
            href="https://www.shanchuann.cn/about/#contact"
            target="_blank"
            rel="noopener noreferrer"
            class="inline-flex items-center gap-1 transition-colors hover:text-ink"
          >
            联系我们
            <span class="font-mono text-[10px] opacity-70">↗</span>
          </a>
        </nav>
        <span class="inline-flex items-center gap-2">
          <span class="inline-block h-1.5 w-1.5 animate-pulse rounded-full bg-pixel-green" />
          中文(简体) · 服务正常
        </span>
      </div>
    </div>
  </footer>
</template>
