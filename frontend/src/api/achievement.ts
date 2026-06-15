import { request } from './http'

export interface AchievementCatalog {
  id: number
  code: string
  title: string
  description: string
  icon: string
  rewardExp: number
  rewardCoin: number
}

export interface UnlockedAchievement extends AchievementCatalog {
  unlockedAt: string
}

export const achievementApi = {
  catalog: () => request<AchievementCatalog[]>({ url: '/achievements', method: 'GET' }),
  mine: () => request<UnlockedAchievement[]>({ url: '/achievements/mine', method: 'GET' }),
}
