package com.isalnikov.msqwen.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Конфигурация веб-слоя Spring MVC.
 *
 * <p>Настраивает CORS для фронтенд-приложений и дополнительные
 * параметры веб-слоя. CORS включен только для указанных origin
 * чтобы обеспечить безопасность API.</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Логгер для записи событий конфигурации.
     */
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    /**
     * Разрешённые CORS origin из конфигурации.
     */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:8080}")
    private List<String> allowedOrigins;

    /**
     * Разрешённые HTTP методы для CORS.
     */
    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private List<String> allowedMethods;

    /**
     * Разрешённые заголовки для CORS.
     */
    @Value("${cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    /**
     * Создаёт CORS фильтр для защиты API.
     *
     * <p>Настраивает разрешённые origin методы и заголовки
     * на основе параметров из application.yml.</p>
     *
     * @return CORS фильтр
     */
    @Bean
    public CorsFilter corsFilter() {
        logger.info("Настройка CORS: origins={}, methods={}, headers={}",
                allowedOrigins, allowedMethods, allowedHeaders);

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(allowedMethods);
        config.setAllowedHeaders(allowedHeaders);
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        logger.info("CORS фильтр настроен для /api/** endpoints");
        return new CorsFilter(source);
    }
}
