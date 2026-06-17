<script setup lang="ts">
import { Shield } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'

const sections = [
  { id: 'collect',    title: '我们收集的信息' },
  { id: 'use',        title: '信息如何被使用' },
  { id: 'third',      title: '第三方服务' },
  { id: 'cookies',    title: 'Cookie 与本地存储' },
  { id: 'security',   title: '数据安全' },
  { id: 'rights',     title: '你的权利' },
  { id: 'retention',  title: '数据的保留期' },
  { id: 'minors',     title: '未成年人保护' },
  { id: 'contact',    title: '隐私问题的反馈渠道' },
]
</script>

<template>
  <AppLayout>
    <section class="container py-16">
      <div class="mx-auto max-w-3xl">
        <header>
          <div class="label-slash">
            <Shield class="mr-1.5 h-3.5 w-3.5" />
            法律
          </div>
          <h1 class="mt-5 font-display text-display-lg font-black leading-[0.98] tracking-tight">
            隐私政策
          </h1>
          <p class="mt-5 max-w-[55ch] text-[15px] leading-[1.75] text-ink-soft">
            学习这件事本身已经够费心神,我们不想让"数据怎么被用"也成为你的负担。
            下面这份隐私政策说明了 Lingo Quest 在你的学习过程中会接触到哪些数据,以及它们都流向了哪里。
          </p>
          <p class="mt-3 font-mono text-[12px] uppercase tracking-[0.18em] text-ink-faint">
            最后更新于 2026 年 6 月 16 日 · 版本 1.0
          </p>
        </header>

        <nav class="mt-10 rounded-md border border-rule bg-paper-2 p-5">
          <div class="label-slash">目录</div>
          <ol class="mt-3 grid gap-1.5 sm:grid-cols-2">
            <li v-for="(s, i) in sections" :key="s.id" class="text-[13.5px]">
              <a
                :href="`#${s.id}`"
                class="inline-flex items-baseline gap-2 text-ink-soft transition-colors hover:text-ink"
              >
                <span class="font-mono text-[11px] text-ink-faint">{{ String(i + 1).padStart(2, '0') }}</span>
                {{ s.title }}
              </a>
            </li>
          </ol>
        </nav>

        <article class="mt-14 space-y-14 text-[15px] leading-[1.8] text-ink-soft">
          <section id="collect">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">1. 我们收集的信息</h2>
            <p class="mt-4">为了让你能登录、玩棋盘、查看战绩,Lingo Quest 会收集以下三类数据:</p>
            <ul class="mt-3 list-inside list-disc space-y-2 pl-2">
              <li>
                <strong class="font-medium text-ink">账号信息</strong>:用户名、电子邮箱、密码(以 BCrypt 哈希存储,我们看不到明文)、可选的头像 URL。
              </li>
              <li>
                <strong class="font-medium text-ink">学习数据</strong>:对局记录、掷骰结果、答题历史、词书学习状态、徽章解锁时间、虚拟金币余额与道具库存。
              </li>
              <li>
                <strong class="font-medium text-ink">技术数据</strong>:登录时的 IP 地址、浏览器 UA、请求时间戳 —— 仅用于安全审计与故障排查,我们不构建用户画像。
              </li>
            </ul>
          </section>

          <section id="use">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">2. 信息如何被使用</h2>
            <p class="mt-4">
              我们仅在以下场景使用你的数据:为你呈现个性化的棋盘体验、生成 AI 导师的回答、计算正确率趋势与排行榜、向你发送账号安全相关通知。
            </p>
            <p class="mt-3">
              <strong class="font-medium text-ink">我们绝不会出售你的数据</strong>,也不会向广告平台分享你的学习记录。
              你的每一次掷骰、每一次回答都属于你。
            </p>
          </section>

          <section id="third">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">3. 第三方服务</h2>
            <p class="mt-4">
              当你使用 AI 导师功能(语法纠错、单词讲解)时,你输入的文本会以 HTTPS 加密形式发送至
              <strong class="font-medium text-ink">DeepSeek</strong>(深度求索)的 API。
              他们的隐私政策见 deepseek.com。我们对发送的文本做内容缓存(24 小时 TTL),
              用于降低重复请求的成本与延迟,缓存内容仅项目组内部可见。
            </p>
          </section>

          <section id="cookies">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">4. Cookie 与本地存储</h2>
            <p class="mt-4">
              Lingo Quest 不使用任何第三方追踪 Cookie。
              我们仅在你的浏览器 localStorage 中保存一个名为 <code class="rounded-sm bg-paper-3 px-1.5 py-0.5 font-mono text-[12.5px] text-ink">lingo-quest-auth</code>
              的本地存储项,用来记住你的登录态(JWT token)与已装备的装饰道具。退出登录后该项即被清除。
            </p>
          </section>

          <section id="security">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">5. 数据安全</h2>
            <p class="mt-4">我们在多个层面保护你的数据:</p>
            <ul class="mt-3 list-inside list-disc space-y-2 pl-2">
              <li>密码使用 BCrypt 算法哈希存储,无法逆向</li>
              <li>所有客户端 ↔ 服务端通信采用 HTTPS 加密</li>
              <li>API 鉴权采用 JWT + 服务端密钥校验,Token 默认 24 小时过期</li>
              <li>AI 调用配有每用户每分钟 10 次的速率限制,防止滥用与代刷</li>
              <li>数据库定期备份,关键操作有审计日志</li>
            </ul>
          </section>

          <section id="rights">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">6. 你的权利</h2>
            <p class="mt-4">作为数据主体,你随时可以:</p>
            <ul class="mt-3 list-inside list-disc space-y-2 pl-2">
              <li>在「个人主页 / 资料」中查看与修改邮箱、头像</li>
              <li>在「个人主页 / 密码」中修改密码</li>
              <li>在「个人主页 / 战绩」中导出你的对局历史与学习进度</li>
              <li>在「个人主页 / 账号」中提交账号注销请求</li>
            </ul>
          </section>

          <section id="retention">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">7. 数据的保留期</h2>
            <p class="mt-4">
              账号活跃期间,我们持续保留你的学习数据以便提供服务。
              账号注销后,你的数据会先进入软删除状态(标记为 <code class="rounded-sm bg-paper-3 px-1.5 py-0.5 font-mono text-[12.5px] text-ink">deleted = 1</code>),保留 30 天以应对误删与司法配合需要,
              30 天后从所有生产数据库中物理删除。
            </p>
          </section>

          <section id="minors">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">8. 未成年人保护</h2>
            <p class="mt-4">
              Lingo Quest 面向英语学习者开放,内容包含小学等级的词书。若你是不满 14 周岁的儿童,
              请在监护人陪同下注册和使用。我们不会主动收集儿童的额外个人信息;若发现误收集,会立即删除。
            </p>
          </section>

          <section id="contact">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">9. 隐私问题的反馈渠道</h2>
            <p class="mt-4">
              对本政策有疑问、希望行使数据权利,或发现可能的数据泄露,请通过
              <a href="https://www.shanchuann.cn/about/#contact" target="_blank" rel="noopener noreferrer" class="text-ink underline decoration-rule-soft underline-offset-[6px] hover:decoration-ink">联系我们</a>
              页面留言。我们会在 5 个工作日内回复。
            </p>
          </section>
        </article>
      </div>
    </section>
  </AppLayout>
</template>
