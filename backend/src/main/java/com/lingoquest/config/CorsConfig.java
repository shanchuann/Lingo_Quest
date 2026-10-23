package com.lingoquest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

/**
 * CORS for direct browser calls.
 *
 * <p>In production the browser talks to Vercel, and Vercel's `/api` rewrite
 * proxies to this service. The proxy forwards the browser's `Origin` header
 * (e.g. `https://xxx.vercel.app`), so the backend must accept it — otherwise
 * Spring returns 403 "Invalid CORS request" for POSTs.</p>
 *
 * <p>Override the list with `CORS_ALLOWED_ORIGINS` (comma separated patterns),
 * e.g. `https://*.vercel.app,https://lingo.example.com`.</p>
 */
@Configuration
public class CorsConfig {

    private static final String DEFAULT_PATTERNS =
            "http://localhost:*,http://127.0.0.1:*,https://*.vercel.app";

    @Value("${app.cors.allowed-origin-patterns:" + DEFAULT_PATTERNS + "}")
    private String allowedOriginPatterns;

    @Bean
    public CorsFilter corsFilter() {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(patterns());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    private List<String> patterns() {
        return Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
