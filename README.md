# Lingo Quest

> AI English board-game learning platform — final project for 《软件设计与体系结构》(软件23级 学号尾号 9, 娱乐+教育方向).

![status](https://img.shields.io/badge/status-feature--complete-7c3aed)
![java](https://img.shields.io/badge/java-21-orange)
![spring](https://img.shields.io/badge/spring--boot-3.3-green)
![vue](https://img.shields.io/badge/vue-3.5-42b883)
![ai](https://img.shields.io/badge/AI-DeepSeek-2563eb)

## Highlights

- **Layered modular monolith** in Spring Boot 3 (Java 21) with domain events bridging the `game`, `achievement` and `ranking` packages — looks like microservices, ships like a monolith.
- **AI Tutor** powered by DeepSeek (OpenAI-compatible API): grammar correction, vocabulary coaching, and weekly learning report — all guarded by Redis-cached responses, per-user rate limiting, and a 30 s fallback.
- **30-tile board game**: dice roll, tile event, question challenge, XP / level / coins, achievements via event listeners, Redis-ZSET weekly leaderboard.
- **GitHub Universe-styled UI**: Vue 3 + Tailwind + shadcn-vue + Lucide icons, deep canvas with gradient glow, zero emojis, dark by default.

## Stack

| Layer | Tech |
|---|---|
| Frontend | Vue 3.5, Vite 6, TypeScript, Tailwind, shadcn-vue, Lucide, Pinia, Axios, ECharts |
| Backend  | Spring Boot 3.3, Spring Security 6, JJWT, MyBatis-Plus 3.5, SpringDoc OpenAPI |
| Data     | MySQL 8.0, Redis 7(AI 缓存 / 限流 / 周榜;**可选**,`REDIS_ENABLED=false` 时降级为进程内实现) |
| AI       | DeepSeek `deepseek-chat` via OpenAI-compatible HTTP |

## Quick start

```bash
# 0. Set secrets
cp .env.example .env
# fill DEEPSEEK_API_KEY, JWT_SECRET, etc.

# 1. Bring up infra
docker compose -f deployment/docker-compose.yml up -d
# MySQL auto-loads database/init/*.sql (schema + 30 tiles + 30 questions + 8 achievements)

# 2. Backend
cd backend
mvn spring-boot:run
# Swagger UI: http://localhost:8080/api/swagger-ui.html

# 3. Frontend
cd frontend
pnpm install
pnpm dev
# Open http://localhost:5173
```

## Deploy

Vercel 只能托管 Vue 前端(静态 + Serverless,没有常驻 JVM 和数据库),所以生产部署拆成两段:

- **前端** → [Vercel](https://vercel.com):Root Directory 设为 `frontend`,构建 `pnpm build`,输出 `dist`。
- **后端 + MySQL** → [Railway](https://railway.com):用仓库自带的 `backend/Dockerfile` 构建,业务数据用 `database/init/*.sql` 导入。Redis 可选,只加 MySQL 时设 `REDIS_ENABLED=false`(AI 缓存 / 限流 / 周榜自动降级为进程内实现,详见 DEPLOY.md 2.6)。

浏览器请求的 `/api/**` 由 `frontend/vercel.json` 反向代理到后端,前端代码和后端 CORS 都不需要改。

完整步骤(建项目、环境变量、数据库导入、验收清单、排错表)见 **[DEPLOY.md](DEPLOY.md)**。

## Project layout

```
LingoQuest/
├── backend/        Spring Boot modular monolith
│   └── src/main/java/com/lingoquest/
│       ├── common/        R<T>, BizException, handler, JWT util
│       ├── config/        security/, redis, mybatis-plus, async, cors
│       ├── user/          register / login / me
│       ├── game/          board, dice, answer, events
│       ├── learning/      questions + records
│       ├── ai/            tutor controller + service
│       ├── achievement/   event listener + catalog
│       ├── ranking/       Redis ZSET weekly + SQL all-time
│       ├── report/        AI weekly summary
│       └── infra/ai/      DeepSeekClient, AiCache, AiRateLimiter
├── frontend/       Vue 3 SPA (Tailwind + shadcn-vue + Lucide)
├── database/init/  SQL schema + seeds (auto-loaded by MySQL container)
├── docs/
│   ├── uml/        13 PlantUML files (usecase/domain/sequence/...)
│   ├── xadl/       xADL 2.0 description
│   └── atam-report.md
└── deployment/
    └── docker-compose.yml
```

## API surface

| Group | Endpoint | Auth |
|---|---|---|
| Auth | `POST /auth/register`, `POST /auth/login` | public |
| User | `GET /users/me` | JWT |
| Game | `GET /games/board`, `POST /games`, `GET /games/current`, `POST /games/{id}/roll`, `POST /games/{id}/answer`, `POST /games/{id}/abandon` | JWT |
| AI | `POST /ai/grammar`, `POST /ai/vocab` | JWT |
| Achievement | `GET /achievements`, `GET /achievements/mine` | JWT |
| Ranking | `GET /ranking/weekly`, `GET /ranking/all-time` | JWT |
| Report | `GET /reports/weekly` | JWT |

Full OpenAPI: `http://localhost:8080/api/v3/api-docs`

## Course deliverables

| Rubric | Where to find it |
|---|---|
| 1-1 调研 / 需求 | `../项目设计.md` §1-§2 |
| 1-2 架构图 | `../项目设计.md` §3 + `docs/uml/12_component.puml`, `13_deployment.puml` |
| 1-3 4+1 视图 | `../项目设计.md` §4 |
| 1-4 UML 全套 | `docs/uml/*.puml` (13 files) |
| 1-5 XADL 2.0 | `docs/xadl/lingo-quest.xml` |
| 1-6 ATAM | `docs/atam-report.md` |
| 1-7 实现 | this repo (frontend + backend) |
| 1-8 问题总结 | `../项目设计.md` §10 + `docs/lessons-learned.md` |

## Screenshots

Run `pnpm dev` and visit:
- `/` — landing page (GitHub Universe-style hero)
- `/register` & `/login` — gradient auth cards
- `/game` — 30-tile board + dice + question modal
- `/tutor` — grammar fix + vocab coach
- `/report` — accuracy gauge + AI tutor's note
- `/ranking`, `/achievements` — leaderboard, badge collection

## License

Course project — for academic use.
