# 课程报告写作大纲（对应 Word 模板）

> 把本文件每一节的内容粘进《软件设计与体系结构》大作业-报告模板对应的章节即可。
> 所有图、代码、表格的引用路径都已经写在本仓库中，直接打开/截图即可。

---

## 封面

- 题目：Lingo Quest — AI 英语桌游学习系统
- 学号尾号：9（娱乐+教育方向）

## 摘要（150~200 字）

Lingo Quest 是一个把「桌游」与「AI 教师」结合的英语学习平台。玩家在 30 格棋盘上掷骰移动，落在不同格子上触发词汇、语法、对话或挑战任务，由 DeepSeek 提供的 AI 教师给出即时反馈与个性化建议。系统采用 Spring Boot 3 模块化单体 + Vue 3 现代前端 + MySQL/Redis 双存储 + DeepSeek（OpenAI 兼容协议）的技术栈，使用分层 + 事件驱动的混合体系结构风格，完成了课程要求的 4+1 视图、完整 UML、xADL 2.0 描述与 ATAM 评估。

## 一、前期调研 (1-1)

抄录 `项目设计.md` §1 §2：
- 社会价值（中国学生英语学习痛点 + 桌游+AI 的差异化定位）
- 功能需求 9 条 (FR-01 ~ FR-09)
- 非功能需求 6 项

## 二、体系结构与架构图 (1-2)

- 文字描述：`项目设计.md` §3.1 (技术栈) + §3.2 (架构图)
- 截图：`docs/uml/12_component.puml`（构件图）+ `13_deployment.puml`（部署图）

## 三、体系结构风格与 4+1 视图 (1-3)

- 风格：分层 + 事件驱动
- 4+1 视图分别由以下产物覆盖：

| 视图 | 产物 |
|---|---|
| Logical | `docs/uml/01_usecase.puml`, `02_domain.puml` |
| Development | `docs/uml/12_component.puml` + 仓库目录树 |
| Process | `docs/uml/04_*`, `05_*`, `06_*` (顺序图)、`08_*`, `09_*` (活动图) |
| Physical | `docs/uml/13_deployment.puml` |
| Scenario | 5 个关键用例（注册 / 开局 / 答题 / AI 纠错 / 周报） |

## 四、UML 建模 (1-4)

按子条目逐一引用：

- 4.1 业务用例图 → `docs/uml/01_usecase.puml`
- 4.2 业务领域类图 → `docs/uml/02_domain.puml`（含实体、操作、关系）
- 4.3 顺序图 → `03_*.puml`、`04_*.puml`、`05_*.puml`、`06_*.puml`
- 4.4 协作图 → `07_collaboration_ai_grammar.puml`
- 4.5 活动图 → `08_activity_answer.puml`、`09_activity_ai.puml`
- 4.6 状态图 → `10_state_game.puml`、`11_state_user_level.puml`
- 4.7 构件图 → `12_component.puml`
- 4.8 部署图 → `13_deployment.puml`

每一节插一张 PlantUML 渲染的 PNG 图，旁边写 100 字说明即可。

## 五、xADL 2.0 描述 (1-5)

- 直接贴 `docs/xadl/lingo-quest.xml`
- 文字补一段：本文件描述了 3 组构件+连接件+接口+配置：
  1. WebClient ↔ ApiGateway（HTTP/JSON）
  2. GameService ↔ AiTutorService（Spring Event + JVM Call）
  3. DeepSeekClient ↔ DeepSeekProvider（HTTPS REST）

## 六、ATAM 评估 (1-6)

直接抄 `docs/atam-report.md`：
- 商业动机 5 条
- 质量属性 utility tree（性能/可用性/可修改性/安全/可测/易用）
- 11 个场景表
- 风险 4、敏感点 3、权衡点 4
- 关键决策日志 5 条

## 七、系统实现 (1-7)

- 技术选型清单（`项目设计.md` §3.1）
- 关键代码截图（每个截图 100~200 字说明）：

  1. `LingoQuestApplication.java` — 启动类
  2. `SecurityConfig.java` + `JwtAuthFilter.java` — 鉴权链
  3. `GameService.java` — 掷骰 + 答题判分核心
  4. `AchievementService.java` — `@EventListener` 异步成就解锁
  5. `DeepSeekClient.java` + `AiCache.java` + `AiRateLimiter.java` — AI 三重护栏
  6. `Home.vue` / `Game.vue` / `Tutor.vue` / `Report.vue` — 前端关键页面

- 数据库表结构截图（`database/init/01_schema.sql` 一次性贴完）
- 运行截图：
  1. 注册成功（深色登录注册卡片）
  2. 棋盘页（30 格 + 骰子 + 玩家位置）
  3. 答题弹窗 + 答对 toast
  4. AI 语法纠错 / 词汇教练结果
  5. 学习周报（精度仪表盘 + AI 建议）
  6. 排行榜
  7. 成就墙
  8. Swagger UI（证明 API 文档完整）

## 八、问题总结 (1-8)

直接抄 `docs/lessons-learned.md`：8 个具体问题 + 解决方式 + 未完成项。

## 答辩 PPT 建议结构（10 页）

1. 封面 + 一句话定位
2. 痛点与立项（FR/NFR 表）
3. 整体架构图（分层 + 模块 + 事件总线）
4. 4+1 视图速览
5. 核心闭环 demo gif（roll → answer → exp → level up）
6. AI 三重护栏（缓存命中 / 限流 / 超时降级 — 配 ATAM 场景）
7. 关键 UML 图（领域类图 + 顺序图）
8. ATAM 结论：风险 / 敏感点 / 权衡点
9. 难点与解决（挑 3 个最有故事的）
10. 未来工作 + 致谢
