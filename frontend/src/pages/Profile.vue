<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import {
  User as UserIcon,
  Lock,
  Activity,
  ShieldAlert,
  AlertCircle,
  Check,
  Loader2,
  LogOut,
  Trash2,
  Save,
} from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import { useAuthStore } from '@/stores/auth'
import { authApi, type UserStats } from '@/api/auth'
import { wordbookApi, type WordBookSummary } from '@/api/wordbook'
import { Library } from 'lucide-vue-next'

const auth = useAuthStore()
const router = useRouter()

type Tab = 'info' | 'password' | 'stats' | 'danger'
const tab = ref<Tab>('info')

// — Profile edit
const profile = reactive({ email: '', avatar: '' })
const profileSaving = ref(false)
const profileErr = ref<string | null>(null)
const profileOk = ref(false)

// — Password change
const pw = reactive({ oldPassword: '', newPassword: '', confirm: '' })
const pwSaving = ref(false)
const pwErr = ref<string | null>(null)
const pwOk = ref(false)

// — Stats
const stats = ref<UserStats | null>(null)
const statsLoading = ref(false)
const books = ref<WordBookSummary[]>([])
const selectingBook = ref(false)
const recentGames = ref<Array<any>>([])
const trend = ref<Array<{ day: string; correct: number; total: number }>>([])

// — Avatar UI state (recoverable fallback instead of mutating DOM)
const avatarBroken = ref(false)

// — Danger zone
const confirmText = ref('')
const deleting = ref(false)
const dangerErr = ref<string | null>(null)

const tabs: Array<{ k: Tab; label: string; icon: any }> = [
  { k: 'info',     label: '资料',  icon: UserIcon },
  { k: 'password', label: '密码',  icon: Lock },
  { k: 'stats',    label: '战绩',  icon: Activity },
  { k: 'danger',   label: '账号',  icon: ShieldAlert },
]

const initial = computed(() => auth.user?.username?.[0]?.toUpperCase() ?? '?')

onMounted(async () => {
  await auth.refreshMe()
  profile.email = auth.user?.email ?? ''
  profile.avatar = auth.user?.avatar ?? ''
  loadStats()
  loadBooks()
})

// Reset broken state whenever the user types a new URL.
watch(() => profile.avatar, () => { avatarBroken.value = false })

async function loadBooks() {
  try {
    books.value = await wordbookApi.books()
  } catch {
    // non-fatal
  }
}

const currentBook = computed(() =>
  books.value.find((b) => b.id === auth.user?.currentBookId) ?? null
)

const currentBookProgress = computed(() => {
  const b = currentBook.value
  if (!b || !b.totalWords) return 0
  return Math.round(((b.masteredCount ?? 0) / b.totalWords) * 100)
})

async function chooseBook(e: Event) {
  const val = (e.target as HTMLSelectElement).value
  const id = val ? Number(val) : null
  selectingBook.value = true
  try {
    await wordbookApi.select(id)
    if (auth.user) auth.user.currentBookId = id ?? undefined
  } finally {
    selectingBook.value = false
  }
}

async function loadStats() {
  statsLoading.value = true
  try {
    const [s, games, t] = await Promise.all([
      authApi.stats(),
      authApi.recentGames(10).catch(() => []),
      authApi.trend(14).catch(() => []),
    ])
    stats.value = s
    recentGames.value = games as any
    trend.value = t as any
  } finally {
    statsLoading.value = false
  }
}

// SVG trend chart geometry
const trendChart = computed(() => {
  const W = 600, H = 120, P = 16
  const days = trend.value
  if (days.length === 0) return { W, H, P, path: '', dots: [] as Array<{ x: number; y: number; label: string; acc: number }> }
  const pts = days.map((d) => ({
    day: d.day,
    acc: d.total > 0 ? (d.correct / d.total) * 100 : 0,
  }))
  const xStep = pts.length > 1 ? (W - P * 2) / (pts.length - 1) : 0
  const yFor = (acc: number) => H - P - ((acc / 100) * (H - P * 2))
  const path = pts
    .map((p, i) => `${i === 0 ? 'M' : 'L'} ${(P + i * xStep).toFixed(1)} ${yFor(p.acc).toFixed(1)}`)
    .join(' ')
  const dots = pts.map((p, i) => ({
    x: P + i * xStep,
    y: yFor(p.acc),
    label: p.day.slice(5),
    acc: Math.round(p.acc),
  }))
  return { W, H, P, path, dots }
})

