# xADL 2.0 description

File `lingo-quest.xml` describes the system at the **C&C (Component-and-Connector)** level using xADL-style XML.

Three component-and-connector groups are captured, which is the minimum mandated by the course rubric:

1. **WebClient ↔ ApiGateway** — connector: HTTPS/JSON REST (Bearer JWT)
2. **GameService ↔ AiTutorService** — connectors: Spring event bus (async) + JVM method call (sync)
3. **DeepSeekClient ↔ DeepSeekProvider** — connector: HTTPS REST against OpenAI-compatible API

For each component we document its interfaces and direction; for each connector its protocol, role, and quality attributes; and finally a `<configuration>` block binds component instances to their deployment locations.
