import { request } from './http'

export interface WeeklyReport {
  stats: {
    totalAnswers: number
    correctAnswers: number
    wrongAnswers: number
    accuracy: number
    xpGained: number
  }
  by_type: Array<{ type: string; total: number; correct: number; accuracy: number }>
  ai_summary: {
    praise_zh: string
    weak_areas: string[]
    suggestions_zh: string[]
    focus_next_week_zh: string
  }
}

export const reportApi = {
  weekly: () => request<WeeklyReport>({ url: '/reports/weekly', method: 'GET' }),
}
