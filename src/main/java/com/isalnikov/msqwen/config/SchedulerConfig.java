package com.isalnikov.msqwen.config;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Конфигурация планировщика задач с использованием Virtual Threads.
 *
 * <p>Настраивает TaskScheduler для использования Virtual Threads (Java 25+)
 * для конкурентного выполнения запланированных задач. Это позволяет
 * эффективно выполнять IO-bound задачи (парсинг, HTTP запросы) без
 * блокировки потоков.</p>
 *
 * <p>Virtual Threads обеспечивают:</p>
 * <ul>
 *   <li>Легковесность - миллионы виртуальных потоков в одном JVM</li>
 *   <li>Эффективность при IO операциях (HTTP запросы к Telegram)</li>
 *   <li>Простоту кода - не нужна async/reactive модель</li>
 * </ul>
 *
 * @author isalnikov
 * @version 1.0
 */
@Configuration
public class SchedulerConfig implements SchedulingConfigurer {

    /**
     * Логгер для записи событий конфигурации.
     */
    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    /**
     * Настраивает TaskScheduler на использование Virtual Threads.
     *
     * @param taskRegistrar регистратор задач
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        logger.info("Настройка планировщика с Virtual Threads (Java 25+)");

        taskRegistrar.setScheduler(scheduledTaskExecutor());
    }

    /**
     * Создаёт Executor на базе Virtual Threads.
     *
     * <p>Использует Executors.newVirtualThreadPerTaskExecutor() который создаёт
     * новый виртуальный поток для каждой задачи. Это идеально подходит
     * для scheduled задач которые выполняют IO операции (парсинг каналов,
     * HTTP запросы к Qwen CLI).</p>
     *
     * @return Executor с Virtual Threads
     */
    @Bean
    public Executor scheduledTaskExecutor() {
        Executor executor = Executors.newVirtualThreadPerTaskExecutor();
        logger.info("Создан TaskScheduler с Virtual Threads");
        return executor;
    }
}
