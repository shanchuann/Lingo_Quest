<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import * as Icons from 'lucide-vue-next'
import {
  Coins,
  Package,
  ShoppingBag,
  Loader2,
  Check,
  AlertCircle,
  Sparkles,
  Wand2,
  Palette,
  Zap,
  ChevronRight,
} from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'
import Button from '@/components/ui/Button.vue'
import { useAuthStore } from '@/stores/auth'
import { shopApi, type ShopItem, type InventoryEntry, type ShopCategory } from '@/api/shop'

const auth = useAuthStore()

const items = ref<ShopItem[]>([])
const inventory = ref<InventoryEntry[]>([])
const loading = ref(false)
const purchasingId = ref<number | null>(null)
const usingId = ref<number | null>(null)
const equippingId = ref<number | null>(null)
const flash = ref<{ kind: 'ok' | 'err'; text: string } | null>(null)

type Tab = 'all' | ShopCategory | 'inventory'
const tab = ref<Tab>('all')

const categories: Array<{ k: Tab; label: string; icon: any; swatch: string }> = [
  { k: 'all',         label: '全部',     icon: ShoppingBag, swatch: 'bg-ink' },
  { k: 'FUNCTIONAL',  label: '功能道具', icon: Wand2,       swatch: 'bg-pixel-green' },
  { k: 'COSMETIC',    label: '装饰道具', icon: Palette,     swatch: 'bg-pixel-pink' },
  { k: 'ACCELERATOR', label: '加速卡',   icon: Zap,         swatch: 'bg-pixel-amber' },
  { k: 'inventory',   label: '我的库存', icon: Package,     swatch: 'bg-pixel-blue' },
]

const filteredItems = computed(() => {
  if (tab.value === 'all') return items.value
  if (tab.value === 'inventory') return []
  return items.value.filter((i) => i.category === tab.value)
})

const ownedMap = computed(() => {
  const m = new Map<number, InventoryEntry>()
  inventory.value.forEach((i) => m.set(i.itemId, i))
  return m
})

onMounted(load)

async function load() {
  loading.value = true
  try {
    const [catalog, inv] = await Promise.all([shopApi.items(), shopApi.inventory()])
    items.value = catalog
    inventory.value = inv
  } finally {
    loading.value = false
  }
}

function fade() {
  window.setTimeout(() => (flash.value = null), 3500)
}

async function buy(item: ShopItem) {
  flash.value = null
  purchasingId.value = item.id
  try {
    const r = await shopApi.purchase(item.id)
    flash.value = { kind: 'ok', text: r.message }
    if (auth.user) auth.user.coins = r.remainingCoins
    await load()
    fade()
  } catch (e: any) {
    flash.value = { kind: 'err', text: e?.response?.data?.message || '购买失败' }
    fade()
  } finally {
    purchasingId.value = null
  }
}

async function useItem(entry: InventoryEntry) {
  flash.value = null
  usingId.value = entry.itemId
  try {
    const r = await shopApi.use(entry.itemId)
    flash.value = { kind: 'ok', text: r.message }
    await load()
    await auth.refreshMe()
    await auth.refreshEquipped()
    fade()
  } catch (e: any) {
    flash.value = { kind: 'err', text: e?.response?.data?.message || '使用失败' }
    fade()
  } finally {
    usingId.value = null
  }
}

async function toggleEquip(entry: InventoryEntry) {
  flash.value = null
  equippingId.value = entry.itemId
  try {
    const on = entry.equipped !== 1
    await shopApi.equip(entry.itemId, on)
    flash.value = { kind: 'ok', text: on ? `已装备 · ${entry.name}` : `已卸下 · ${entry.name}` }
    await load()
    await auth.refreshEquipped()
    fade()
  } catch (e: any) {
    flash.value = { kind: 'err', text: e?.response?.data?.message || '操作失败' }
    fade()
  } finally {
    equippingId.value = null
  }
}

// Same as toggleEquip but starting from a catalog item; finds the inventory entry first.
async function toggleEquipFromCatalog(item: ShopItem) {
  const entry = ownedMap.value.get(item.id)
  if (!entry) return
  await toggleEquip(entry)
}

function resolveIcon(name: string) {
  return (Icons as any)[name] ?? Sparkles
}

// Map category → pixel swatch (used to color the icon backdrop)
const swatchFor: Record<ShopCategory, string> = {
  FUNCTIONAL:  'bg-pixel-green text-ink',
  COSMETIC:    'bg-pixel-pink text-ink',
  ACCELERATOR: 'bg-pixel-amber text-ink',
}
</script>

