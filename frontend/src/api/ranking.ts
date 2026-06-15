import { request } from './http'

export interface RankRow {
  rank: number
  userId: number
  username: string
  level: number
  score?: number
  experience?: number
}

export const rankingApi = {
  weekly: (limit = 20) => request<RankRow[]>({ url: '/ranking/weekly', params: { limit }, method: 'GET' }),
  allTime: (limit = 20) => request<RankRow[]>({ url: '/ranking/all-time', params: { limit }, method: 'GET' }),
}
