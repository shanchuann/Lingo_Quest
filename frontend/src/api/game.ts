import { request } from './http'

export interface TileView {
  position: number
  tileType: 'START' | 'END' | 'VOCAB' | 'GRAMMAR' | 'SPEAKING' | 'LUCKY' | 'TRAP' | 'CHALLENGE'
  tileName: string
  rewardExp: number
  rewardCoin: number
}

export interface GameView {
  id: number
  userId: number
  currentPosition: number
  roundNum: number
  status: 'CREATED' | 'IN_PROGRESS' | 'PAUSED' | 'FINISHED' | 'ABANDONED'
  totalScore: number
}

export interface QuestionView {
  id: number
  type: string
  content: string
  options: string[] | null
  difficulty: number
}

export interface RollResult {
  dice: number
  fromPosition: number
  toPosition: number
  landed: TileView
  question: QuestionView | null
  finished: boolean
}

export interface AnswerResult {
  correct: boolean
  correctAnswer: string
  gainedExp: number
  totalExp: number
  level: number
  levelUp: boolean
}

export interface HintResult {
  questionId: number
  hint: string
  message: string
}

export const gameApi = {
  board: () => request<TileView[]>({ url: '/games/board', method: 'GET' }),
  current: () => request<GameView>({ url: '/games/current', method: 'GET' }),
  create: () => request<GameView>({ url: '/games', method: 'POST' }),
  roll: (id: number) => request<RollResult>({ url: `/games/${id}/roll`, method: 'POST' }),
  answer: (id: number, questionId: number, answer: string) =>
    request<AnswerResult>({ url: `/games/${id}/answer`, method: 'POST', data: { questionId, answer } }),
  abandon: (id: number) => request<GameView>({ url: `/games/${id}/abandon`, method: 'POST' }),
  hint: (id: number, questionId: number) =>
    request<HintResult>({ url: `/games/${id}/hint`, method: 'POST', params: { questionId } }),
  skip: (id: number, questionId: number) =>
    request<AnswerResult>({ url: `/games/${id}/skip`, method: 'POST', params: { questionId } }),
  retry: (id: number) =>
    request<RollResult>({ url: `/games/${id}/retry`, method: 'POST' }),
}
