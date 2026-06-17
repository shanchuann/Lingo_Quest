import { request } from './http'

export type ShopCategory = 'FUNCTIONAL' | 'COSMETIC' | 'ACCELERATOR'
export type ShopEffect =
  | 'HINT' | 'RETRY' | 'SKIP' | 'XP_DOUBLE'
  | 'DICE_SKIN' | 'BOARD_THEME' | 'AVATAR_FRAME'
  | 'ACH_UNLOCK'

export interface ShopItem {
  id: number
  code: string
  name: string
  description: string
  category: ShopCategory
  effect: ShopEffect
  price: number
  icon: string
  payload?: Record<string, any> | null
  consumable: number       // 1 or 0
  sortOrder: number
  enabled: number
}

export interface InventoryEntry {
  id: number
  itemId: number
  quantity: number
  equipped: number
  acquiredAt: string
  code: string
  name: string
  description: string
  category: ShopCategory
  effect: ShopEffect
  price: number
  icon: string
  payload?: Record<string, any> | null
  consumable: number
}

export interface PurchaseResult {
  itemId: number
  code: string
  quantity: number
  remainingCoins: number
  message: string
}

export interface UseResult {
  itemId: number
  effect: ShopEffect
  remainingQuantity: number
  message: string
}

export const shopApi = {
  items: () => request<ShopItem[]>({ url: '/shop/items', method: 'GET' }),
  inventory: () => request<InventoryEntry[]>({ url: '/shop/inventory', method: 'GET' }),
  purchase: (itemId: number) =>
    request<PurchaseResult>({ url: `/shop/purchase/${itemId}`, method: 'POST' }),
  use: (itemId: number) =>
    request<UseResult>({ url: `/shop/inventory/${itemId}/use`, method: 'POST' }),
  equip: (itemId: number, on: boolean) =>
    request<void>({
      url: `/shop/inventory/${itemId}/equip`,
      method: 'POST',
      params: { on },
    }),
}
