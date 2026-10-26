<script setup lang="ts">
import { ref } from 'vue'
import { ChevronDown, LifeBuoy, Dice5, BookOpen, ShoppingBag, Trophy, User, Sparkles } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'

interface FaqGroup {
  id: string
  title: string
  icon: any
  items: { q: string; a: string }[]
}

const groups: FaqGroup[] = [
  {
    id: 'start',
    title: '入门',
    icon: Sparkles,
    items: [
      {
        q: '我是新用户,该从哪里开始?',
        a: '注册账号后,先在「词书」里选一本最贴近你水平的书(小学 / 中考 / 高考 / CET-4 / CET-6 / 考研 / 雅思 / 托福 共 8 本),然后到「棋盘」点「开始新一局」掷骰子。落到单词格时,题目会优先从你选中的词书里出。',
      },
      {
        q: '可以同时进行多局棋盘吗?',
        a: '同一时间只能有一局进行中。点「开始新一局」会自动放弃当前未完成的对局并开新局。',
      },
      {
        q: '为什么默认是浅色?有深色模式吗?',
        a: '当前版本只提供浅色主题。这是基于"学习场景多发生在白天 / 中等亮度环境"的设计判断。后续版本会按需评估深色支持。',
      },
    ],
  },
  {
    id: 'board',
    title: '棋盘',
    icon: Dice5,
    items: [
      {
        q: '棋盘上各种颜色的格子分别代表什么?',
        a: '绿色 = 起点 / 蓝色 = 单词 / 紫色 = 语法 / 粉色 = 口语 / 薄荷绿 = 幸运 / 红色 = 陷阱 / 橙色 = 挑战 / 琥珀色 = 终点。单词 / 语法 / 口语 / 挑战格会弹出题目,幸运 / 陷阱直接结算奖惩。',
      },
      {
        q: '掷骰子后的"提示卷"、"重掷令"、"越格符"怎么用?',
        a: '到「商城」购买后,会出现在题目弹窗右上(提示卷 / 越格符)和骰子卡片下方(重掷令)。点击使用即可消耗一张,即时生效。',
      },
      {
        q: '"下题双倍经验"是什么时候触发的?',
        a: '在「商城 / 库存」点了「使用」XP_DOUBLE 道具后,该 buff 持续到下一次"答对"为止 —— 答对时经验翻倍,然后 buff 自动清除。答错不消耗。',
      },
    ],
  },
  {
    id: 'wordbook',
    title: '词书',
    icon: BookOpen,
    items: [
      {
        q: '词书可以同时学多本吗?',
        a: '词书的「学习进度」是按本书独立记录的(NEW / LEARNING / MASTERED),不会互相覆盖。但棋盘出题时只会用你设为「当前词书」的那一本。',
      },
      {
        q: '为什么每本词书只有 25 个词?',
        a: '当前版本是 demo 数据。每本词书的 25 个词都经过精选,覆盖该等级的代表性词汇。后续我们会接入完整词表。',
      },
      {
        q: '一个单词的"已掌握"状态怎么变?',
        a: '两种方式:在词书详情页手动点击切换;或者在棋盘上答对该单词的题目,会自动标为 MASTERED。',
      },
    ],
  },
  {
    id: 'shop',
    title: '商城',
    icon: ShoppingBag,
    items: [
      {
        q: '金币怎么获得?',
        a: '掷骰子落到幸运格、答对题、完成对局到达终点都会获得金币。陷阱格会扣除少量经验,但不扣金币。',
      },
      {
        q: '装饰道具(骰皮 / 棋盘主题)买了为什么没生效?',
        a: '首次购买的装饰会自动装备并替换同类已装备项。如果之前手动卸下了,可以在「商城 / 库存」或商品卡上点「装备」切换。装备后的视觉效果会立即反映到棋盘和骰子上。',
      },
      {
        q: '加速卡能解锁任何徽章吗?',
        a: '加速卡对应的是某一类特定徽章(如"词海速记"卡解锁 VOCAB_MASTER)。如果对应徽章已经解锁,使用时会自动退款,不消耗道具。',
      },
    ],
  },
  {
    id: 'progress',
    title: '进度与战绩',
    icon: Trophy,
    items: [
      {
        q: '等级和经验是怎么算的?',
        a: 'Lv1: 0~99 xp,Lv2: 100~249,Lv3: 250~499,Lv4: 500~899,Lv5: 900+。答对得 +10 xp(双倍卡 +20),答错 -2,掉到 0 为止。等级会随经验自动跃迁。',
      },
      {
        q: '排行榜什么时候更新?',
        a: '总榜是实时的,基于 user.experience 排序;周榜从 Redis ZSET 读取,每次答题事件触发后立即写入,所以也基本实时。每周一 0:00 重置周榜键。',
      },
      {
        q: '战绩里的"正确率趋势"统计什么?',
        a: '最近 14 天每天的"答对题数 / 总答题数"。如果某一天没有答题,折线会跳过这一天。',
      },
    ],
  },
  {
    id: 'account',
    title: '账号',
    icon: User,
    items: [
      {
        q: '怎么修改密码?',
        a: '「个人主页 / 密码」页填旧密码 + 新密码 + 确认新密码。修改成功后当前会话不会被踢出,但下次登录需要用新密码。',
      },
      {
        q: '怎么换头像?',
        a: '「个人主页 / 资料」里填入图片 URL 即可。URL 失败时会回落到首字母占位,改对 URL 会自动恢复。',
      },
      {
        q: '注销账号会丢失什么?',
        a: '注销后账号、对局历史、词书进度、金币、库存全部进入软删状态,保留 30 天以应对误删,30 天后物理删除。注销前请确认你不会再回来。',
      },
    ],
  },
]

