<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { UserPlus, AlertCircle, Loader2 } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()

const form = reactive({ username: '', email: '', password: '' })
const loading = ref(false)
const error = ref<string | null>(null)

async function submit() {
  error.value = null
  loading.value = true
  try {
    await auth.register({ ...form })
    await router.replace('/dashboard')
  } catch (e: any) {
    error.value = e?.response?.data?.message || e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <section class="container py-20">
      <div class="mx-auto grid w-full max-w-4xl items-stretch gap-[2px] overflow-hidden rounded-md border border-rule bg-rule lg:grid-cols-2">
        <div class="hidden flex-col justify-between bg-paper p-8 lg:flex">
          <div>
            <div class="label-slash">创建账号</div>
            <h2 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              开始你的<span class="text-ink-faint">旅程。</span>
            </h2>
            <p class="mt-4 max-w-[28ch] text-[15px] leading-[1.7] text-ink-soft">
              永久免费,无需信用卡。填三栏,就能掷骰子。
            </p>
          </div>
          <ol class="mt-10 space-y-3 text-[12.5px] text-ink-soft">
            <li class="flex items-center gap-3">
              <span class="inline-block h-3 w-3 bg-pixel-green" />
              <span class="font-mono text-ink">01</span>
              <span class="h-px flex-1 bg-rule-soft" />
              <span>填写三个字段</span>
            </li>
            <li class="flex items-center gap-3">
              <span class="inline-block h-3 w-3 bg-pixel-blue" />
              <span class="font-mono text-ink">02</span>
              <span class="h-px flex-1 bg-rule-soft" />
              <span>掷出第一颗骰子</span>
            </li>
            <li class="flex items-center gap-3">
              <span class="inline-block h-3 w-3 bg-pixel-amber" />
              <span class="font-mono text-ink">03</span>
              <span class="h-px flex-1 bg-rule-soft" />
              <span>登上周排行榜</span>
            </li>
          </ol>
        </div>

        <div class="bg-paper p-8">
          <h1 class="font-display text-[32px] font-black leading-tight tracking-tight">创建账号</h1>
          <p class="mt-2 text-[14px] text-ink-soft">
            永久免费,无需信用卡。
          </p>

          <form class="mt-7 space-y-4" @submit.prevent="submit">
            <div class="space-y-2">
              <label class="label-slash" for="r-username">用户名</label>
              <Input
                id="r-username"
                v-model="form.username"
                autocomplete="username"
                placeholder="3–32 字符"
              />
            </div>
            <div class="space-y-2">
              <label class="label-slash" for="r-email">邮箱</label>
              <Input
                id="r-email"
                v-model="form.email"
                type="email"
                autocomplete="email"
                placeholder="you@example.com"
              />
            </div>
            <div class="space-y-2">
              <label class="label-slash" for="r-password">密码</label>
              <Input
                id="r-password"
                v-model="form.password"
                type="password"
                autocomplete="new-password"
                placeholder="至少 6 位"
              />
            </div>

            <div
              v-if="error"
              class="flex items-start gap-2 rounded-md border-2 border-danger bg-pixel-red/15 p-3 text-[13px] text-ink"
            >
              <AlertCircle class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
              <span>{{ error }}</span>
            </div>

            <Button type="submit" size="lg" class="w-full" :disabled="loading">
              <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
              <UserPlus v-else class="h-4 w-4" />
              {{ loading ? '创建中…' : '创建账号' }}
            </Button>
          </form>

          <p class="mt-6 text-[13.5px] text-ink-soft">
            已经有账号了?
            <RouterLink to="/login" class="text-ink underline decoration-rule-soft underline-offset-[6px] transition-colors hover:decoration-ink">
              直接登录
            </RouterLink>
          </p>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
