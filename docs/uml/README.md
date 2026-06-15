# UML Index

> Render with PlantUML: `plantuml -tpng docs/uml/*.puml`
> Or use VS Code "PlantUML" extension to preview each file.

| # | File | Type | Covers |
|---|---|---|---|
| 1 | `01_usecase.puml`            | Use Case  | Business use cases for Player + external systems |
| 2 | `02_domain.puml`             | Class     | Domain classes, attributes, operations, relations |
| 3 | `03_sequence_register.puml`  | Sequence  | UC-Register flow |
| 4 | `04_sequence_roll_answer.puml`| Sequence | UC-RollDice + UC-Answer (with event fan-out) |
| 5 | `05_sequence_ai_grammar.puml`| Sequence  | UC-AI Grammar with rate limit + cache + DeepSeek |
| 6 | `06_sequence_weekly_report.puml`| Sequence | UC-WeeklyReport with AI summary + fallback |
| 7 | `07_collaboration_ai_grammar.puml`| Collaboration | UC-AI Grammar object collaboration |
| 8 | `08_activity_answer.puml`    | Activity  | Internal flow of "answer + score + events" |
| 9 | `09_activity_ai.puml`        | Activity  | AI invocation pipeline (rate-limit / cache / timeout) |
| 10 | `10_state_game.puml`        | State     | Game lifecycle state machine |
| 11 | `11_state_user_level.puml`  | State     | User level transitions |
| 12 | `12_component.puml`         | Component | Web / Application / Domain / Infra layering |
| 13 | `13_deployment.puml`        | Deployment| Single-node deployment topology |
