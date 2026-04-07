package com.isalnikov.msqwen.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Главный класс CLI модуля.
 *
 * <p>Реализует CommandLineRunner для обработки аргументов командной строки.
 * Активируется только при наличии CLI параметров или флага --help.
 * Поддерживает все команды: --help, --parse, --analyze, --cleanup, --stats, --version.</p>
 *
 * <p>Примеры запуска:</p>
 * <pre>
 * java -jar msqwen.jar --help
 * java -jar msqwen.jar --parse --user.id=1
 * java -jar msqwen.jar --analyze --prompt.id=1 --user.id=1
 * java -jar msqwen.jar --cleanup --target=news --user.id=1
 * java -jar msqwen.jar --stats --user.id=1
 * java -jar msqwen.jar --version
 * </pre>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
@ConditionalOnProperty(name = "cli.enabled", havingValue = "true", matchIfMissing = true)
public class CliRunner implements CommandLineRunner {

    /**
     * Логгер для записи событий CLI.
     */
    private static final Logger logger = LoggerFactory.getLogger(CliRunner.class);

    /**
     * Обработчик CLI команд.
     */
    private final CliCommandHandler commandHandler;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param commandHandler обработчик команд
     */
    public CliRunner(CliCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    /**
     * Точка входа CLI.
     *
     * <p>Разбирает аргументы и выполняет соответствующую команду.
     * Если аргументы не распознаны - запускает Spring Boot приложение в обычном режиме.</p>
     *
     * @param args аргументы командной строки
     */
    @Override
    public void run(String... args) throws Exception {
        CliArgumentParser parser = new CliArgumentParser(args);

        // Если нет аргументов - просто выходим (приложение запустится)
        if (args.length == 0) {
            logger.info("CLI параметры не указаны. Запуск в стандартном режиме...");
            return;
        }

        logger.info("CLI режим. Аргументы: {}", parser.getAll());

        // Определяем команду и выполняем
        if (parser.hasFlag("help")) {
            commandHandler.handleHelp();
        } else if (parser.hasFlag("version")) {
            commandHandler.handleVersion();
        } else if (parser.hasFlag("stats")) {
            Long userId = parser.getLong("user.id");
            commandHandler.handleStats(userId);
        } else if (parser.hasFlag("parse")) {
            Long userId = parser.getLong("user.id");
            commandHandler.handleParse(userId);
        } else if (parser.hasFlag("analyze")) {
            Long promptId = parser.getLong("prompt.id");
            Long userId = parser.getLong("user.id");
            commandHandler.handleAnalyze(promptId, userId);
        } else if (parser.hasFlag("cleanup")) {
            String target = parser.get("target");
            Long userId = parser.getLong("user.id");
            commandHandler.handleCleanup(target, userId);
        } else {
            logger.warn("Неизвестная команда. Используйте --help для справки");
            commandHandler.handleHelp();
        }
    }
}
