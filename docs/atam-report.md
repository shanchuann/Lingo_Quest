# ATAM 评估报告 — Lingo Quest

> Architecture Tradeoff Analysis Method (ATAM) report for the Lingo Quest learning platform.
> Date: 2026-06-15  ·  Architect: 个人课程设计

---

## 1. Business drivers

| ID | Driver |
|----|--------|
| BD-1 | Course final project demo must run end-to-end on a single laptop. |
| BD-2 | "Game + AI" is the headline differentiator vs. classic flash-card apps. |
| BD-3 | LLM calls must be cost-controlled (DeepSeek API is paid). |
| BD-4 | Defense panel will probe architectural decisions; the codebase must mirror the documented architecture. |
| BD-5 | Open-source on GitHub — code quality and readability matter. |

## 2. Architecture overview

See `项目设计.md` §3-§5 and `docs/uml/12_component.puml`. Summary:

- **Style**: layered (Web / Application / Domain / Infrastructure) over a **modular monolith**, with **event-driven** integration between the `game`, `achievement`, and `ranking` packages.
- **Tech stack**: Vue 3 + Spring Boot 3 + MySQL 8 + Redis 7 + DeepSeek API (OpenAI-compatible).
- **Cross-cutting**: Spring Security + JWT, MyBatis-Plus, AiCache + AiRateLimiter, SpringDoc OpenAPI.

## 3. Quality attribute utility tree

```
Utility
├─ Performance
│   ├─ [H,M] P95 API latency (non-AI) < 300 ms                         (S1)
│   └─ [H,M] AI grammar correction P95 < 3 s (with cache hit < 50 ms)  (S2)
├─ Availability
│   ├─ [M,M] If DeepSeek is unreachable, app remains usable except AI  (S3)
│   └─ [M,L] Restart recovery < 30 s with data preserved               (S4)
├─ Modifiability
│   ├─ [H,L] Swap AI provider (DeepSeek ↔ OpenAI ↔ GLM) in < 1 file    (S5)
│   └─ [H,M] Split game package into a separate service                (S6)
├─ Security
│   ├─ [H,L] No password stored in plain text                          (S7)
│   ├─ [H,L] All endpoints except /auth/** require JWT                 (S8)
│   └─ [H,M] AI endpoints throttled to 10 req/min/user                 (S9)
├─ Testability
│   └─ [M,L] Domain logic (Dice / LevelPolicy) unit-testable in <1 s   (S10)
└─ Usability
    └─ [M,L] First-time user can roll the dice in < 30 s after login   (S11)
```
Notation: `[Business importance, Technical risk]` — H=high, M=medium, L=low.

## 4. Scenarios (stimulus / response)

| ID | Scenario | Stimulus | Response | Architectural mechanism |
|----|---|---|---|---|
| S1 | High-RPS user list | 100 concurrent calls to `/users/me` | P95 latency &lt; 300 ms | MyBatis-Plus pooled JDBC; `user.id` PK lookup |
| S2 | Grammar fix cached | Same sentence sent twice within 24 h | 2nd call &lt; 50 ms | AiCache (Redis 24h TTL, SHA-256 key) |
| S3 | DeepSeek 500 outage | DeepSeek returns 5xx | App returns "AI tutor is thinking, try later"; rest of app fully functional | DeepSeekClient timeout + try/catch, GlobalExceptionHandler |
| S4 | API crash | JVM killed during play | Restart in &lt; 30 s; game/user state intact | MySQL is canonical store; Redis is purely cache; statelessness of JVM |
| S5 | Provider swap | Replace DeepSeek with OpenAI | Edit `application.yml` (`base-url`, `model`); no Java change | OpenAI-compatible HTTP contract |
| S6 | Service extraction | Spin out `game` as microservice | New Maven module, copy events, replace Spring event bus with Kafka/RabbitMQ | Domain events are first-class; package boundaries map to bounded contexts |
| S7 | Plaintext audit | Static analysis on user table | All passwords are BCrypt hashes (60 chars) | BCryptPasswordEncoder enforced in UserService.register |
| S8 | Unauth probe | curl `/games/board` without token | 401 Unauthorized with JSON body | SecurityFilterChain default-deny |
| S9 | Token spam | Same user calls `/ai/grammar` 11x in 1 minute | 11th call returns 429 | AiRateLimiter Redis token bucket |
| S10 | Domain unit test | `mvn test` on `LevelPolicy` | Completes in &lt; 1 s, no Spring context | Pure Java, no Spring dependencies |
| S11 | Onboarding latency | Brand-new user → register → first roll | &lt; 30 s end-to-end | Persisted JWT, single-form register, direct redirect to `/dashboard` then `/game` |