<template>
  <AppLayout>
    <section class="container py-8 md:py-12">
      <div class="mx-auto max-w-5xl">
        <header class="flex flex-wrap items-end justify-between gap-4">
          <div>
            <div class="label-slash">
              <ShoppingBag class="mr-1.5 h-3.5 w-3.5" />
              积分商城
            </div>
            <h1 class="mt-4 font-display text-display-lg font-black leading-[0.98] tracking-tight">
              用金币兑换<span class="text-ink-faint">小小的助力。</span>
            </h1>
            <p class="mt-3 max-w-[44ch] text-[14.5px] text-ink-soft">
              功能道具能改变下一步棋的局面,装饰道具能让你的棋盘自成一派,
              加速卡能直接点亮你想要的徽章。
            </p>
          </div>
          <div class="rounded-md border-2 border-ink bg-pixel-amber px-5 py-3">
            <div class="text-[11px] uppercase tracking-[0.18em] text-ink">钱包</div>
            <div class="mt-1 flex items-baseline gap-2 font-display text-[28px] font-black leading-none">
              <Coins class="h-5 w-5" />
              {{ auth.user?.coins ?? 0 }}
            </div>
          </div>
        </header>

        <!-- Tabs -->
        <nav class="mt-8 flex flex-wrap gap-1 rounded-md border border-rule bg-paper p-1 md:mt-10">
          <button
            v-for="t in categories"
            :key="t.k"
            class="inline-flex h-10 items-center gap-2 rounded-sm px-3.5 text-[13.5px] transition-colors duration-200 focus-ring"
            :class="tab === t.k ? 'bg-ink text-paper font-medium' : 'text-ink-soft hover:text-ink'"
            @click="tab = t.k"
          >
            <span :class="['inline-block h-2 w-2', t.swatch]" />
            <component :is="t.icon" class="h-3.5 w-3.5" />
            {{ t.label }}
            <span v-if="t.k === 'inventory' && inventory.length" class="ml-1 rounded-sm bg-pixel-amber/80 px-1.5 py-0.5 font-mono text-[10px] text-ink">
              {{ inventory.length }}
            </span>
          </button>
        </nav>

        <!-- Flash -->
        <Transition
          enter-active-class="transition duration-300 ease-out"
          enter-from-class="opacity-0 -translate-y-2"
          leave-active-class="transition duration-200"
          leave-to-class="opacity-0"
        >
          <div
            v-if="flash"
            class="mt-4 flex items-start gap-2 rounded-md border-2 p-3 text-[13px] text-ink"
            :class="flash.kind === 'ok' ? 'border-pixel-green bg-pixel-green/15' : 'border-danger bg-pixel-red/15'"
          >
            <Check v-if="flash.kind === 'ok'" class="mt-0.5 h-4 w-4 shrink-0 text-success" />
            <AlertCircle v-else class="mt-0.5 h-4 w-4 shrink-0 text-danger" />
            <span>{{ flash.text }}</span>
          </div>
        </Transition>

        <!-- Loading -->
        <div v-if="loading" class="mt-10 rounded-md border border-rule bg-paper p-10 text-center text-[13.5px] text-ink-soft">
          <Loader2 class="mx-auto mb-2 h-5 w-5 animate-spin" />
          打开柜台中…
        </div>

        <!-- INVENTORY tab -->
        <div v-else-if="tab === 'inventory'" class="mt-6">
          <div v-if="inventory.length === 0" class="rounded-md border border-rule bg-paper p-10 text-center">
            <Package class="mx-auto mb-3 h-7 w-7 text-ink-faint" />
            <p class="text-[14px] text-ink-soft">还没拥有任何道具 —— 先去逛逛柜台。</p>
            <Button class="mt-5" variant="bone" @click="tab = 'all'">
              <ShoppingBag class="h-3.5 w-3.5" />
              返回柜台
            </Button>
          </div>
          <div v-else class="grid gap-[2px] overflow-hidden rounded-md border border-rule bg-rule md:grid-cols-2">
            <article
              v-for="e in inventory"
              :key="e.id"
              class="relative flex flex-col gap-4 bg-paper p-5"
            >
              <div class="flex items-start gap-4">
                <div :class="['flex h-12 w-12 shrink-0 items-center justify-center rounded-sm', swatchFor[e.category]]">
                  <component :is="resolveIcon(e.icon)" class="h-6 w-6" />
                </div>
                <div class="flex-1">
                  <div class="flex items-center gap-2 font-display text-[18px] font-black leading-tight tracking-tight">
                    {{ e.name }}
                    <span v-if="e.equipped === 1" class="rounded-sm bg-pixel-green/30 px-1.5 py-0.5 text-[11px] text-ink">已装备</span>
                  </div>
                  <p class="mt-2 text-[13.5px] leading-relaxed text-ink-soft">{{ e.description }}</p>
                  <div class="mt-3 flex items-center gap-3 font-mono text-[11px] text-ink-soft">
                    <span>持有 · {{ e.quantity }}</span>
                    <span class="text-ink-faint">·</span>
                    <span>{{ e.consumable === 1 ? '消耗' : '装饰' }}</span>
                  </div>
                </div>
              </div>
              <div class="flex justify-end gap-2">
                <Button
                  v-if="e.consumable === 1"
                  size="sm"
                  :disabled="usingId === e.itemId"
                  class="min-h-[40px] sm:min-h-0"
                  @click="useItem(e)"
                >
                  <Loader2 v-if="usingId === e.itemId" class="h-3.5 w-3.5 animate-spin" />
                  <Zap v-else class="h-3.5 w-3.5" />
                  使用
                </Button>
                <Button
                  v-else
                  size="sm"
                  :variant="e.equipped === 1 ? 'bone' : 'ink'"
                  :disabled="equippingId === e.itemId"
                  class="min-h-[40px] sm:min-h-0"
                  @click="toggleEquip(e)"
                >
                  <Loader2 v-if="equippingId === e.itemId" class="h-3.5 w-3.5 animate-spin" />
                  <Check v-else-if="e.equipped === 1" class="h-3.5 w-3.5" />
                  <Sparkles v-else class="h-3.5 w-3.5" />
                  {{ e.equipped === 1 ? '已装备 · 卸下' : '装备' }}
                </Button>
              </div>
            </article>
          </div>
        </div>

        <!-- CATALOG -->
        <div v-else>
          <div v-if="filteredItems.length === 0" class="mt-10 rounded-md border border-rule bg-paper p-10 text-center text-[13.5px] text-ink-soft">
            该类目下暂无道具。
          </div>
          <div v-else class="mt-6 grid gap-[2px] overflow-hidden rounded-md border border-rule bg-rule sm:grid-cols-2 lg:grid-cols-3">
            <article
              v-for="i in filteredItems"
              :key="i.id"
              class="group relative flex flex-col gap-4 bg-paper p-5 transition-colors hover:bg-paper-2"
            >
              <div class="flex items-start justify-between">
                <div :class="['flex h-12 w-12 items-center justify-center rounded-sm', swatchFor[i.category]]">
                  <component :is="resolveIcon(i.icon)" class="h-6 w-6" />
                </div>
                <div class="flex items-center gap-1 rounded-sm border border-rule-soft bg-paper px-2 py-1 font-mono text-[11.5px] text-ink">
                  <Coins class="h-3 w-3" />
                  {{ i.price }}
                </div>
              </div>
              <div class="flex-1">
                <div class="font-display text-[18px] font-black leading-tight tracking-tight">{{ i.name }}</div>
                <p class="mt-2 text-[13px] leading-relaxed text-ink-soft">{{ i.description }}</p>
              </div>
              <div class="flex items-center justify-between font-mono text-[11px] text-ink-soft">
                <span>{{ i.consumable === 1 ? '消耗品' : '装饰' }}</span>
                <span v-if="ownedMap.has(i.id)" class="rounded-sm bg-pixel-green/25 px-1.5 py-0.5 text-ink">
                  已有 · {{ ownedMap.get(i.id)?.quantity }}
                </span>
              </div>
              <!-- For owned cosmetics, the CTA becomes an equip toggle so the user
                   can switch skins/themes without leaving the catalog. -->
              <template v-if="i.consumable !== 1 && ownedMap.has(i.id)">
                <Button
                  size="sm"
                  :variant="ownedMap.get(i.id)?.equipped === 1 ? 'bone' : 'ink'"
                  :disabled="purchasingId === i.id"
                  class="min-h-[40px] sm:min-h-0"
                  @click="toggleEquipFromCatalog(i)"
                >
                  <Loader2 v-if="purchasingId === i.id" class="h-3.5 w-3.5 animate-spin" />
                  <Check v-else-if="ownedMap.get(i.id)?.equipped === 1" class="h-3.5 w-3.5" />
                  <ShoppingBag v-else class="h-3.5 w-3.5" />
                  {{ ownedMap.get(i.id)?.equipped === 1 ? '已装备 · 卸下' : '装备' }}
                </Button>
              </template>
              <Button
                v-else
                size="sm"
                :variant="(auth.user?.coins ?? 0) < i.price ? 'bone' : 'pop'"
                :disabled="purchasingId === i.id || (auth.user?.coins ?? 0) < i.price"
                class="min-h-[40px] sm:min-h-0"
                @click="buy(i)"
              >
                <Loader2 v-if="purchasingId === i.id" class="h-3.5 w-3.5 animate-spin" />
                <ShoppingBag v-else class="h-3.5 w-3.5" />
                <template v-if="(auth.user?.coins ?? 0) < i.price">金币不足</template>
                <template v-else>购买</template>
                <ChevronRight v-if="purchasingId !== i.id" class="h-3.5 w-3.5" />
              </Button>
            </article>
          </div>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