const openItems = ref(new Set<string>())
function toggle(key: string) {
  if (openItems.value.has(key)) openItems.value.delete(key)
  else openItems.value.add(key)
}
</script>

<template>
  <AppLayout>
    <section class="container py-10 sm:py-16">
      <div class="mx-auto max-w-3xl">
        <header>
          <div class="label-slash">
            <LifeBuoy class="mr-1.5 h-3.5 w-3.5" />
            帮助
          </div>
          <h1 class="mt-5 font-display text-display-lg font-black leading-[0.98] tracking-tight">
            帮助中心
          </h1>
          <p class="mt-5 max-w-[55ch] text-[15px] leading-[1.75] text-ink-soft">
            按场景分组的常见问题。如果下面找不到答案,请通过页脚「联系我们」直接跟我们说。
          </p>
        </header>

        <div class="mt-8 space-y-8 sm:mt-12 sm:space-y-10">
          <section v-for="g in groups" :key="g.id" :id="g.id" class="scroll-mt-20 lg:scroll-mt-0">
            <header class="mb-5 flex items-center gap-3 border-b border-rule-soft pb-3">
              <span class="inline-flex h-9 w-9 shrink-0 items-center justify-center rounded-sm bg-pixel-green text-ink">
                <component :is="g.icon" class="h-4 w-4" />
              </span>
              <h2 class="min-w-0 font-display text-[22px] font-black leading-tight tracking-tight text-ink">{{ g.title }}</h2>
              <span class="ml-auto shrink-0 font-mono text-[11px] uppercase tracking-[0.18em] text-ink-faint">
                {{ g.items.length }} 个问题
              </span>
            </header>

            <ul class="divide-y divide-rule-soft">
              <li
                v-for="(item, idx) in g.items"
                :key="`${g.id}-${idx}`"
              >
                <button
                  class="flex w-full items-start gap-2.5 py-4 text-left transition-colors hover:bg-paper-2 focus-ring sm:gap-3"
                  @click="toggle(`${g.id}-${idx}`)"
                >
                  <span class="mt-1 font-mono text-[11px] text-ink-faint">
                    Q{{ String(idx + 1).padStart(2, '0') }}
                  </span>
                  <span class="min-w-0 flex-1 break-words font-display text-[15.5px] font-bold leading-snug text-ink">
                    {{ item.q }}
                  </span>
                  <ChevronDown
                    class="mt-1 h-4 w-4 shrink-0 text-ink-soft transition-transform duration-200"
                    :class="openItems.has(`${g.id}-${idx}`) ? 'rotate-180' : ''"
                  />
                </button>
                <Transition
                  enter-active-class="transition duration-200 ease-out-expo"
                  enter-from-class="opacity-0 -translate-y-1"
                  leave-active-class="transition duration-150"
                  leave-to-class="opacity-0"
                >
                  <div
                    v-if="openItems.has(`${g.id}-${idx}`)"
                    class="pb-5 pl-8 pr-4 text-[14.5px] leading-[1.75] text-ink-soft sm:pl-[40px] sm:pr-8"
                  >
                    {{ item.a }}
                  </div>
                </Transition>
              </li>
            </ul>
          </section>
        </div>

        <!-- Contact CTA -->
        <div class="mt-12 rounded-md border-2 border-ink bg-pixel-amber/30 p-5 sm:mt-16 sm:p-6">
          <div class="label-slash">没找到答案?</div>
          <h3 class="mt-3 font-display text-[22px] font-black leading-tight tracking-tight text-ink">
            直接告诉我们,会优先排进路线图。
          </h3>
          <p class="mt-3 max-w-[55ch] text-[14.5px] leading-[1.7] text-ink-soft">
            Lingo Quest 还在持续打磨。无论是发现 bug、想要的新词书、对界面或玩法的建议 ——
            最快的反馈方式就是直接告诉我们。
          </p>
          <a
            href="https://www.shanchuann.cn/about/#contact"
            target="_blank"
            rel="noopener noreferrer"
            class="mt-5 inline-flex h-11 items-center gap-2 rounded-md bg-ink px-5 text-[14px] font-medium text-paper transition-colors hover:bg-ink-soft focus-ring"
          >
            联系我们
            <span class="font-mono text-[11px] opacity-70">↗</span>
          </a>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
