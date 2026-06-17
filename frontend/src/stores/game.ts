import { defineStore } from 'pinia'
import { gameApi, type TileView, type GameView, type QuestionView, type RollResult, type AnswerResult } from '@/api/game'

interface GameState {
  board: TileView[]
  game: GameView | null
  rolling: boolean
  diceValue: number | null
  pendingQuestion: QuestionView | null
  lastRoll: RollResult | null
  lastAnswer: AnswerResult | null
}

export const useGameStore = defineStore('game', {
  state: (): GameState => ({
    board: [],
    game: null,
    rolling: false,
    diceValue: null,
    pendingQuestion: null,
    lastRoll: null,
    lastAnswer: null,
  }),
  getters: {
    inProgress: (s) => s.game?.status === 'IN_PROGRESS',
    finished: (s) => s.game?.status === 'FINISHED',
  },
  actions: {
    async loadBoard() {
      if (this.board.length === 0) {
        this.board = await gameApi.board()
      }
    },
    async loadCurrent() {
      try {
        this.game = await gameApi.current()
      } catch {
        this.game = null
      }
    },
    async createGame() {
      this.game = await gameApi.create()
      this.pendingQuestion = null
      this.lastRoll = null
      this.lastAnswer = null
      this.diceValue = null
    },
    async rollDice() {
      if (!this.game || this.rolling) return
      this.rolling = true
      try {
        const result = await gameApi.roll(this.game.id)
        this.lastRoll = result
        this.diceValue = result.dice
        this.game.currentPosition = result.toPosition
        this.game.roundNum += 1
        this.pendingQuestion = result.question ?? null
        if (result.finished) this.game.status = 'FINISHED'
      } finally {
        this.rolling = false
      }
    },
    async submitAnswer(answer: string) {
      if (!this.game || !this.pendingQuestion) return
      const ans = await gameApi.answer(this.game.id, this.pendingQuestion.id, answer)
      this.lastAnswer = ans
      this.game.totalScore += ans.gainedExp
      this.pendingQuestion = null
      return ans
    },
    async abandon() {
      if (!this.game) return
      this.game = await gameApi.abandon(this.game.id)
    },
    clearAnswerToast() {
      this.lastAnswer = null
    },
    async useHint() {
      if (!this.game || !this.pendingQuestion) return null
      const r = await gameApi.hint(this.game.id, this.pendingQuestion.id)
      return r.hint
    },
    async useSkip() {
      if (!this.game || !this.pendingQuestion) return
      const r = await gameApi.skip(this.game.id, this.pendingQuestion.id)
      this.lastAnswer = r
      this.pendingQuestion = null
    },
    async useRetry() {
      if (!this.game) return
      const r = await gameApi.retry(this.game.id)
      this.lastRoll = r
      this.diceValue = r.dice
      this.game.currentPosition = r.toPosition
      this.game.roundNum += 1
      this.pendingQuestion = r.question ?? null
      if (r.finished) this.game.status = 'FINISHED'
    },
  },
})
