import { request } from './http'

export type WordState = 'NEW' | 'LEARNING' | 'MASTERED'

export interface WordBookSummary {
  id: number
  code: string
  name: string
  description: string
  level: string
  coverColor: string
  totalWords: number
  sortOrder: number
  masteredCount: number
  learningCount: number
}

export interface WordEntry {
  id: number
  word: string
  phonetic?: string
  partOfSpeech?: string
  meaningZh: string
  meaningEn?: string
  exampleEn?: string
  exampleZh?: string
  difficulty: number
  sortOrder: number
  state: WordState
}

export interface RandomWord {
  id: number
  bookId: number
  word: string
  phonetic?: string
  partOfSpeech?: string
  meaningZh: string
  meaningEn?: string
  exampleEn?: string
  exampleZh?: string
  difficulty: number
}

export const wordbookApi = {
  books: () => request<WordBookSummary[]>({ url: '/wordbooks', method: 'GET' }),
  words: (bookId: number, state?: WordState) =>
    request<WordEntry[]>({
      url: `/wordbooks/${bookId}/words`,
      method: 'GET',
      params: state ? { state } : undefined,
    }),
  mark: (wordId: number, state: WordState) =>
    request<void>({
      url: `/wordbooks/words/${wordId}/mark`,
      method: 'POST',
      data: { state },
    }),
  select: (bookId: number | null) =>
    request<void>({
      url: '/wordbooks/select',
      method: 'POST',
      params: bookId == null ? {} : { bookId },
    }),
  random: (bookId?: number) =>
    request<RandomWord | null>({
      url: '/wordbooks/random',
      method: 'GET',
      params: bookId ? { bookId } : undefined,
    }),
}
