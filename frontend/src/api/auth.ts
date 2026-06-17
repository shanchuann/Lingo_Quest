import { request } from './http'

export interface UserProfile {
  id: number
  username: string
  email: string
  avatar?: string
  level: number
  experience: number
  coins: number
  currentBookId?: number | null
  xpDoublePending?: number
}

export interface AuthResponse {
  token: string
  expireInMs: number
  user: UserProfile
}

export interface UserStats {
  totalGames: number
  finishedGames: number
  totalAnswers: number
  correctAnswers: number
  accuracy: number
  level: number
  experience: number
  coins: number
}

export const authApi = {
  register(payload: { username: string; email: string; password: string }) {
    return request<AuthResponse>({ url: '/auth/register', method: 'POST', data: payload })
  },
  login(payload: { username: string; password: string }) {
    return request<AuthResponse>({ url: '/auth/login', method: 'POST', data: payload })
  },
  me() {
    return request<UserProfile>({ url: '/users/me', method: 'GET' })
  },
  updateProfile(payload: { email?: string; avatar?: string }) {
    return request<UserProfile>({ url: '/users/me', method: 'PUT', data: payload })
  },
  changePassword(payload: { oldPassword: string; newPassword: string }) {
    return request<void>({ url: '/users/me/password', method: 'PUT', data: payload })
  },
  stats() {
    return request<UserStats>({ url: '/users/me/stats', method: 'GET' })
  },
  deleteAccount() {
    return request<void>({ url: '/users/me', method: 'DELETE' })
  },
  recentGames(limit = 10) {
    return request<Array<{
      id: number
      currentPosition: number
      roundNum: number
      status: string
      totalScore: number
      startTime: string
      endTime?: string | null
    }>>({ url: '/users/me/games', method: 'GET', params: { limit } })
  },
  trend(days = 14) {
    return request<Array<{ day: string; correct: number; total: number }>>({
      url: '/users/me/stats/trend', method: 'GET', params: { days },
    })
  },
}
