package com.isalnikov.msqwen.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Сервис для взаимодействия с Qwen Code CLI.
 * 
 * <p>Вызывает Qwen CLI через ProcessBuilder передаёт промпт
 * и получает результат анализа. Поддерживает таймауты и обработку ошибок.</p>
 * 
 * @author isalnikov
 * @version 1.0
 */
@Service
public class QwenService {

    /**
     * Логгер для записи событий сервиса.
     */
    private static final Logger logger = LoggerFactory.getLogger(QwenService.class);

    /**
     * Путь к исполняемому файлу Qwen CLI.
     */
    @Value("${qwen.cli.path:qwen}")
    private String qwenCliPath;

    /**
     * Таймаут выполнения Qwen CLI в секундах.
     */
    @Value("${qwen.cli.timeout:300}")
    private int timeoutSeconds;

    /**
     * Отправляет промпт в Qwen CLI и возвращает результат анализа.
     *
     * @param prompt текст промпта с контекстом новостей
     * @return результат анализа от Qwen
     * @throws RuntimeException если вызов CLI завершился ошибкой
     */
    public String sendToQwen(String prompt) {
        logger.info("Отправка промпта в Qwen CLI (timeout={}s)", timeoutSeconds);
        
        try {
            // Формируем команду
            ProcessBuilder processBuilder = new ProcessBuilder(
                    qwenCliPath,
                    "--prompt",
                    prompt
            );
            
            processBuilder.redirectErrorStream(true);
            
            // Запускаем процесс
            Process process = processBuilder.start();
            
            // Читаем вывод
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Ждём завершения с таймаутом
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                logger.error("Таймаут выполнения Qwen CLI ({}s)", timeoutSeconds);
                throw new RuntimeException("Таймаут выполнения Qwen CLI");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                logger.error("Qwen CLI завершился с кодом ошибки: {}", exitCode);
                throw new RuntimeException("Qwen CLI завершился с кодом: " + exitCode);
            }
            
            String result = output.toString().trim();
            logger.info("Qwen CLI успешно вернул результат (длина={})", result.length());
            
            return result;
            
        } catch (IOException e) {
            logger.error("Ошибка ввода/вывода при вызове Qwen CLI", e);
            throw new RuntimeException("Ошибка при вызове Qwen CLI: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("Процесс Qwen CLI был прерван", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException("Процесс Qwen CLI был прерван", e);
        }
    }

    /**
     * Выполняет произвольную команду Qwen CLI.
     *
     * @param command команда для выполнения
     * @return вывод команды
     * @throws RuntimeException если команда завершилась ошибкой
     */
    public String executeCommand(String command) {
        logger.info("Выполнение команды Qwen CLI: {}", command);
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "/bin/bash", "-c",
                    qwenCliPath + " " + command
            );
            
            processBuilder.redirectErrorStream(true);
            
            Process process = processBuilder.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("Таймаут выполнения команды Qwen CLI");
            }
            
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                throw new RuntimeException("Команда Qwen CLI завершилась с кодом: " + exitCode);
            }
            
            return output.toString().trim();
            
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при выполнении команды Qwen CLI", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Процесс был прерван", e);
        }
    }
}
