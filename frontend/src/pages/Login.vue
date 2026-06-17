<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { LogIn, AlertCircle, Loader2 } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref<string | null>(null)

async function submit() {
  error.value = null
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    const redirect = (route.query.redirect as string) || '/dashboard'
    await router.replace(redirect)
  } catch (e: any) {
    error.value = e?.response?.data?.message || e?.message || '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <section class="container py-20">
      <div class="mx-auto grid w-full max-w-4xl items-stretch gap-[2px] overflow-hidden rounded-md border border-rule bg-rule lg:grid-cols-2">
        <!-- Editorial panel -->
        <div class="hidden flex-col justify-between bg-paper p-8 lg:flex">
          <div>
            <div class="label-slash">登录</div>
            <h2 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              欢迎回来,<span class="text-ink-faint">学者。</span>
            </h2>
            <p class="mt-4 max-w-[28ch] text-[15px] leading-[1.7] text-ink-soft">
              你的玩家令牌还停在上次离开的那格上。拿起骰子继续。
            </p>
          </div>

          <div class="mt-10 grid grid-cols-6 gap-[3px]" aria-hidden="true">
            <span class="aspect-square bg-pixel-green" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-blue" />
            <span class="aspect-square bg-pixel-amber" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-violet" />
            <span class="aspect-square bg-pixel-pink" />
            <span class="aspect-square bg-pixel-mint" />
            <span class="aspect-square bg-paper-3" />
            <span class="aspect-square bg-pixel-orange" />
            <span class="aspect-square bg-pixel-red" />
            <span class="aspect-square bg-pixel-green" />
          </div>
        </div>

        <!-- Form -->
        <div class="bg-paper p-8">
          <h1 class="font-display text-[32px] font-black leading-tight tracking-tight">登录</h1>
          <p class="mt-2 text-[14px] text-ink-soft">
            用注册时的用户名和密码进入。
          </p>

          <form class="mt-7 space-y-4" @submit.prevent="submit">
            <div class="space-y-2">
              <label class="label-slash" for="username">用户名</label>
              <Input
                id="username"
                v-model="form.username"
                name="username"
                autocomplete="username"
                placeholder="hermione"
              />
            </div>
            <div class="space-y-2">
              <label class="label-slash" for="password">密码</label>
              <Input
                id="password"
                v-model="form.password"
                type="password"
                name="password"
                autocomplete="current-password"
                placeholder="••••••••"
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
              <LogIn v-else class="h-4 w-4" />
              {{ loading ? '登录中…' : '登录' }}
            </Button>
          </form>

          <p class="mt-6 text-[13.5px] text-ink-soft">
            还没有账号?
            <RouterLink to="/register" class="text-ink underline decoration-rule-soft underline-offset-[6px] transition-colors hover:decoration-ink">
              马上创建一个
            </RouterLink>
          </p>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
