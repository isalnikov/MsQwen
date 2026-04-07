package com.isalnikov.msqwen.config;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * Конфигурация планировщика задач с использованием Virtual Threads.
 *
 * <p>Настраивает TaskScheduler для использования Virtual Threads (Java 25+)
 * для конкурентного выполнения запланированных задач.</p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Configuration
@ConditionalOnProperty(name = "scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerConfig {

    /**
     * Логгер для записи событий конфигурации.
     */
    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    /**
     * Создаёт TaskScheduler на базе Virtual Threads.
     *
     * <p>Использует Executors.newSingleThreadScheduledExecutor() с Virtual Threads
     * для выполнения запланированных задач.</p>
     *
     * @return TaskScheduler с Virtual Threads
     */
    @Bean
    public TaskScheduler taskScheduler() {
        logger.info("Создание TaskScheduler с Virtual Threads (Java 25+)");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().factory()
        );
        return new ConcurrentTaskScheduler(scheduler);
    }
}
