# Lingo Quest

AI English board-game learning platform — final project for 《软件设计与体系结构》.

## Stack

- **Frontend**: Vue 3.5 + Vite 6 + TypeScript + Tailwind CSS + shadcn-vue + Lucide
- **Backend**: Java 21 + Spring Boot 3.3 + MyBatis-Plus + Spring Security 6 + JWT
- **Data**: MySQL 8, Redis 7
- **AI**: DeepSeek API (OpenAI-compatible)
- **Architecture**: Modular monolith, package-per-domain

## Quick start

```bash
# 1. Configure secrets
cp .env.example .env
# edit .env and fill DEEPSEEK_API_KEY etc.

# 2. Start infra
docker compose -f deployment/docker-compose.yml up -d

# 3. Backend
cd backend
mvn spring-boot:run
# Swagger UI: http://localhost:8080/swagger-ui.html

# 4. Frontend
cd frontend
pnpm install
pnpm dev
# Open http://localhost:5173
```

## Layout

```
LingoQuest/
├── backend/       Spring Boot API
├── frontend/      Vue 3 SPA
├── database/      seed data / sample questions
├── docs/          UML / XADL / ATAM artifacts
├── deployment/    docker-compose + nginx
└── .env           secrets (gitignored)
```

See `../项目设计.md` for the full design spec and `../开发计划.md` for the 4-week plan.