## 5. Risks, sensitivity points, and tradeoffs

### Risks (R)
- **R-1**: DeepSeek free-tier rate limits or pricing change → cost overrun.
  *Mitigation:* AiCache hit ratio dashboards; per-user rate limit; can switch provider via S5.
- **R-2**: Single-node deployment → no HA. *Acceptable* for a course demo, but called out for the defense.
- **R-3**: All AI calls go through a synchronous WebClient `.block()` — under heavy load this could exhaust the request thread pool.
  *Mitigation roadmap:* switch to true reactive return type in milestone v0.2.
- **R-4**: JSON parsing of LLM output is best-effort. *Mitigation:* strict prompts ("reply ONLY with JSON"), markdown-fence stripping, fallback path.

### Sensitivity points (S)
- **S-A**: `app.jwt.secret` must be ≥ 32 bytes; configured in `.env`, enforced in `JwtTokenProvider.init()`.
- **S-B**: `mybatis-plus.global-config.db-config.logic-delete-field=deleted` — every entity with `@TableLogic` relies on this. Changing it breaks soft-delete project-wide.
- **S-C**: Redis ZSET key format `lingo:rank:weekly:{ISO-week}`. Clients reading the leaderboard must know this format; changing it breaks the contract between `RankingService` listeners and the controller.

### Tradeoff points (T)
- **T-1 — Latency vs. cost**: caching AI responses for 24 h sacrifices freshness to slash token spend by ~80 % on repeated queries. Acceptable because grammar / vocab answers are stable.
- **T-2 — Simplicity vs. scalability**: modular monolith ships fast and is easy to demo, but cannot scale individual modules independently. We accept the limit and document the path to split (S6).
- **T-3 — Strong typing vs. LLM flexibility**: enforcing a JSON schema on LLM output makes the response predictable but rejects valid prose answers. We mitigate by stripping markdown fences and providing a fallback.
- **T-4 — Synchronous vs. streaming AI**: non-streaming WebClient block call keeps code simple and works with `R<T>`. Streaming would feel faster but requires SSE on the frontend; deferred.

## 6. Architecture decisions log (key ones)

| ID | Decision | Why | Alternatives rejected |
|----|---|---|---|
| AD-01 | Modular monolith over true microservices | One person, four weeks, defense favors working demo | Spring Cloud + Nacos (too complex), Serverless (cold start, vendor lock-in) |
| AD-02 | DeepSeek as default LLM | Cheapest 中文-capable model with OpenAI-compatible API | OpenAI (network + payment friction), Local Ollama (8GB VRAM not guaranteed) |
| AD-03 | Spring `ApplicationEvent` for cross-module integration | Lets `game` stay ignorant of `achievement`/`ranking`; makes future split easy | Direct method calls (couples modules), message broker (overkill for monolith) |
| AD-04 | Redis ZSET for weekly leaderboard | O(log N) inserts/queries, atomic ranking, decoupled from MySQL load | SQL `ORDER BY` (slow under writes), in-memory map (lost on restart) |
| AD-05 | shadcn-vue + Tailwind + Lucide for UI | Matches GitHub Universe aesthetic; no emoji, no Element Plus bloat | Element Plus (admin look), Vuetify (Material look) |

## 7. Conclusion

The architecture meets the high-priority scenarios (S1, S2, S3, S5, S7, S8, S9, S10, S11) with low technical risk. The two areas to monitor are **R-1 (LLM cost)** and **R-3 (sync blocking)**, both with documented mitigation paths. The single explicit non-goal is multi-node availability (R-2), which the course rubric does not require.

The architecture's main strength is that **domain events make the monolith look and behave like microservices internally**, satisfying both the defense narrative ("we have a Game service, an AI service, …") and the implementation reality (one Maven module, one JAR).
