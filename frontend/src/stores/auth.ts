import { defineStore } from 'pinia'
import { authApi, type UserProfile } from '@/api/auth'
import { shopApi } from '@/api/shop'

interface EquippedCosmetics {
  diceHue?: string | null      // amber | violet | ...
  boardHue?: string | null     // ocean | sakura | ...
  avatarFrame?: string | null  // laurel | ...
}

interface AuthState {
  token: string | null
  user: UserProfile | null
  equipped: EquippedCosmetics
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    token: null,
    user: null,
    equipped: {},
  }),
  getters: {
    isAuthed: (s) => !!s.token,
  },
  actions: {
    async login(username: string, password: string) {
      const data = await authApi.login({ username, password })
      this.token = data.token
      this.user = data.user
      this.refreshEquipped().catch(() => {})
    },
    async register(payload: { username: string; email: string; password: string }) {
      const data = await authApi.register(payload)
      this.token = data.token
      this.user = data.user
      this.refreshEquipped().catch(() => {})
    },
    async refreshMe() {
      if (!this.token) return
      this.user = await authApi.me()
      this.refreshEquipped().catch(() => {})
    },
    async refreshEquipped() {
      if (!this.token) return
      try {
        const inv = await shopApi.inventory()
        const eq: EquippedCosmetics = {}
        for (const it of inv) {
          if (it.equipped !== 1) continue
          const hue = (it.payload?.hue as string | undefined) ?? null
          const style = (it.payload?.style as string | undefined) ?? null
          if (it.effect === 'DICE_SKIN')    eq.diceHue = hue
          if (it.effect === 'BOARD_THEME')  eq.boardHue = hue
          if (it.effect === 'AVATAR_FRAME') eq.avatarFrame = style
        }
        this.equipped = eq
      } catch {
        // non-fatal
      }
    },
    logout() {
      this.token = null
      this.user = null
      this.equipped = {}
    },
  },
  persist: {
    key: 'lingo-quest-auth',
    storage: localStorage,
  },
})
