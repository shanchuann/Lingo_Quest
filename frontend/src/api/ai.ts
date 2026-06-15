import { request } from './http'

export interface GrammarCheck {
  original: string
  corrected: string
  explanation_zh: string
  is_correct: boolean
}

export interface VocabCoach {
  word: string
  phonetic: string
  part_of_speech: string
  meaning_zh: string
  meaning_en: string
  examples: string[]
  synonyms: string[]
  memory_tip: string
}

export const aiApi = {
  grammar(sentence: string) {
    return request<GrammarCheck>({ url: '/ai/grammar', method: 'POST', data: { sentence } })
  },
  vocab(word: string) {
    return request<VocabCoach>({ url: '/ai/vocab', method: 'POST', data: { word } })
  },
}
