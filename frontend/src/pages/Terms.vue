<script setup lang="ts">
import { FileText } from 'lucide-vue-next'
import AppLayout from '@/layouts/AppLayout.vue'

const sections = [
  { id: 'account',   title: '账号注册与使用' },
  { id: 'content',   title: '内容与知识产权' },
  { id: 'virtual',   title: '虚拟金币与道具' },
  { id: 'ai',        title: 'AI 服务说明' },
  { id: 'avail',     title: '服务可用性' },
  { id: 'prohibit',  title: '禁止行为' },
  { id: 'liability', title: '责任限制' },
  { id: 'changes',   title: '条款的变更' },
  { id: 'contact',   title: '联系方式与适用法律' },
]
</script>

<template>
  <AppLayout>
    <section class="container py-10 sm:py-16">
      <div class="mx-auto max-w-3xl">
        <header>
          <div class="label-slash">
            <FileText class="mr-1.5 h-3.5 w-3.5" />
            法律
          </div>
          <h1 class="mt-5 font-display text-display-lg font-black leading-[0.98] tracking-tight">
            服务条款
          </h1>
          <p class="mt-5 max-w-[55ch] text-[15px] leading-[1.75] text-ink-soft">
            欢迎来到 Lingo Quest。注册账号、使用服务即表示你已阅读并同意本条款的全部内容。
            条款采用清晰的中文,我们尽量不让你的时间花在解读法律术语上。
          </p>
          <p class="mt-3 font-mono text-[12px] uppercase tracking-[0.18em] text-ink-faint max-sm:tracking-[0.12em]">
            最后更新于 2026 年 6 月 16 日 · 版本 1.0
          </p>
        </header>

        <!-- TOC -->
        <nav class="mt-8 rounded-md border border-rule bg-paper-2 p-4 sm:mt-10 sm:p-5">
          <div class="label-slash">目录</div>
          <ol class="mt-3 grid gap-1.5 sm:grid-cols-2">
            <li v-for="(s, i) in sections" :key="s.id" class="text-[13.5px]">
              <a
                :href="`#${s.id}`"
                class="inline-flex items-baseline gap-2 text-ink-soft transition-colors hover:text-ink max-sm:h-10 max-sm:items-center"
              >
                <span class="font-mono text-[11px] text-ink-faint">{{ String(i + 1).padStart(2, '0') }}</span>
                {{ s.title }}
              </a>
            </li>
          </ol>
        </nav>

        <article class="mt-10 space-y-10 break-words text-[15px] leading-[1.8] text-ink-soft sm:mt-14 sm:space-y-14">
          <section id="account" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">1. 账号注册与使用</h2>
            <p class="mt-4">
              每位用户应使用真实有效的电子邮箱注册一个账号。账号属于个人,不得出借、出售、共享给他人使用。
              你应妥善保管账号密码,因密码泄露造成的损失由你自行承担。
            </p>
            <p class="mt-3">
              若发现账号被他人盗用,请第一时间通过页脚的「联系我们」与我们沟通,我们会协助你冻结账号。
            </p>
          </section>

          <section id="content" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">2. 内容与知识产权</h2>
            <p class="mt-4">
              Lingo Quest 平台上的词书、题库、棋盘、UI 设计、文案、品牌标识及相关数据库内容,
              其知识产权归属于 Lingo Quest 项目组。未经书面许可,任何人不得复制、转载、改编或用于商业用途。
            </p>
            <p class="mt-3">
              你在平台上产生的学习记录、答题历史等数据归你所有。我们仅在条款及隐私政策允许的范围内使用这些数据。
            </p>
          </section>

          <section id="virtual" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">3. 虚拟金币与道具</h2>
            <p class="mt-4">
              金币、提示卷、重掷令、装饰道具等虚拟物品仅可在 Lingo Quest 内使用,
              <strong class="font-medium text-ink">不可兑换为法定货币或任何第三方资产</strong>,亦不可转赠给其他账号。
            </p>
            <p class="mt-3">
              因系统漏洞、外挂、恶意刷取等行为获得的虚拟物品,我们有权追回并视情节冻结账号。
            </p>
          </section>

          <section id="ai" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">4. AI 服务说明</h2>
            <p class="mt-4">
              AI 导师由第三方 DeepSeek API 提供算力支持。AI 生成的内容不构成任何形式的官方教学建议、考试参考或职业指导。
            </p>
            <p class="mt-3">
              AI 输出可能存在错误、偏差或上下文丢失,请在依赖前自行核实。我们已为高频请求做缓存与降级处理,
              但不保证 AI 服务 100% 可用。
            </p>
          </section>

          <section id="avail" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">5. 服务可用性</h2>
            <p class="mt-4">
              我们会尽合理努力维持服务稳定,但下列情况可能造成服务暂时中断且不构成违约:
              系统升级与维护、第三方依赖故障(如 DeepSeek、CDN、域名解析)、不可抗力(自然灾害、电力中断等)、政府监管要求。
            </p>
          </section>

          <section id="prohibit" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">6. 禁止行为</h2>
            <p class="mt-4">使用 Lingo Quest 时,你同意不:</p>
            <ul class="mt-3 list-inside list-disc space-y-2 pl-2">
              <li>通过爬虫、自动化脚本或其他技术手段绕过速率限制</li>
              <li>对服务发起拒绝服务攻击、注入攻击或任何形式的渗透测试(已获书面授权除外)</li>
              <li>利用平台漏洞获取不当利益,或不及时披露已知漏洞</li>
              <li>在词书、答案、题目讨论中发布违法、暴力、色情或侵犯他人权益的内容</li>
              <li>使用 Lingo Quest 为他人代写作业、代考或提供任何形式的学术不端服务</li>
            </ul>
          </section>

          <section id="liability" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">7. 责任限制</h2>
            <p class="mt-4">
              在适用法律允许的最大范围内,Lingo Quest 不对因使用或无法使用本服务而产生的间接、附带、惩罚性损失承担责任,
              包括但不限于利润损失、数据丢失、学习进度延误。
            </p>
          </section>

          <section id="changes" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">8. 条款的变更</h2>
            <p class="mt-4">
              本条款可能随产品迭代而更新。重大变更将在页面顶部或通过站内通知提前 7 天告知你。
              如果你在变更生效后继续使用本服务,即视为同意更新后的条款。
            </p>
          </section>

          <section id="contact" class="scroll-mt-20 lg:scroll-mt-0">
            <h2 class="font-display text-[24px] font-black tracking-tight text-ink">9. 联系方式与适用法律</h2>
            <p class="mt-4">
              本条款受中华人民共和国法律管辖。任何因本条款或本服务产生的争议,
              双方应首先尝试友好协商;协商不成的,提交项目组所在地有管辖权的人民法院诉讼解决。
            </p>
            <p class="mt-3">
              如对条款有任何疑问,请通过页脚「<a href="https://www.shanchuann.cn/about/#contact" target="_blank" rel="noopener noreferrer" class="text-ink underline decoration-rule-soft underline-offset-[6px] hover:decoration-ink">联系我们</a>」与我们沟通。
            </p>
          </section>
        </article>
      </div>
    </section>
  </AppLayout>
</template>
