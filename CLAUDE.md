# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project: Lingo Quest

**Lingo Quest** is an AI-powered English learning platform structured as a board game. It is the final project for a *Software Design & Architecture* course (学号尾号 9 → 娱乐 + 教育 方向).

The grade is split across 8 deliverables (1-1 ~ 1-8) defined in `软件设计与体系结构-大作业考核内容-软件23级.pdf`. Implementation correctness (1-7) is one of eight scoring points — **documentation completeness (UML, XADL, ATAM, 4+1 views) is just as important**. When planning work, do not skip the docs for the code.

`项目设计.md` is the authoritative design spec. `开发计划.md` is the week-by-week plan. `项目设计.原始版本.md` is a backup of the user's first draft and must not be edited.

## Tech stack (final, do not regress to the original draft)

- **Frontend**: Vue 3.5 + Vite 6 + TypeScript + Tailwind CSS + **shadcn-vue** (copied into `components/ui/`) + **Lucide Vue Next** icons + Pinia + Vue Router + Axios + ECharts
- **Backend**: Java 21 + Spring Boot 3.3 + Spring Security 6 + JJWT + MyBatis-Plus + SpringDoc OpenAPI + Lombok/MapStruct, Maven
- **Data**: MySQL 8, Redis 7 (ZSET ranking, AI response cache, token-bucket rate limit, JWT blacklist)
- **AI**: **DeepSeek API** via OpenAI-compatible protocol (`base_url=https://api.deepseek.com`, model `deepseek-chat`). Do NOT use OpenAI SDK directly — DeepSeek is the configured provider. Spring AI's `spring-ai-openai-spring-boot-starter` works against DeepSeek by overriding `base-url`.
- **Architecture**: **Modular monolith** in one Spring Boot module, package-per-domain (`user`, `game`, `learning`, `ai`, `achievement`, `ranking`). The course report describes it using layered + microservice terminology — code stays a single deployable.

## Hard rules for the frontend

These come from the user and are non-negotiable:

1. **Aesthetic target**: clone the look-and-feel of <https://githubuniverse.com> — deep dark canvas (`#0d1117` / `#010409`), blue-purple gradient accents, Inter + JetBrains Mono typography, generous spacing, subtle gradient hover states.
2. **No emoji anywhere in UI** — not in components, not in placeholder text, not in toast messages. Use **Lucide icons** (`Dice5`, `Trophy`, `Sparkles`, `BookOpen`, `User`, `LogIn`, …) instead.
3. Use the **`/impeccable`** skill when crafting new pages/components. It enforces the design quality required here. Run `/polish` before declaring UI work done.
4. Component primitives come from **shadcn-vue** — copy into `frontend/src/components/ui/`, don't add a heavy component library (no Element Plus, no Ant Design Vue).

## Commands

Once scaffolding lands under `lingo-quest/`, the following commands will apply. They are not yet wired up — when you create the project, set them up to match this contract.

```bash
# infra (run from lingo-quest/)
docker compose -f deployment/docker-compose.yml up -d   # MySQL + Redis

# backend (from lingo-quest/backend/)
mvn spring-boot:run                 # dev server on :8080, Swagger at /swagger-ui.html
mvn test                            # run all tests
mvn test -Dtest=GameServiceTest     # single test class
mvn test -Dtest=GameServiceTest#rollDice_movesPlayerCorrectly  # single method
mvn -DskipTests package             # build jar

# frontend (from lingo-quest/frontend/)
pnpm install
pnpm dev                            # Vite dev server on :5173
pnpm build                          # type-check + production build
pnpm test                           # vitest
pnpm test -- src/pages/Game.spec.ts # single file
pnpm lint                           # eslint
```

Required env vars (backend):
```
DEEPSEEK_API_KEY=sk-...
SPRING_DATASOURCE_PASSWORD=...
JWT_SECRET=...
```

## Architecture notes future Claude needs to know

- **AI calls are guarded by three things** — Redis-cached responses (24h TTL keyed by prompt hash), per-user Redis token-bucket rate limit (10/min), and a 3s timeout with a friendly fallback message. Don't bypass these when adding new AI features.
- **Game flow is event-driven inside the monolith**: `landOn(tile)` publishes a Spring `ApplicationEvent`; the `learning` module reacts to question tiles, `achievement` listens for any state change. Keep cross-module calls through events, not direct service injection, to preserve the "split into microservices later" narrative.
- **Ranking has two paths**: weekly leaderboard lives in Redis ZSET `lingo:rank:weekly:{isoWeek}`; the all-time board reads from `user.experience`. There is intentionally **no `ranking` table** — the original draft had one; the corrected design dropped it.
- **State machines are explicit**: `Game.status` ∈ `CREATED|IN_PROGRESS|PAUSED|FINISHED|ABANDONED`, `User.level` ∈ `Lv1..Lv5`. State transitions must go through the entity methods, not raw setters — the state diagrams in `docs/uml/state.puml` are part of the deliverable.
- **DB schema differs from the original draft**: passwords are 72 chars (BCrypt), `question.options` is JSON, `learning_record` joins back to `game_id`, and there's a `user_achievement` join table. The schema in `项目设计.md §8.2` is the source of truth — earlier ER drafts are obsolete.

## Documentation deliverables (don't skip)

| Course point | Output file | Tool |
|---|---|---|
| 1-4 UML | `docs/uml/*.puml` (usecase/domain/sequence/collab/activity/state/component/deploy) | PlantUML |
| 1-5 XADL 2.0 | `docs/xadl/lingo-quest.xml` | hand-written XML |
| 1-6 ATAM | `docs/atam-report.md` | markdown |
| API docs | auto-served at `/swagger-ui.html` | SpringDoc |

When asked to "add a feature", also update the relevant UML files in the same change — they're graded artifacts, not optional.

## Files of note in this directory

- `项目设计.md` — authoritative design spec (rewrite of user's draft, do not lose its structure)
- `项目设计.原始版本.md` — backup of original draft, **read-only**
- `开发计划.md` — 4-week plan, daily granularity
- `《软件设计与体系结构》大作业-报告模板-202606.doc` — the report template students must fill in for grading
- `软件设计与体系结构-大作业考核内容-软件23级.pdf` — the grading rubric (1-1 ~ 1-8)
