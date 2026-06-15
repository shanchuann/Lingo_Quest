package com.lingoquest.infra.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lingoquest.common.exception.BizException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepSeekClient {

    @Value("${app.deepseek.api-key}")
    private String apiKey;

    @Value("${app.deepseek.base-url}")
    private String baseUrl;

    @Value("${app.deepseek.model}")
    private String model;

    @Value("${app.deepseek.timeout-ms}")
    private long timeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WebClient webClient;

    @PostConstruct
    void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("DEEPSEEK_API_KEY is empty - AI calls will fail until you set it in .env");
        }
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Send a single-shot chat completion request and return the assistant text.
     * Uses non-streaming mode for simplicity in the first iteration.
     */
    public String chat(String systemPrompt, String userPrompt, double temperature) {
        var body = Map.of(
                "model", model,
                "temperature", temperature,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user",   "content", userPrompt)
                )
        );
        try {
            var resp = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();
            if (resp == null) throw new BizException(50001, "Empty AI response");
            return resp.path("choices").path(0).path("message").path("content").asText("");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek call failed", e);
            throw new BizException(50001, "AI service unavailable, please retry");
        }
    }

    public <T> T chatAsJson(String systemPrompt, String userPrompt, double temperature, Class<T> clazz) {
        var raw = chat(systemPrompt, userPrompt, temperature);
        // Strip markdown code fences if model wraps JSON in ```json ... ```
        var clean = raw.trim()
                .replaceFirst("^```json\\s*", "")
                .replaceFirst("^```\\s*", "")
                .replaceFirst("\\s*```$", "")
                .trim();
        try {
            return objectMapper.readValue(clean, clazz);
        } catch (Exception e) {
            log.warn("AI did not return valid JSON: {}", raw);
            throw new BizException(50002, "AI returned malformed response");
        }
    }
}
