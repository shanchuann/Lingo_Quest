# 问题与经验总结 (对应课程评分点 1-8)

> 在 Lingo Quest 的设计与实现过程中遇到的关键问题、解决方式，以及尚未解决的部分。

## 问题 1 — OpenAI 在国内不可直连

**现象**：原设计文档默认调用 OpenAI API，但是国内访问 `api.openai.com` 需要代理且要绑定境外信用卡。

**解决**：改用 **DeepSeek** 的 OpenAI-Compatible 接口（`base_url=https://api.deepseek.com`），仅需修改 `application.yml` 中的两行配置，Java 代码完全不动（`spring-ai-openai` 或自写 WebClient 都兼容）。这一选择同时记录在 ATAM 评估的 AD-02 决策表里，作为「可修改性」的具体例证（场景 S5）。

## 问题 2 — 微服务 vs. 单体的权衡

**现象**：课程报告希望写「微服务架构」拿创新分，但单人 4 周做真·微服务（Spring Cloud + Nacos + Gateway + 各自部署）极易翻车。

**解决**：采用 **模块化单体**（modular monolith）：一个 Maven 工程，按领域分包 `user / game / learning / ai / achievement / ranking`；跨模块通信走 Spring **Application Event**（异步发布订阅），不直接调对方 Service。这样：
- 实现层面：一个 jar，一个端口，演示稳定；
- 报告层面：构件图把六个包画成「逻辑微服务」，配合事件驱动叙事完全成立；
- 演化层面：将来真要拆服务，只需把事件换成 Kafka，包变 module，不必重构业务逻辑。

## 问题 3 — LLM 输出不可控

**现象**：DeepSeek 返回的 JSON 偶尔会被 markdown 代码围栏（``` ```json ... ``` ```）包住，或多输出一段「解释」。直接 `objectMapper.readValue` 会抛异常。

**解决**：
1. 在 system prompt 强制「Reply ONLY with JSON, no prose, no markdown fences」并显式给出 JSON Schema 字段。
2. `DeepSeekClient.chatAsJson()` 在解析前对返回字符串做 `replaceFirst("^```json\\s*", "")` 等清洗。
3. 解析失败时抛 `BizException(50002, "AI returned malformed response")`，由 `GlobalExceptionHandler` 转成 400 + 友好提示。
4. 周报场景还有兜底逻辑：拿不到 AI 回复就用本地规则生成「夸奖+建议」。

## 问题 4 — AI 调用的成本与限流

**现象**：DeepSeek 按 token 计费，如果用户疯狂调用 `/ai/grammar` 会迅速烧钱；同时同一个句子被反复纠错没有意义。

**解决**：
- `AiCache`：以 `sha256(input)` 为 key、24h TTL 写 Redis。同一个句子一天内只调一次。
- `AiRateLimiter`：每用户每分钟 10 次的 Redis INCR + EXPIRE 限流；超额返回 `429` + 中文友好提示。
- `DeepSeekClient` 30 秒超时，超时降级返回「AI 老师正在思考」，避免请求线程被长时间挂住。

## 问题 5 — 跨模块通信的「显式但解耦」

**现象**：答题完成后，需要同时触发：①写学习记录 ②加经验扣金币 ③成就判定 ④更新排行榜。如果都写在 `GameService.answer()` 里，会变成胖方法且模块之间强耦合。

**解决**：`GameService.answer()` 内只完成「答题判分 + 用户经验更新」，然后 `publishEvent(new AnswerSubmittedEvent(...))`。`AchievementService` 和 `RankingService` 用 `@Async @EventListener` 异步消费：
- 成就服务负责检查 5 个解锁规则、写 `user_achievement`、发奖励；
- 排行榜服务负责 `ZINCRBY` 周榜。

好处：单元测试 `GameService` 时不用 mock 上述两个服务；以后要拆微服务，把事件换成 Kafka 即可，业务零改动。

## 问题 6 — Spring Security + JWT 的状态化陷阱

**现象**：默认 Spring Security 会创建 HTTP Session，导致接口看起来鉴权通过但每个请求都生成 JSESSIONID，与 JWT 设计冲突。

**解决**：在 `SecurityConfig` 显式 `sessionCreationPolicy(STATELESS)` + 自定义 `JwtAuthFilter` 直接从 `Authorization: Bearer xxx` 头解析，注入 `SecurityContextHolder`。CORS 放行通过单独的 `CorsFilter` Bean 在过滤器链最前。

## 问题 7 — MyBatis-Plus 的逻辑删除字段命名冲突

**现象**：MyBatis-Plus 默认 `@TableLogic` 的字段名约定可能与数据库实际字段对不上。

**解决**：在 `application.yml` 显式声明 `mybatis-plus.global-config.db-config.logic-delete-field=deleted`，并在每个需要软删的实体（User / Game / Question 等）的字段上加 `@TableLogic`。schema 中所有相关表都加 `deleted TINYINT DEFAULT 0` 字段保持一致。这一致性被 ATAM 的「Sensitivity Point S-B」明确记录。

## 问题 8 — 前端「不允许 emoji」的硬约束

**现象**：很多 UI 框架（Element Plus、Naive UI）默认会用 emoji 做状态指示器，需求方明确要求全程 Lucide 图标。

**解决**：组件库选 `shadcn-vue`（不预设图标）+ `lucide-vue-next` 图标库；所有状态/按钮/卡片图标统一从 Lucide 引入（`Dice5`、`Trophy`、`Sparkles` …）；toast/error 用 `<AlertCircle />` 而非 emoji；棋盘格子也用 Lucide 区分 `LUCKY` / `TRAP` / `CHALLENGE` 等类型。

## 未完成 / 未来工作

| 项 | 现状 | 后续 |
|---|---|---|
| AI 对话陪练 (多轮 role-play) | 后端 prompt 框架已就绪，未做多轮上下文管理 | 用 Redis 存会话 ID + 消息历史，新增 `/ai/dialogue` API |
| 流式 (SSE) 响应 | 现在 WebClient `.block()` | 改用 `Flux<String>`，前端 `EventSource` 接收 |
| 移动端适配 | 桌面优先，移动端布局未做断点 | 用 `/adapt` skill 加 sm/md/lg 断点 |
| 多人对战 | 单人单局 | 引入 WebSocket，房间 + 回合制 |
| 自定义题库 | 题库固定 30 条种子 | 教师/玩家上传 CSV 导入 |
| 真·微服务化 | 模块化单体 | Spring Cloud + Nacos + Gateway 拆 6 个服务，事件换 RocketMQ |

## 收获

1. **「单体写得像微服务」是真的可行**：领域驱动包结构 + Spring Event 让边界清晰，演化路径自然。
2. **LLM 必须当作「外部依赖」**对待：缓存、限流、超时、降级、JSON Schema 校验都不可省。
3. **Tailwind + shadcn-vue 拷贝式组件**比堆 Element Plus 更适合做「有品味」的 UI；GitHub Universe 那种深色辉光风格本质上是 token + 渐变 + 留白 + 不用 emoji。
4. **文档先行真的省时间**：用 PlantUML 把图先画对，写代码时遇到歧义直接对图，少返工。
