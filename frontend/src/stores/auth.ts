import { defineStore } from 'pinia'
import { authApi, type UserProfile } from '@/api/auth'

interface AuthState {
  token: string | null
  user: UserProfile | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: null,
    user: null,
  }),
  getters: {
    isAuthed: (s) => !!s.token,
  },
  actions: {
    async login(username: string, password: string) {
      const data = await authApi.login({ username, password })
      this.token = data.token
      this.user = data.user
    },
    async register(payload: { username: string; email: string; password: string }) {
      const data = await authApi.register(payload)
      this.token = data.token
      this.user = data.user
    },
    async refreshMe() {
      if (!this.token) return
      this.user = await authApi.me()
    },
    logout() {
      this.token = null
      this.user = null
    },
  },
  persist: {
    key: 'lingo-quest-auth',
    storage: localStorage,
  },
})
