<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { UserPlus, Sparkles, AlertCircle, Loader2 } from 'lucide-vue-next'
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
    error.value = e?.response?.data?.message || e?.message || 'Registration failed'
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
              Start your quest
            </div>
            <h1 class="text-2xl font-semibold tracking-tight">Create an account</h1>
            <p class="mt-2 text-sm text-muted">
              Free forever. No credit card required.
            </p>

            <form class="mt-8 space-y-4" @submit.prevent="submit">
              <div class="space-y-2">
                <label class="text-xs font-medium uppercase tracking-wider text-muted" for="r-username">
                  Username
                </label>
                <Input
                  id="r-username"
                  v-model="form.username"
                  autocomplete="username"
                  placeholder="3-32 characters"
                />
              </div>
              <div class="space-y-2">
                <label class="text-xs font-medium uppercase tracking-wider text-muted" for="r-email">
                  Email
                </label>
                <Input
                  id="r-email"
                  v-model="form.email"
                  type="email"
                  autocomplete="email"
                  placeholder="you@example.com"
                />
              </div>
              <div class="space-y-2">
                <label class="text-xs font-medium uppercase tracking-wider text-muted" for="r-password">
                  Password
                </label>
                <Input
                  id="r-password"
                  v-model="form.password"
                  type="password"
                  autocomplete="new-password"
                  placeholder="At least 6 characters"
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
                <UserPlus v-else class="h-4 w-4" />
                {{ loading ? 'Creating…' : 'Create account' }}
              </Button>
            </form>

            <p class="mt-6 text-center text-sm text-muted">
              Already have an account?
              <RouterLink to="/login" class="text-foreground underline decoration-border underline-offset-4 transition hover:decoration-accent">
                Sign in
              </RouterLink>
            </p>
          </div>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
