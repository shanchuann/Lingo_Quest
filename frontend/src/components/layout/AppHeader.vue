<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { Menu, X } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const initial = computed(() => auth.user?.username?.[0]?.toUpperCase() ?? '?')

const menuOpen = ref(false)

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

// Close the mobile panel whenever the route changes or the user logs out.
watch(() => route.fullPath, () => { menuOpen.value = false })
watch(() => auth.isAuthed, (authed) => { if (!authed) menuOpen.value = false })
</script>

<template>
  <!-- 顶部 ticker -->
  <div class="border-b border-rule bg-ink">
    <div class="container flex h-7 items-center gap-3 font-mono text-[10.5px] uppercase tracking-[0.18em] text-paper">
      <span class="inline-flex h-2.5 w-2.5 shrink-0 bg-pixel-green" />
      <span class="hidden truncate sm:inline">AI 中文讲解 · 8 本主流词书 · 30 格棋盘 · 服务正常</span>
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
    <div class="container flex h-[60px] items-center gap-3 sm:gap-6 lg:h-[68px] lg:gap-8">
      <RouterLink to="/" class="group flex min-w-0 items-center gap-2.5 focus-ring rounded-sm sm:gap-3">
        <span class="grid shrink-0 grid-cols-2 grid-rows-2 gap-[2px]">
          <span class="h-2.5 w-2.5 bg-pixel-green sm:h-3 sm:w-3" />
          <span class="h-2.5 w-2.5 bg-pixel-blue sm:h-3 sm:w-3" />
          <span class="h-2.5 w-2.5 bg-pixel-pink sm:h-3 sm:w-3" />
          <span class="h-2.5 w-2.5 bg-pixel-amber sm:h-3 sm:w-3" />
        </span>
        <span class="truncate font-display text-[17px] font-black uppercase leading-none tracking-tight text-ink min-[380px]:text-[19px] sm:text-[26px]">
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

      <div class="ml-auto flex shrink-0 items-center gap-1.5 sm:gap-3">
        <template v-if="auth.isAuthed">
          <div class="hidden text-right font-mono text-[11px] uppercase tracking-[0.16em] text-ink-faint sm:block">
            Lv {{ auth.user?.level ?? 1 }} · {{ auth.user?.experience ?? 0 }} xp
          </div>
          <button
            type="button"
            class="-mr-1 inline-flex h-10 w-10 items-center justify-center rounded-sm text-ink transition-colors hover:bg-paper-3 focus-ring lg:hidden"
            :aria-expanded="menuOpen"
            aria-controls="mobile-nav"
            :aria-label="menuOpen ? '关闭菜单' : '打开菜单'"
            @click="menuOpen = !menuOpen"
          >
            <X v-if="menuOpen" class="h-5 w-5" />
            <Menu v-else class="h-5 w-5" />
          </button>
          <RouterLink
            to="/profile"
            class="flex h-8 w-8 items-center justify-center rounded-sm bg-ink font-mono text-[12px] font-semibold text-paper transition-colors hover:bg-ink-soft focus-ring"
            :title="(auth.user?.username ?? '') + ' · 进入个人中心'"
          >
            {{ initial }}
          </RouterLink>
          <button
            class="hidden rounded-sm px-2 text-[13.5px] text-ink-soft transition-colors hover:text-ink focus-ring sm:block"
            @click="auth.logout()"
          >
            退出<span class="ml-0.5 font-mono text-ink-faint">/</span>
          </button>
        </template>
        <template v-else>
          <RouterLink
            class="shrink-0 whitespace-nowrap rounded-sm px-1 text-[13px] text-ink-soft transition-colors hover:text-ink focus-ring sm:px-2 sm:text-[13.5px]"
            to="/login"
          >
            登录<span class="ml-0.5 font-mono text-ink-faint">/</span>
          </RouterLink>
          <RouterLink
            class="inline-flex h-9 shrink-0 items-center gap-1.5 whitespace-nowrap rounded-md bg-ink px-2.5 text-[13px] font-medium text-paper transition-colors duration-300 hover:bg-ink-soft focus-ring sm:h-9 sm:gap-2 sm:px-4 sm:text-[13.5px]"
            to="/register"
          >
            开始游戏
            <span class="inline-flex h-2 w-2 bg-pixel-green" />
          </RouterLink>
        </template>
      </div>
    </div>

    <!-- Mobile navigation panel (< lg) -->
    <Transition
      enter-active-class="transition duration-200 ease-out-expo"
      enter-from-class="opacity-0 -translate-y-1"
      leave-active-class="transition duration-150"
      leave-to-class="opacity-0 -translate-y-1"
    >
      <div
        v-if="menuOpen && auth.isAuthed"
        id="mobile-nav"
        class="border-t border-rule bg-paper lg:hidden"
      >
        <nav class="container grid grid-cols-2 gap-1.5 py-3">
          <RouterLink
            v-for="item in navAuthed"
            :key="item.to"
            :to="item.to"
            class="flex h-11 items-center justify-between rounded-sm border border-rule-soft bg-paper px-3 text-[14px] text-ink-soft transition-colors hover:bg-paper-3 focus-ring"
            :class="route.path.startsWith(item.to) ? 'border-ink bg-paper-2 font-medium text-ink' : ''"
          >
            <span>{{ item.label }}</span>
            <span class="font-mono text-[11px] text-ink-faint">/</span>
          </RouterLink>
        </nav>
        <div class="container flex items-center justify-between border-t border-rule-soft py-3">
          <RouterLink to="/profile" class="inline-flex h-10 items-center text-[14px] text-ink-soft focus-ring">
            个人中心<span class="ml-0.5 font-mono text-ink-faint">/</span>
          </RouterLink>
          <button
            class="inline-flex h-10 items-center px-2 text-[14px] text-ink-soft transition-colors hover:text-ink focus-ring"
            @click="auth.logout()"
          >
            退出登录<span class="ml-0.5 font-mono text-ink-faint">/</span>
          </button>
        </div>
      </div>
    </Transition>
  </header>
</template>
