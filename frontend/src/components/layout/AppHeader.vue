<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const initial = computed(() => auth.user?.username?.[0]?.toUpperCase() ?? '?')

const navAuthed = [
  { to: '/dashboard',    label: '概览' },
  { to: '/game',         label: '棋盘' },
  { to: '/wordbooks',    label: '词书' },
  { to: '/tutor',        label: '导师' },
  { to: '/ranking',      label: '排行' },
  { to: '/achievements', label: '徽章' },
  { to: '/shop',         label: '商城' },
  { to: '/report',       label: '周报' },
]
</script>

<template>
  <!-- 顶部 ticker -->
  <div class="border-b border-rule bg-ink">
    <div class="container flex h-7 items-center gap-3 font-mono text-[10.5px] uppercase tracking-[0.18em] text-paper">
      <span class="inline-flex h-2.5 w-2.5 shrink-0 bg-pixel-green" />
      <span class="hidden sm:inline">AI 中文讲解 · 8 本主流词书 · 30 格棋盘 · 服务正常</span>
      <span class="ml-auto inline-flex items-center gap-1">
        <span class="inline-flex h-2 w-2 bg-pixel-green" />
        <span class="inline-flex h-2 w-2 bg-pixel-blue" />
        <span class="inline-flex h-2 w-2 bg-pixel-pink" />
        <span class="inline-flex h-2 w-2 bg-pixel-amber" />
        <span class="inline-flex h-2 w-2 bg-pixel-violet" />
      </span>
    </div>
  </div>

  <header class="sticky top-0 z-30 border-b border-rule bg-paper/95 backdrop-blur-sm">
    <div class="container flex h-[68px] items-center gap-8">
      <RouterLink to="/" class="group flex items-center gap-3 focus-ring rounded-sm">
        <span class="grid grid-cols-2 grid-rows-2 gap-[2px]">
          <span class="h-3 w-3 bg-pixel-green" />
          <span class="h-3 w-3 bg-pixel-blue" />
          <span class="h-3 w-3 bg-pixel-pink" />
          <span class="h-3 w-3 bg-pixel-amber" />
        </span>
        <span class="font-display text-[26px] font-black uppercase leading-none tracking-tight text-ink">
          Lingo<span class="text-ink-faint">·</span>Quest
        </span>
      </RouterLink>

      <nav v-if="auth.isAuthed" class="hidden flex-1 items-center gap-1 lg:flex">
        <RouterLink
          v-for="item in navAuthed"
          :key="item.to"
          :to="item.to"
          class="group relative inline-flex h-9 items-baseline rounded-sm px-2 text-[14px] tracking-tight text-ink-soft transition-colors duration-200 focus-ring hover:text-ink"
          :class="route.path.startsWith(item.to) ? 'text-ink font-medium' : ''"
        >
          {{ item.label }}<span class="ml-0.5 font-mono text-ink-faint">/</span>
          <span
            v-if="route.path.startsWith(item.to)"
            class="absolute inset-x-2 -bottom-[20px] h-[3px] bg-ink"
            aria-hidden="true"
          />
        </RouterLink>
      </nav>

      <div class="ml-auto flex items-center gap-3">
        <template v-if="auth.isAuthed">
          <div class="hidden text-right font-mono text-[11px] uppercase tracking-[0.16em] text-ink-faint sm:block">
            Lv {{ auth.user?.level ?? 1 }} · {{ auth.user?.experience ?? 0 }} xp
          </div>
          <RouterLink
            to="/profile"
            class="flex h-8 w-8 items-center justify-center rounded-sm bg-ink font-mono text-[12px] font-semibold text-paper transition-colors hover:bg-ink-soft focus-ring"
            :title="(auth.user?.username ?? '') + ' · 进入个人中心'"
          >
            {{ initial }}
          </RouterLink>
          <button
            class="rounded-sm px-2 text-[13.5px] text-ink-soft transition-colors hover:text-ink focus-ring"
            @click="auth.logout()"
          >
            退出<span class="ml-0.5 font-mono text-ink-faint">/</span>
          </button>
        </template>
        <template v-else>
          <RouterLink
            class="rounded-sm px-2 text-[13.5px] text-ink-soft transition-colors hover:text-ink focus-ring"
            to="/login"
          >
            登录<span class="ml-0.5 font-mono text-ink-faint">/</span>
          </RouterLink>
          <RouterLink
            class="inline-flex h-9 items-center gap-2 rounded-md bg-ink px-4 text-[13.5px] font-medium text-paper transition-colors duration-300 hover:bg-ink-soft focus-ring"
            to="/register"
          >
            开始游戏
            <span class="inline-flex h-2 w-2 bg-pixel-green" />
          </RouterLink>
        </template>
      </div>
    </div>
  </header>
</template>
