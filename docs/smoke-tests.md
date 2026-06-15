# Lingo Quest API smoke tests

These are not automated; they're a manual checklist after `mvn spring-boot:run` succeeds.

Base URL: `http://localhost:8080/api`

## 1. Auth

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"a@x.com","password":"secret123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"secret123"}'

# Save token
export TOKEN="paste-token-here"

# Me
curl http://localhost:8080/api/users/me -H "Authorization: Bearer $TOKEN"
```

## 2. Game

```bash
curl http://localhost:8080/api/games/board -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8080/api/games -H "Authorization: Bearer $TOKEN"
# remember the returned game id
curl -X POST http://localhost:8080/api/games/1/roll -H "Authorization: Bearer $TOKEN"
curl -X POST http://localhost:8080/api/games/1/answer \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"questionId":1,"answer":"to give up"}'
```

## 3. AI

```bash
curl -X POST http://localhost:8080/api/ai/grammar \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"sentence":"He go to school."}'

curl -X POST http://localhost:8080/api/ai/vocab \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"word":"abandon"}'
```

## 4. Achievement / Ranking / Report

```bash
curl http://localhost:8080/api/achievements -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/achievements/mine -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/ranking/weekly -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/ranking/all-time -H "Authorization: Bearer $TOKEN"
curl http://localhost:8080/api/reports/weekly -H "Authorization: Bearer $TOKEN"
```
