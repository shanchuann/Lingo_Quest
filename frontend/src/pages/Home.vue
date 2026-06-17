<script setup lang="ts">
import { ArrowUpRight, MoveRight, Github } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'

const pillars = [
  {
    k: '棋盘',
    title: '是棋盘,不是题海。',
    body: '30 格地图按蛇形排列。掷骰、前进、触发 —— 每一格都是一个小课时:单词、语法、口语、陷阱、幸运。',
    swatch: 'bg-pixel-green',
  },
  {
    k: '导师',
    title: '每一步都有 AI 老师。',
    body: 'DeepSeek 读题、看你答案、用中文给反馈。Redis 缓存、限速保护、3 秒预算、超时也有友好兜底。',
    swatch: 'bg-pixel-blue',
  },
  {
    k: '排行',
    title: '看得见的进步。',
    body: '基于 Redis ZSET 的周榜、Lv1 → Lv5 的完整经验曲线,以及周末替你陈情的 AI 学习报告。',
    swatch: 'bg-pixel-amber',
  },
]

const agenda: Array<[string, string, string]> = [
  ['01:24', '注册账号 · 投出第一颗骰子', '入局'],
  ['02:10', '踩到单词格 · 与 AI 导师初次见面', '进行中'],
  ['03:05', '撞上陷阱 · 失 5 xp · 学到那个词', '进行中'],
  ['04:00', '走完一圈 · 周榜爬升一位', '排行'],
  ['05:00', '看 AI 周报 · 选定下周的发力点', '周报'],
]
</script>

