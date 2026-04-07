package com.isalnikov.msqwen.cli;

import java.util.HashMap;
import java.util.Map;

/**
 * Парсер аргументов командной строки для CLI модуля.
 *
 * <p>Разбирает аргументы формата --key=value и --flag
 * в структуру Map для удобной обработки CLI командами.</p>
 *
 * <p>Поддерживаемые форматы:</p>
 * <ul>
 *   <li>--key=value (например --user.id=1)</li>
 *   <li>--flag (например --help)</li>
 * </ul>
 *
 * @author isalnikov
 * @version 1.0
 */
public class CliArgumentParser {

    /**
     * Карта разобранных аргументов (ключ -> значение).
     */
    private final Map<String, String> arguments = new HashMap<>();

    /**
     * Создаёт парсер и разбирает переданные аргументы.
     *
     * @param args аргументы командной строки
     */
    public CliArgumentParser(String[] args) {
        parse(args);
    }

    /**
     * Разбирает массив аргументов в карту.
     *
     * @param args аргументы командной строки
     */
    private void parse(String[] args) {
        if (args == null) {
            return;
        }

        for (String arg : args) {
            if (arg.startsWith("--")) {
                String key = arg.substring(2);
                if (key.contains("=")) {
                    String[] parts = key.split("=", 2);
                    arguments.put(parts[0], parts[1]);
                } else {
                    arguments.put(key, "true");
                }
            }
        }
    }

    /**
     * Получает значение аргумента по ключу.
     *
     * @param key ключ аргумента
     * @return значение или null если не найден
     */
    public String get(String key) {
        return arguments.get(key);
    }

    /**
     * Получает значение аргумента или значение по умолчанию.
     *
     * @param key ключ аргумента
     * @param defaultValue значение по умолчанию
     * @return значение аргумента или default
     */
    public String getOrDefault(String key, String defaultValue) {
        return arguments.getOrDefault(key, defaultValue);
    }

    /**
     * Проверяет наличие флага.
     *
     * @param flag имя флага
     * @return true если флаг установлен
     */
    public boolean hasFlag(String flag) {
        return "true".equals(arguments.get(flag));
    }

    /**
     * Получает Long значение аргумента.
     *
     * @param key ключ аргумента
     * @return Long значение или null
     */
    public Long getLong(String key) {
        String value = get(key);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Проверяет наличие указанного аргумента.
     *
     * @param key ключ аргумента
     * @return true если аргумент присутствует
     */
    public boolean has(String key) {
        return arguments.containsKey(key);
    }

    /**
     * Возвращает все разобранные аргументы.
     *
     * @return карта аргументов
     */
    public Map<String, String> getAll() {
        return Map.copyOf(arguments);
    }

    /**
     * Возвращает строковое представление всех аргументов.
     *
     * @return строка с аргументами
     */
    @Override
    public String toString() {
        return arguments.toString();
    }
}