const statusLabel: Record<string, string> = {
  IN_PROGRESS: '进行中',
  FINISHED: '已完成',
  ABANDONED: '已放弃',
  PAUSED: '暂停',
  CREATED: '已创建',
}
const statusClass: Record<string, string> = {
  IN_PROGRESS: 'bg-pixel-blue/30 text-ink',
  FINISHED: 'bg-pixel-green/30 text-ink',
  ABANDONED: 'bg-paper-3 text-ink-soft',
  PAUSED: 'bg-pixel-amber/30 text-ink',
  CREATED: 'bg-paper-3 text-ink',
}

async function saveProfile() {
  profileErr.value = null
  profileOk.value = false
  profileSaving.value = true
  try {
    const updated = await authApi.updateProfile({
      email: profile.email,
      avatar: profile.avatar,
    })
    auth.user = updated
    profileOk.value = true
    window.setTimeout(() => (profileOk.value = false), 2500)
  } catch (e: any) {
    profileErr.value = e?.response?.data?.message || e?.message || '保存失败'
  } finally {
    profileSaving.value = false
  }
}

async function savePassword() {
  pwErr.value = null
  pwOk.value = false
  if (pw.newPassword !== pw.confirm) {
    pwErr.value = '两次输入的新密码不一致'
    return
  }
  if (pw.newPassword.length < 6) {
    pwErr.value = '新密码至少 6 位'
    return
  }
  pwSaving.value = true
  try {
    await authApi.changePassword({
      oldPassword: pw.oldPassword,
      newPassword: pw.newPassword,
    })
    pw.oldPassword = pw.newPassword = pw.confirm = ''
    pwOk.value = true
    window.setTimeout(() => (pwOk.value = false), 2500)
  } catch (e: any) {
    pwErr.value = e?.response?.data?.message || e?.message || '修改失败'
  } finally {
    pwSaving.value = false
  }
}

async function deleteAccount() {
  dangerErr.value = null
  if (confirmText.value !== auth.user?.username) {
    dangerErr.value = '请输入完整的用户名以确认'
    return
  }
  deleting.value = true
  try {
    await authApi.deleteAccount()
    auth.logout()
    await router.replace('/')
  } catch (e: any) {
    dangerErr.value = e?.response?.data?.message || e?.message || '注销失败'
    deleting.value = false
  }
}

function signOut() {
  auth.logout()
  router.replace('/login')
}
</script>