<template>
  <AppLayout>
    <!-- ─────────────────── HERO ─────────────────── -->
    <section class="border-b border-rule">
      <div class="container grid gap-x-10 gap-y-12 py-16 lg:grid-cols-12 lg:py-24">
        <!-- Headline -->
        <div class="lg:col-span-9">
          <div class="label-slash">
            <span class="inline-flex h-2.5 w-2.5 bg-pixel-green animate-pixel-blink" />
            <span class="ml-2">AI 驱动 · 中文界面 · 8 本词书已上线</span>
          </div>

          <h1 class="mt-6 max-w-[18ch] font-display text-display-xl font-black leading-[0.92] tracking-tight text-ink">
            把英语
            <span class="relative inline-block">
              <span class="relative z-10 text-ink">当成棋盘游戏来玩。</span>
              <span aria-hidden="true" class="absolute -bottom-2 left-0 -z-0 inline-block h-3 w-full bg-pixel-green/60" />
            </span>
          </h1>

          <p class="mt-8 max-w-[40ch] text-[17px] leading-[1.7] text-ink-soft text-pretty">
            掷骰子 · 落在一格未知的题面上 · 让一位小型 AI 导师替你讲解。
            它先是一款棋盘游戏,然后才是一款学习应用 —— 因为没有人围着篝火讲过测验的故事。
          </p>

          <div class="mt-9 flex flex-wrap items-center gap-3">
            <RouterLink
              to="/register"
              class="inline-flex h-12 items-center gap-2 rounded-md bg-ink px-6 text-[15px] font-medium text-paper transition-colors duration-300 hover:bg-ink-soft focus-ring"
            >
              开始我的旅程<span class="font-mono text-paper/60">/</span>
              <MoveRight class="h-4 w-4" />
            </RouterLink>
            <RouterLink
              to="/tutor"
              class="inline-flex h-12 items-center gap-2 rounded-md border border-rule bg-paper px-6 text-[15px] font-medium text-ink transition-colors duration-300 hover:bg-paper-3 focus-ring"
            >
              先试试 AI 导师<span class="font-mono text-ink-faint">/</span>
              <ArrowUpRight class="h-4 w-4" />
            </RouterLink>
            <a
              href="https://github.com/shanchuann/Lingo_Quest"
              target="_blank"
              class="ml-1 inline-flex items-center gap-1.5 text-[13.5px] text-ink-soft underline decoration-rule-soft underline-offset-[6px] transition-colors hover:text-ink hover:decoration-ink focus-ring rounded-sm"
            >
              <Github class="h-3.5 w-3.5" />
              在 GitHub 上看源码
            </a>
          </div>
        </div>

        <!-- Right pixel mosaic + sample tile -->
        <aside class="lg:col-span-3">
          <!-- Pixel mosaic -->
          <div class="grid grid-cols-6 gap-[3px]" aria-hidden="true">
            <span class="aspect-square bg-pixel-green" />
            <span class="aspect-square bg-pixel-mint" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-amber" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-pink" />

            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-blue" />
            <span class="aspect-square bg-pixel-blue" />
            <span class="aspect-square bg-pixel-amber" />
            <span class="aspect-square bg-pixel-mint" />
            <span class="aspect-square bg-paper-3" />

            <span class="aspect-square bg-pixel-violet" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-green" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-red" />
            <span class="aspect-square bg-pixel-amber" />

            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-pink" />
            <span class="aspect-square bg-pixel-violet" />
            <span class="aspect-square bg-pixel-orange" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-green" />
          </div>

          <!-- Sample tile card -->
          <div class="paper-card mt-5 p-4">
            <div class="flex items-center justify-between font-mono text-[10.5px] uppercase tracking-[0.18em] text-ink-faint">
              <span>tile · 18 / 30</span>
              <span class="inline-flex items-center gap-1.5 text-ink"><span class="h-2 w-2 bg-pixel-violet" /> 语法</span>
            </div>
            <div class="mt-3 font-display text-[22px] font-black leading-tight tracking-tight">
              文字匠人的桥
            </div>
            <p class="mt-2 text-[13.5px] leading-relaxed text-ink-soft">
              "She _____ to school every morning before the bell."
            </p>
            <div class="mt-4 space-y-1.5 text-[13.5px]">
              <div class="flex items-center gap-2 rounded-sm border border-rule-soft bg-paper px-2.5 py-2">
                <span class="font-mono text-[11px] text-ink-faint">A</span>
                <span class="text-ink">go</span>
              </div>
              <div class="flex items-center gap-2 rounded-sm border-2 border-ink bg-pixel-green/40 px-2.5 py-2">
                <span class="font-mono text-[11px] text-ink">B</span>
                <span class="font-medium text-ink">goes</span>
              </div>
              <div class="flex items-center gap-2 rounded-sm border border-rule-soft bg-paper px-2.5 py-2">
                <span class="font-mono text-[11px] text-ink-faint">C</span>
                <span class="text-ink">going</span>
              </div>
            </div>
          </div>
        </aside>
      </div>
    </section>

    <!-- ─────────────────── FACT STRIP ─────────────────── -->
    <section class="border-b border-rule">
      <div class="container grid grid-cols-2 divide-rule-soft md:grid-cols-4 md:divide-x">
        <div class="px-2 py-8 md:px-7">
          <div class="font-display text-[40px] font-black leading-none tracking-tight md:text-[52px]">30</div>
          <div class="mt-2 text-[12.5px] text-ink-soft">棋盘格数</div>
        </div>
        <div class="px-2 py-8 md:px-7">
          <div class="font-display text-[40px] font-black leading-none tracking-tight md:text-[52px]">
            8<span class="ml-2 inline-flex h-7 align-middle">
              <span class="h-7 w-2 bg-pixel-green" />
              <span class="h-7 w-2 bg-pixel-blue" />
              <span class="h-7 w-2 bg-pixel-violet" />
              <span class="h-7 w-2 bg-pixel-pink" />
              <span class="h-7 w-2 bg-pixel-amber" />
              <span class="h-7 w-2 bg-pixel-mint" />
              <span class="h-7 w-2 bg-pixel-orange" />
              <span class="h-7 w-2 bg-pixel-red" />
            </span>
          </div>
          <div class="mt-2 text-[12.5px] text-ink-soft">格子类型</div>
        </div>
        <div class="px-2 py-8 md:px-7">
          <div class="font-display text-[40px] font-black leading-none tracking-tight md:text-[52px]">Lv1→5</div>
          <div class="mt-2 text-[12.5px] text-ink-soft">经验等级曲线</div>
        </div>
        <div class="px-2 py-8 md:px-7">
          <div class="font-display text-[40px] font-black leading-none tracking-tight md:text-[52px]">&lt;3s</div>
          <div class="mt-2 text-[12.5px] text-ink-soft">AI 响应预算</div>
        </div>
      </div>
    </section>

    <!-- ─────────────────── THREE PILLARS ─────────────────── -->
    <section class="border-b border-rule">
      <div class="container py-20">
        <div class="grid gap-y-10 lg:grid-cols-12">
          <div class="lg:col-span-5">
            <div class="label-slash">三个立场</div>
            <h2 class="mt-5 font-display text-display-lg font-black leading-[0.98] tracking-tight text-balance">
              一个小产品,
              <span class="block text-ink-faint">带着三个立场。</span>
            </h2>
          </div>
          <p class="text-[16px] leading-relaxed text-ink-soft lg:col-span-6 lg:col-start-7">
            我们不打算做下一个多邻国。我们做的是
            <em class="not-italic font-medium text-ink">掷 → 触发 → 学</em>
            这个尽可能短的循环 —— 并把这套系统的每一层(AI、排行、持久化)都当成展示架构的舞台。
          </p>
        </div>

        <ol class="mt-14 grid grid-cols-1 gap-[2px] overflow-hidden rounded-md border border-rule bg-rule md:grid-cols-3">
          <li
            v-for="(p, i) in pillars"
            :key="p.k"
            class="group relative flex flex-col gap-5 bg-paper p-6 transition-colors duration-300 hover:bg-paper-2"
          >
            <div class="flex items-start justify-between">
              <span class="font-mono text-[12px] tracking-tight text-ink-soft">
                {{ String(i + 1).padStart(2, '0') }} · {{ p.k }}<span class="text-ink-faint">/</span>
              </span>
              <span :class="['inline-block h-5 w-5 shrink-0', p.swatch]" />
            </div>
            <h3 class="font-display text-[24px] font-black leading-tight tracking-tight">
              {{ p.title }}
            </h3>
            <p class="text-[14.5px] leading-relaxed text-ink-soft">{{ p.body }}</p>
          </li>
        </ol>
      </div>
    </section>

    <!-- ─────────────────── AGENDA ─────────────────── -->
    <section class="border-b border-rule">
      <div class="container py-20">
        <div class="grid gap-y-8 lg:grid-cols-12">
          <div class="lg:col-span-4">
            <div class="label-slash">前五分钟</div>
            <h2 class="mt-5 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              你的前五分钟,
              <span class="text-ink-faint">大概长这样。</span>
            </h2>
          </div>
          <ol class="divide-y divide-rule-soft border-y border-rule-soft lg:col-span-7 lg:col-start-6">
            <li
              v-for="[time, what, where] in agenda"
              :key="time"
              class="grid grid-cols-[80px_1fr_auto] items-baseline gap-6 py-4"
            >
              <span class="font-mono text-[13px] text-ink-faint">{{ time }}</span>
              <span class="text-[15px] text-ink">{{ what }}</span>
              <span class="font-mono text-[11px] uppercase tracking-[0.16em] text-ink-soft">[ {{ where }} ]</span>
            </li>
          </ol>
        </div>
      </div>
    </section>

    <!-- ─────────────────── CTA STRIP ─────────────────── -->
    <section>
      <div class="container py-20">
        <div class="grid items-center gap-8 md:grid-cols-[1fr_auto]">
          <div>
            <div class="label-slash">随时开始</div>
            <p class="mt-5 max-w-[20ch] font-display text-display-lg font-black leading-[0.98] tracking-tight">
              棋盘已经备好。
              <span class="block text-pixel-green/90">该你掷骰子了。</span>
            </p>
          </div>
          <div class="flex flex-wrap items-center gap-3">
            <RouterLink
              to="/register"
              class="inline-flex h-12 items-center gap-2 rounded-md bg-ink px-7 text-[15px] font-medium text-paper transition-colors duration-300 hover:bg-ink-soft focus-ring"
            >
              创建账号<span class="font-mono text-paper/60">/</span>
              <MoveRight class="h-4 w-4" />
            </RouterLink>
            <RouterLink
              to="/login"
              class="inline-flex h-12 items-center gap-2 rounded-md border border-rule bg-paper px-6 text-[15px] font-medium text-ink transition-colors hover:bg-paper-3 focus-ring"
            >
              我已有账号<span class="font-mono text-ink-faint">/</span>
            </RouterLink>
          </div>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
