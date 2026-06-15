<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute, RouterLink } from 'vue-router'
import { LogIn, Sparkles, AlertCircle, Loader2 } from 'lucide-vue-next'
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
    error.value = e?.response?.data?.message || e?.message || 'Sign in failed'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <section class="container flex flex-1 items-center justify-center py-20">
      <div class="w-full max-w-md">
        <div class="card-surface relative overflow-hidden p-8">
          <div class="pointer-events-none absolute -top-32 left-1/2 h-56 w-72 -translate-x-1/2 bg-gradient-radial blur-2xl" />

          <div class="relative">
            <div class="mb-6 flex items-center gap-2 text-xs text-muted">
              <Sparkles class="h-3.5 w-3.5 text-accent" />
              Welcome back, scholar
            </div>
            <h1 class="text-2xl font-semibold tracking-tight">Sign in to Lingo Quest</h1>
            <p class="mt-2 text-sm text-muted">
              Resume your quest — the next tile is waiting.
            </p>

            <form class="mt-8 space-y-4" @submit.prevent="submit">
              <div class="space-y-2">
                <label class="text-xs font-medium uppercase tracking-wider text-muted" for="username">
                  Username
                </label>
                <Input
                  id="username"
                  v-model="form.username"
                  name="username"
                  autocomplete="username"
                  placeholder="hermione"
                />
              </div>
              <div class="space-y-2">
                <label class="text-xs font-medium uppercase tracking-wider text-muted" for="password">
                  Password
                </label>
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
                class="flex items-start gap-2 rounded-md border border-danger/40 bg-danger/10 p-3 text-xs text-danger"
              >
                <AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
                <span>{{ error }}</span>
              </div>

              <Button type="submit" size="lg" class="w-full" :disabled="loading">
                <Loader2 v-if="loading" class="h-4 w-4 animate-spin" />
                <LogIn v-else class="h-4 w-4" />
                {{ loading ? 'Signing in…' : 'Sign in' }}
              </Button>
            </form>

            <p class="mt-6 text-center text-sm text-muted">
              New to Lingo Quest?
              <RouterLink to="/register" class="text-foreground underline decoration-border underline-offset-4 transition hover:decoration-accent">
                Create an account
              </RouterLink>
            </p>
          </div>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