<template>
  <AppLayout>
    <section class="container py-8 md:py-12">
      <div class="mx-auto max-w-4xl">
        <!-- Identity card -->
        <header class="flex flex-wrap items-end justify-between gap-6">
          <div class="flex min-w-0 items-center gap-4 sm:gap-5">
            <div class="flex h-16 w-16 shrink-0 items-center justify-center rounded-md border-2 border-ink bg-pixel-green font-display text-[28px] font-black text-ink">
              {{ initial }}
            </div>
            <div class="min-w-0">
              <div class="label-slash">个人中心</div>
              <h1 class="mt-2 break-words font-display text-display-lg font-black leading-[0.98] tracking-tight [overflow-wrap:anywhere]">
                {{ auth.user?.username }}
              </h1>
              <p class="mt-1 font-mono text-[12px] text-ink-soft">
                Lv {{ auth.user?.level ?? 1 }} · {{ auth.user?.experience ?? 0 }} xp · {{ auth.user?.coins ?? 0 }} 金币
              </p>
            </div>
          </div>
          <Button variant="bone" size="sm" class="min-h-[40px] sm:min-h-0" @click="signOut">
            <LogOut class="h-3.5 w-3.5" />
            退出登录
          </Button>
        </header>

        <!-- Tabs -->
        <nav class="mt-8 flex w-full flex-wrap rounded-md border border-rule bg-paper p-1 sm:mt-10 sm:inline-flex sm:w-auto sm:flex-nowrap">
          <button
            v-for="t in tabs"
            :key="t.k"
            class="inline-flex h-10 flex-1 items-center justify-center gap-2 rounded-sm px-2 text-[13.5px] transition-colors duration-200 focus-ring sm:flex-none sm:px-4"
            :class="tab === t.k ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
            @click="tab = t.k"
          >
            <component :is="t.icon" class="h-3.5 w-3.5" />
            {{ t.label }}
          </button>
        </nav>

        <!-- ───────── INFO ───────── -->
        <div v-if="tab === 'info'" class="mt-6 rounded-md border border-rule bg-paper p-5 md:p-6">
          <div class="grid gap-6 md:grid-cols-[180px_1fr]">
            <div>
              <div class="label-slash">头像预览</div>
              <div class="mt-3 flex aspect-square w-full max-w-[160px] items-center justify-center overflow-hidden rounded-md border border-rule bg-paper-2 md:max-w-none">
                <img
                  v-if="profile.avatar && !avatarBroken"
                  :src="profile.avatar"
                  alt="avatar"
                  class="h-full w-full object-cover"
                  @error="avatarBroken = true"
                  @load="avatarBroken = false"
                />
                <span v-else class="font-display text-[64px] font-black text-ink">{{ initial }}</span>
              </div>
              <p v-if="avatarBroken" class="mt-2 text-[11.5px] text-danger">
                图片加载失败,换个 URL 试试。
              </p>
            </div>

            <form class="space-y-4" @submit.prevent="saveProfile">
              <div class="space-y-2">
                <label class="label-slash">用户名</label>
                <Input :model-value="auth.user?.username ?? ''" disabled />
                <p class="text-[12px] text-ink-soft">用户名注册后不可修改。</p>
              </div>
              <div class="space-y-2">
                <label class="label-slash">邮箱</label>
                <Input v-model="profile.email" type="email" placeholder="you@example.com" />
              </div>
              <div class="space-y-2">
                <label class="label-slash">头像链接</label>
                <Input v-model="profile.avatar" placeholder="https://…" />
                <p class="text-[12px] text-ink-soft">填入一个可访问的图片 URL,留空则使用首字母。</p>
              </div>

              <div v-if="profileErr" class="flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink">
                <AlertCircle class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
                <span>{{ profileErr }}</span>
              </div>
              <div v-if="profileOk" class="flex items-start gap-2 rounded-md border-2 border-pixel-green bg-pixel-green/20 p-3 text-[13px] text-ink">
                <Check class="mt-0.5 h-4 w-4 shrink-0 text-success" />
                已保存。
              </div>

              <div class="flex justify-end">
                <Button :disabled="profileSaving" type="submit">
                  <Loader2 v-if="profileSaving" class="h-4 w-4 animate-spin" />
                  <Save v-else class="h-4 w-4" />
                  {{ profileSaving ? '保存中…' : '保存修改' }}
                </Button>
              </div>
            </form>
          </div>
        </div>

        <!-- ───────── PASSWORD ───────── -->
        <div v-else-if="tab === 'password'" class="mt-6 rounded-md border border-rule bg-paper p-5 md:p-6">
          <form class="max-w-md space-y-4" @submit.prevent="savePassword">
            <div class="space-y-2">
              <label class="label-slash">原密码</label>
              <Input v-model="pw.oldPassword" type="password" autocomplete="current-password" placeholder="••••••••" />
            </div>
            <div class="space-y-2">
              <label class="label-slash">新密码</label>
              <Input v-model="pw.newPassword" type="password" autocomplete="new-password" placeholder="至少 6 位" />
            </div>
            <div class="space-y-2">
              <label class="label-slash">再次输入新密码</label>
              <Input v-model="pw.confirm" type="password" autocomplete="new-password" placeholder="•••••••" />
            </div>

            <div v-if="pwErr" class="flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink">
              <AlertCircle class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
              <span>{{ pwErr }}</span>
            </div>
            <div v-if="pwOk" class="flex items-start gap-2 rounded-md border-2 border-pixel-green bg-pixel-green/20 p-3 text-[13px] text-ink">
              <Check class="mt-0.5 h-4 w-4 shrink-0 text-success" />
              密码已更新。
            </div>

            <div class="flex justify-end">
              <Button :disabled="pwSaving" type="submit">
                <Loader2 v-if="pwSaving" class="h-4 w-4 animate-spin" />
                <Lock v-else class="h-4 w-4" />
                {{ pwSaving ? '提交中…' : '修改密码' }}
              </Button>
            </div>
          </form>
        </div>

        <!-- ───────── STATS ───────── -->
        <div v-else-if="tab === 'stats'" class="mt-6 space-y-4">
          <div v-if="statsLoading" class="rounded-md border border-rule bg-paper p-10 text-center text-[13.5px] text-ink-soft">
            <Loader2 class="mx-auto mb-2 h-5 w-5 animate-spin" />
            读取战绩中…
          </div>
          <template v-else-if="stats">
            <div class="grid grid-cols-2 gap-[2px] overflow-hidden rounded-md border border-rule bg-rule md:grid-cols-4">
              <div class="bg-paper p-5">
                <div class="text-[12px] text-ink-soft">参与对局</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight">{{ stats.totalGames }}</div>
              </div>
              <div class="bg-paper p-5">
                <div class="text-[12px] text-ink-soft">完成对局</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight">{{ stats.finishedGames }}</div>
              </div>
              <div class="bg-paper p-5">
                <div class="text-[12px] text-ink-soft">答题次数</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight">{{ stats.totalAnswers }}</div>
              </div>
              <div class="bg-paper p-5">
                <div class="text-[12px] text-success">答对</div>
                <div class="mt-2 font-display text-[32px] font-black leading-none tracking-tight text-success">{{ stats.correctAnswers }}</div>
              </div>
            </div>

            <div class="rounded-md border border-rule bg-paper p-5 md:p-6">
              <div class="label-slash">综合正确率</div>
              <div class="mt-4 flex flex-wrap items-baseline gap-x-3 gap-y-1">
                <div class="font-display text-[40px] font-black leading-none tracking-tight md:text-[56px]">{{ stats.accuracy.toFixed(1) }}%</div>
                <span class="font-mono text-[12px] text-ink-soft">基于全部历史答题</span>
              </div>
              <div class="mt-5 h-2 w-full overflow-hidden border border-rule bg-paper-2">
                <div class="h-full bg-ink transition-[width] duration-700 ease-out-expo" :style="{ width: Math.min(100, stats.accuracy) + '%' }" />
              </div>
            </div>

            <!-- Current word book block -->
            <div class="rounded-md border border-rule bg-paper p-5 md:p-6">
              <div class="flex flex-wrap items-center justify-between gap-3">
                <div class="label-slash">
                  <Library class="mr-1.5 h-3.5 w-3.5" />
                  当前词书
                </div>
                <select
                  class="h-11 w-full max-w-full rounded-sm border border-rule bg-paper px-3 text-[13px] text-ink focus-ring sm:h-9 sm:w-auto"
                  :value="auth.user?.currentBookId ?? ''"
                  :disabled="selectingBook"
                  @change="chooseBook"
                >
                  <option value="">— 未选 —</option>
                  <option v-for="b in books" :key="b.id" :value="b.id">{{ b.name }}</option>
                </select>
              </div>
              <template v-if="currentBook">
                <div class="mt-4 font-display text-[22px] font-black leading-tight tracking-tight">
                  {{ currentBook.name }}
                </div>
                <p class="mt-2 text-[13.5px] text-ink-soft">{{ currentBook.description }}</p>
                <div class="mt-4 flex items-baseline justify-between font-mono text-[11.5px] text-ink-soft">
                  <span>掌握 {{ currentBook.masteredCount ?? 0 }} / {{ currentBook.totalWords }}</span>
                  <span>{{ currentBookProgress }}%</span>
                </div>
                <div class="mt-2 h-2 w-full overflow-hidden border border-rule bg-paper-2">
                  <div class="h-full bg-ink transition-[width] duration-700 ease-out-expo" :style="{ width: currentBookProgress + '%' }" />
                </div>
              </template>
              <p v-else class="mt-3 text-[13.5px] text-ink-soft">
                还没有选定词书。先到 <RouterLink to="/wordbooks" class="text-ink underline decoration-rule-soft underline-offset-[4px] hover:decoration-ink">词书馆</RouterLink> 挑一本吧。
              </p>
            </div>

            <!-- Accuracy trend chart -->
            <div class="rounded-md border border-rule bg-paper p-5 md:p-6">
              <div class="label-slash">最近 14 天正确率</div>
              <div v-if="trend.length === 0" class="mt-4 text-[13.5px] text-ink-soft">
                还没有足够数据 —— 多答几道题就有趋势图了。
              </div>
              <div v-else class="mt-4 w-full overflow-x-auto">
                <svg :viewBox="`0 0 ${trendChart.W} ${trendChart.H}`" class="h-32 w-full min-w-[440px] sm:min-w-0">
                  <!-- baseline 100% & 50% -->
                  <line :x1="trendChart.P" :y1="trendChart.P" :x2="trendChart.W - trendChart.P" :y2="trendChart.P"
                        stroke="oklch(0.78 0.012 90)" stroke-dasharray="2 4" />
                  <line :x1="trendChart.P" :y1="(trendChart.H + trendChart.P*0)/2" :x2="trendChart.W - trendChart.P"
                        :y2="(trendChart.H + trendChart.P*0)/2" stroke="oklch(0.78 0.012 90)" stroke-dasharray="2 4" />
                  <path :d="trendChart.path" fill="none" stroke="oklch(0.18 0.010 80)" stroke-width="2" />
                  <g v-for="(p, i) in trendChart.dots" :key="i">
                    <circle :cx="p.x" :cy="p.y" r="3" fill="oklch(0.72 0.18 145)" stroke="oklch(0.18 0.010 80)" />
                    <text :x="p.x" :y="p.y - 8" font-size="9" text-anchor="middle" fill="oklch(0.36 0.010 80)">
                      {{ p.acc }}%
                    </text>
                    <text :x="p.x" :y="trendChart.H - 2" font-size="9" text-anchor="middle" fill="oklch(0.55 0.010 80)">
                      {{ p.label }}
                    </text>
                  </g>
                </svg>
              </div>
            </div>

            <!-- Recent games list -->
            <div class="rounded-md border border-rule bg-paper p-5 md:p-6">
              <div class="label-slash">最近对局</div>
              <div v-if="recentGames.length === 0" class="mt-4 text-[13.5px] text-ink-soft">
                还没开始过对局 —— 去棋盘掷第一颗骰子吧。
              </div>
              <ul v-else class="mt-4 divide-y divide-rule-soft">
                <li v-for="g in recentGames" :key="g.id" class="flex flex-wrap items-center gap-x-3 gap-y-1 py-2.5 sm:grid sm:grid-cols-[60px_1fr_auto_auto] sm:gap-3">
                  <span class="font-mono text-[12px] text-ink-soft">#{{ g.id }}</span>
                  <span class="font-mono text-[12.5px] text-ink-soft">{{ String(g.startTime).slice(0, 16).replace('T', ' ') }}</span>
                  <span class="font-mono text-[12.5px] text-ink">回合 {{ g.roundNum }} · 分 {{ g.totalScore }}</span>
                  <span :class="['rounded-sm px-2 py-0.5 text-[11px]', statusClass[g.status] ?? 'bg-paper-3']">{{ statusLabel[g.status] ?? g.status }}</span>
                </li>
              </ul>
            </div>

            <div class="rounded-md border border-rule bg-paper p-5 md:p-6">
              <div class="label-slash">资源储备</div>
              <dl class="mt-4 grid grid-cols-3 gap-4 text-[14px]">
                <div>
                  <dt class="text-[12px] text-ink-soft">等级</dt>
                  <dd class="mt-1 font-mono text-[18px] text-ink">Lv {{ stats.level }}</dd>
                </div>
                <div>
                  <dt class="text-[12px] text-ink-soft">经验</dt>
                  <dd class="mt-1 font-mono text-[18px] text-ink">{{ stats.experience }}</dd>
                </div>
                <div>
                  <dt class="text-[12px] text-ink-soft">金币</dt>
                  <dd class="mt-1 font-mono text-[18px] text-ink">{{ stats.coins }}</dd>
                </div>
              </dl>
            </div>
          </template>
        </div>

        <!-- ───────── DANGER ───────── -->
        <div v-else-if="tab === 'danger'" class="mt-6 rounded-md border-2 border-danger bg-paper p-5 md:p-6">
          <div class="flex items-start gap-3">
            <span class="flex h-10 w-10 shrink-0 items-center justify-center rounded-sm bg-pixel-red text-paper">
              <ShieldAlert class="h-5 w-5" />
            </span>
            <div class="flex-1">
              <h3 class="font-display text-[20px] font-black leading-tight tracking-tight text-ink">
                注销账号
              </h3>
              <p class="mt-2 text-[14px] leading-relaxed text-ink-soft">
                注销后,你的游戏对局、徽章、库存会保留但无法访问,登录通道关闭。
                <span class="text-ink">为了确认,请在下方输入你的用户名 <span class="font-mono">{{ auth.user?.username }}</span>。</span>
              </p>

              <div class="mt-5 max-w-sm space-y-3">
                <Input v-model="confirmText" :placeholder="auth.user?.username || '用户名'" />
                <div v-if="dangerErr" class="flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink">
                  <AlertCircle class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
                  <span>{{ dangerErr }}</span>
                </div>
                <button
                  class="inline-flex h-11 items-center gap-2 rounded-md bg-pixel-red px-5 text-[14px] font-medium text-paper transition-colors hover:opacity-90 focus-ring disabled:opacity-50"
                  :disabled="deleting || !confirmText"
                  @click="deleteAccount"
                >
                  <Loader2 v-if="deleting" class="h-4 w-4 animate-spin" />
                  <Trash2 v-else class="h-4 w-4" />
                  {{ deleting ? '处理中…' : '永久注销账号' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
