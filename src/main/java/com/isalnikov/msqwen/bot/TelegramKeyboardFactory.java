package com.isalnikov.msqwen.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Фабрика клавиатур для Telegram бота.
 *
 * <p>Создаёт стандартные и inline клавиатуры для интерактивного взаимодействия
 * с пользователем. Все клавиатуры типизированы и используют callback данные
 * для обработки нажатий.</p>
 *
 * <p>Префиксы callback данных:</p>
 * <ul>
 *   <li>prompt: - операции с промптами</li>
 *   <li>channel: - операции с каналами</li>
 *   <li>confirm: - подтверждения действий</li>
 *   <li>nav: - навигация (пагинация)</li>
 * </ul>
 *
 * @author isalnikov
 * @version 1.0
 */
public final class TelegramKeyboardFactory {

    /** Приватный конструктор */
    private TelegramKeyboardFactory() {
    }

    /**
     * Создаёт главное меню бота.
     *
     * @return inline клавиатура главного меню
     */
    public static InlineKeyboardMarkup createMainMenu() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // Первый ряд: Промпты и Каналы
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("📝 Мои промпты", "menu:prompts"));
        row1.add(createButton("📡 Каналы", "menu:channels"));
        rows.add(row1);

        // Второй ряд: Парсинг и Анализ
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("⬇️ Парсинг", "menu:parse"));
        row2.add(createButton("🔍 Анализ", "menu:analyze"));
        rows.add(row2);

        // Третий ряд: Новости и Статистика
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("📰 Новости", "menu:news"));
        row3.add(createButton("📊 Статистика", "menu:stats"));
        rows.add(row3);

        return createMarkup(rows);
    }

    /**
     * Создаёт клавиатуру выбора промпта.
     *
     * @param prompts список промптов
     * @param action действие (select, delete, analyze)
     * @return inline клавиатура с промптами
     */
    public static InlineKeyboardMarkup createPromptSelection(List<PromptItem> prompts, String action) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (PromptItem prompt : prompts) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(prompt.name(), action + ":prompt:" + prompt.id()));
            rows.add(row);
        }

        // Кнопка отмены
        rows.add(List.of(createButton("❌ Отмена", "cancel")));

        return createMarkup(rows);
    }

    /**
     * Создаёт клавиатуру выбора канала.
     *
     * @param channels список каналов
     * @param action действие (select, delete)
     * @return inline клавиатура с каналами
     */
    public static InlineKeyboardMarkup createChannelSelection(List<ChannelItem> channels, String action) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (ChannelItem channel : channels) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(createButton(channel.name(), action + ":channel:" + channel.id()));
            rows.add(row);
        }

        // Кнопка отмены
        rows.add(List.of(createButton("❌ Отмена", "cancel")));

        return createMarkup(rows);
    }

    /**
     * Создаёт клавиатуру подтверждения удаления.
     *
     * @param entityType тип сущности (промпт, канал, новость)
     * @param entityId ID сущности
     * @return inline клавиатура подтверждения
     */
    public static InlineKeyboardMarkup createConfirmation(String entityType, Long entityId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("✅ Да, удалить", "confirm:delete:" + entityType + ":" + entityId));
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("❌ Нет, отменить", "cancel"));
        rows.add(row2);

        return createMarkup(rows);
    }

    /**
     * Создаёт клавиатуру пагинации.
     *
     * @param currentPage текущая страница
     * @param totalPages всего страниц
     * @param context контекст для callback (news, analysis)
     * @return inline клавиатура навигации
     */
    public static InlineKeyboardMarkup createPagination(int currentPage, int totalPages, String context) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();

        if (currentPage > 0) {
            row.add(createButton("⬅️ Назад", "nav:" + context + ":prev:" + currentPage));
        }

        row.add(createButton("Страница " + (currentPage + 1) + "/" + totalPages, "noop"));

        if (currentPage < totalPages - 1) {
            row.add(createButton("Вперёд ➡️", "nav:" + context + ":next:" + currentPage));
        }

        rows.add(row);
        return createMarkup(rows);
    }

    /**
     * Создаёт inline кнопку.
     *
     * @param text текст кнопки
     * @param callbackData callback данные
     * @return кнопка
     */
    public static InlineKeyboardButton createButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    /**
     * Создаёт markup из строк кнопок.
     *
     * @param rows строки кнопок
     * @return inline keyboard markup
     */
    public static InlineKeyboardMarkup createMarkup(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    /**
     * Элемент промпта для клавиатуры.
     */
    public record PromptItem(Long id, String name) {
    }

    /**
     * Элемент канала для клавиатуры.
     */
    public record ChannelItem(Long id, String name) {
    }
}
