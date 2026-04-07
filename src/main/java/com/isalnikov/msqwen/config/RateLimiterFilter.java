package com.isalnikov.msqwen.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Фильтр ограничения частоты запросов (Rate Limiter).
 *
 * <p>Защищает API от чрезмерного количества запросов с одного IP-адреса.
 * Использует алгоритм Token Bucket с фиксированным окном в 1 минуту.
 * При превышении лимита возвращает HTTP 429 Too Many Requests.</p>
 *
 * <p>Настройки через application.yml:</p>
 * <ul>
 *   <li>rate-limit.enabled - включение/выключение фильтра</li>
 *   <li>rate-limit.requests-per-minute - максимальное количество запросов в минуту</li>
 * </ul>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    /**
     * Логгер для записи событий фильтра.
     */
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterFilter.class);

    /**
     * Хранилище счётчиков запросов по IP-адресам.
     */
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    /**
     * Флаг включения rate limiting.
     */
    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    /**
     * Максимальное количество запросов в минуту.
     */
    @Value("${rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Пропускаем если rate limiting выключен
        if (!rateLimitEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        // Пропускаем health check endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/actuator") || requestURI.startsWith("/swagger-ui")
                || requestURI.startsWith("/v3/api-docs") || requestURI.startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = request.getRemoteAddr();
        RequestCounter counter = requestCounts.computeIfAbsent(clientIp,
                ip -> new RequestCounter());

        // Проверяем истекло ли окно
        long currentTime = System.currentTimeMillis();
        if (currentTime - counter.windowStart > 60000) {
            // Сбрасываем счётчик
            counter.reset(currentTime);
        }

        // Увеличиваем счётчик
        int currentRequests = counter.increment();

        // Добавляем заголовки с информацией о лимитах
        response.addHeader("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
        response.addHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, requestsPerMinute - currentRequests)));

        // Проверяем превышение лимита
        if (currentRequests > requestsPerMinute) {
            logger.warn("Превышение лимита запросов: ip={}, requests={}", clientIp, currentRequests);
            response.setStatus(429); // HTTP 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Превышен лимит запросов. Попробуйте позже.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Счётчик запросов для одного IP-адреса.
     */
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        int increment() {
            return count.incrementAndGet();
        }

        void reset(long newWindowStart) {
            count.set(0);
            this.windowStart = newWindowStart;
        }
    }
}
