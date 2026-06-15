import { request } from './http'

export interface UserProfile {
  id: number
  username: string
  email: string
  avatar?: string
  level: number
  experience: number
  coins: number
}

export interface AuthResponse {
  token: string
  expireInMs: number
  user: UserProfile
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
}
