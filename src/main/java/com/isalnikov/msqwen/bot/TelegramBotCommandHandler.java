package com.isalnikov.msqwen.bot;

import com.isalnikov.msqwen.dto.*;
import com.isalnikov.msqwen.entity.User;
import com.isalnikov.msqwen.parser.NewsParserService;
import com.isalnikov.msqwen.service.AnalysisService;
import com.isalnikov.msqwen.service.ChannelService;
import com.isalnikov.msqwen.service.NewsService;
import com.isalnikov.msqwen.service.PromptService;
import com.isalnikov.msqwen.service.UserService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Обработчик команд Telegram бота.
 *
 * <p>Обрабатывает все команды и текстовые сообщения от пользователей.
 * Реализует state management для многошаговых диалогов добавления промптов и каналов.
 * Все операции изолированы по user_id.</p>
 *
 * <p>Поддерживаемые команды:
 * /start - Приветствие и главное меню
 * /help - Справка по командам
 * /status - Статистика системы
 * /prompts - Список промптов
 * /channels - Список каналов
 * /parse - Запустить парсинг
 * /analyze - Запустить анализ
 * /news - Получить новости/результаты анализа
 * /addprompt - Добавить промпт (многошаговый диалог)
 * /deleteprompt - Удалить промпт
 * /addchannel - Добавить канал (многошаговый диалог)
 * /deletechannel - Удалить канал
 * </p>
 *
 * @author isalnikov
 * @version 1.0
 */
@Component
public class TelegramBotCommandHandler {

    /**
     * Логгер для записи событий обработчика.
     */
    private static final Logger logger = LoggerFactory.getLogger(TelegramBotCommandHandler.class);

    /**
     * Сервис для управления пользователями.
     */
    private final UserService userService;

    /**
     * Сервис для управления промптами.
     */
    private final PromptService promptService;

    /**
     * Сервис для управления каналами.
     */
    private final ChannelService channelService;

    /**
     * Сервис для управления новостями.
     */
    private final NewsService newsService;

    /**
     * Сервис для анализа новостей.
     */
    private final AnalysisService analysisService;

    /**
     * Сервис для парсинга новостей.
     */
    private final NewsParserService newsParserService;

    /**
     * Хранение состояний диалогов (userId -> DialogContext).
     */
    private final Map<Long, DialogContext> dialogContexts = new HashMap<>();

    /**
     * Таймаут диалога в минутах.
     */
    @Value("${telegram.bot.dialog-timeout:5}")
    private int dialogTimeoutMinutes;

    /**
     * Конструктор с внедрением зависимостей через конструктор.
     *
     * @param userService сервис пользователей
     * @param promptService сервис промптов
     * @param channelService сервис каналов
     * @param newsService сервис новостей
     * @param analysisService сервис анализа
     * @param newsParserService сервис парсинга
     */
    public TelegramBotCommandHandler(UserService userService,
                                     PromptService promptService,
                                     ChannelService channelService,
                                     NewsService newsService,
                                     AnalysisService analysisService,
                                     NewsParserService newsParserService) {
        this.userService = userService;
        this.promptService = promptService;
        this.channelService = channelService;
        this.newsService = newsService;
        this.analysisService = analysisService;
        this.newsParserService = newsParserService;
    }

    /**
     * Обрабатывает текстовое сообщение.
     *
     * @param message сообщение от Telegram
     */
    public void handleMessage(Message message) {
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();
        String text = message.hasText() ? message.getText() : "";

        logger.debug("Обработка сообщения от пользователя: userId={}, text={}", userId, text);

        // Проверяем активный диалог
        DialogContext context = dialogContexts.get(userId);
        if (context != null && !context.isExpired(dialogTimeoutMinutes)) {
            handleDialogMessage(chatId, userId, text, context);
            return;
        } else if (context != null && context.isExpired(dialogTimeoutMinutes)) {
            // Удаляем просроченный диалог
            dialogContexts.remove(userId);
            sendTextMessage(chatId, "⏰ Диалог истёк по таймауту. Начните заново.");
        }

        // Обрабатываем команды
        if (text == null || !text.startsWith("/")) {
            sendTextMessage(chatId, "Используйте /help для списка команд");
            return;
        }

        String command = text.split(" ")[0].toLowerCase();

        switch (command) {
            case "/start" -> handleStart(chatId, userId);
            case "/help" -> handleHelp(chatId);
            case "/status" -> handleStatus(chatId, userId);
            case "/prompts" -> handlePrompts(chatId, userId);
            case "/channels" -> handleChannels(chatId, userId);
            case "/parse" -> handleParse(chatId, userId);
            case "/analyze" -> handleAnalyze(chatId, userId);
            case "/news" -> handleNews(chatId, userId);
            case "/version" -> handleVersion(chatId);
            case "/addprompt" -> handleAddPrompt(chatId, userId);
            case "/deleteprompt" -> handleDeletePrompt(chatId, userId);
            case "/addchannel" -> handleAddChannel(chatId, userId);
            case "/deletechannel" -> handleDeleteChannel(chatId, userId);
            case "/cancel" -> handleCancel(chatId, userId);
            default -> sendTextMessage(chatId, "❓ Неизвестная команда. Используйте /help");
        }
    }

    /**
     * Обрабатывает callback от inline клавиатуры.
     *
     * @param callback callback запрос
     */
    public void handleCallback(CallbackQuery callback) {
        Long chatId = callback.getMessage().getChatId();
        Long userId = callback.getFrom().getId();
        String data = callback.getData();

        logger.debug("Обработка callback: userId={}, data={}", userId, data);

        // Разбираем callback данные: action:entity:id
        String[] parts = data.split(":");
        if (parts.length < 2) {
            sendTextMessage(chatId, "❌ Некорректный callback");
            return;
        }

        String action = parts[0];
        String entity = parts[1];

        switch (action) {
            case "menu" -> handleMenuAction(chatId, userId, entity);
            case "confirm" -> handleConfirmation(chatId, userId, parts);
            case "nav" -> handleNavigation(chatId, userId, parts);
            case "cancel" -> handleCancel(chatId, userId);
            default -> sendTextMessage(chatId, "❓ Неизвестное действие");
        }
    }

    /**
     * Команда /start - Приветствие и регистрация.
     */
    private void handleStart(Long chatId, Long telegramId) {
        logger.info("Команда /start: userId={}", telegramId);

        // Регистрируем пользователя если новый
        try {
            userService.registerUser(telegramId, null, null, null);
        } catch (Exception e) {
            logger.warn("Пользователь уже зарегистрирован: {}", telegramId);
        }

        String welcome = """
                👋 Добро пожаловать в **MsQwen**!

                Я помогу вам парсить и анализировать новости из Telegram каналов через ИИ.

                **Основные команды:**
                /prompts - Управление промптами
                /channels - Управление каналами
                /parse - Парсить новости
                /analyze - Анализировать новости
                /news - Получить результаты
                /status - Статистика
                /help - Справка

                Начните с создания промпта!
                """;

        sendTextMessageWithKeyboard(chatId, welcome, TelegramKeyboardFactory.createMainMenu());
    }

    /**
     * Команда /help - Справка по командам.
     */
    private void handleHelp(Long chatId) {
        String help = """
                📖 **Справка по командам MsQwen**

                **Базовые команды:**
                /start - Запустить бота
                /help - Эта справка
                /version - Информация о сборке
                /status - Статистика

                **Промпты:**
                /prompts - Список промптов
                /addprompt - Добавить промпт
                /deleteprompt - Удалить промпт

                **Каналы:**
                /channels - Список каналов
                /addchannel - Добавить канал
                /deletechannel - Удалить канал

                **Новости и анализ:**
                /parse - Парсить новости
                /analyze - Анализировать
                /news - Результаты анализа

                **Отмена:**
                /cancel - Отменить диалог
                """;

        sendTextMessage(chatId, help);
    }

    /**
     * Команда /status - Статистика системы.
     */
    private void handleStatus(Long chatId, Long telegramId) {
        logger.info("Команда /status: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            long promptsCount = promptService.getUserPrompts(user.id()).size();
            long channelsCount = channelService.getAllUserChannels(user.id()).size();
            long newsCount = newsService.getNewsCount(user.id());
            long analyzedCount = newsService.getAnalyzedNewsCount(user.id());

            String status = """
                    📊 **Статистика**

                    Промптов: %d
                    Каналов: %d
                    Новостей: %d
                    Проанализировано: %d
                    """.formatted(promptsCount, channelsCount, newsCount, analyzedCount);

            sendTextMessage(chatId, status);
        } catch (Exception e) {
            logger.error("Ошибка при получении статистики", e);
            sendTextMessage(chatId, "❌ Ошибка при получении статистики");
        }
    }

    /**
     * Команда /prompts - Список промптов.
     */
    private void handlePrompts(Long chatId, Long telegramId) {
        logger.info("Команда /prompts: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<PromptDTO> prompts = promptService.getUserPrompts(user.id());

            if (prompts.isEmpty()) {
                sendTextMessage(chatId, "📝 У вас пока нет промптов. Используйте /addprompt");
                return;
            }

            StringBuilder sb = new StringBuilder("📝 **Ваши промпты:**\n\n");
            for (PromptDTO prompt : prompts) {
                sb.append("• **%s** (ID: %d)\n".formatted(prompt.name(), prompt.id()));
                if (prompt.description() != null) {
                    sb.append("  _%s_\n".formatted(prompt.description()));
                }
            }
            sb.append("\nИспользуйте /deleteprompt для удаления");

            sendTextMessage(chatId, sb.toString());
        } catch (Exception e) {
            logger.error("Ошибка при получении промптов", e);
            sendTextMessage(chatId, "❌ Ошибка при получении промптов");
        }
    }

    /**
     * Команда /channels - Список каналов.
     */
    private void handleChannels(Long chatId, Long telegramId) {
        logger.info("Команда /channels: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<ChannelDTO> channels = channelService.getAllUserChannels(user.id());

            if (channels.isEmpty()) {
                sendTextMessage(chatId, "📡 У вас пока нет каналов. Используйте /addchannel");
                return;
            }

            StringBuilder sb = new StringBuilder("📡 **Ваши каналы:**\n\n");
            for (ChannelDTO channel : channels) {
                sb.append("• **%s** (%s)\n".formatted(channel.name(), channel.telegramHandle()));
                if (channel.description() != null) {
                    sb.append("  _%s_\n".formatted(channel.description()));
                }
            }
            sb.append("\nИспользуйте /deletechannel для удаления");

            sendTextMessage(chatId, sb.toString());
        } catch (Exception e) {
            logger.error("Ошибка при получении каналов", e);
            sendTextMessage(chatId, "❌ Ошибка при получении каналов");
        }
    }

    /**
     * Команда /parse - Запустить парсинг.
     */
    private void handleParse(Long chatId, Long telegramId) {
        logger.info("Команда /parse: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<ChannelDTO> channels = channelService.getAllUserChannels(user.id());

            if (channels.isEmpty()) {
                sendTextMessage(chatId, "❌ Нет каналов для парсинга. Добавьте каналы командой /addchannel");
                return;
            }

            sendTextMessage(chatId, "⏳ Запускаю парсинг %d каналов...".formatted(channels.size()));

            // Запускаем парсинг (синхронно для простоты)
            var channelEntities = channels.stream()
                    .map(dto -> {
                        var channel = new com.isalnikov.msqwen.entity.Channel();
                        channel.setId(dto.id());
                        channel.setTelegramHandle(dto.telegramHandle());
                        channel.setIsActive(dto.isActive());
                        return channel;
                    })
                    .toList();

            int newsCount = newsParserService.parseAllChannels(user.id(), channelEntities);

            sendTextMessage(chatId, "✅ Парсинг завершён!\n📰 Добавлено новостей: %d".formatted(newsCount));
        } catch (Exception e) {
            logger.error("Ошибка при парсинге", e);
            sendTextMessage(chatId, "❌ Ошибка при парсинге: " + e.getMessage());
        }
    }

    /**
     * Команда /analyze - Запустить анализ.
     */
    private void handleAnalyze(Long chatId, Long telegramId) {
        logger.info("Команда /analyze: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<PromptDTO> prompts = promptService.getUserPrompts(user.id());

            if (prompts.isEmpty()) {
                sendTextMessage(chatId, "❌ Нет промптов для анализа. Создайте промпт командой /addprompt");
                return;
            }

            // Для простоты анализируем первый промпт
            PromptDTO firstPrompt = prompts.get(0);
            List<NewsDTO> unanalyzedNews = newsService.getUnanalyzedNews(firstPrompt.id(), user.id());

            if (unanalyzedNews.isEmpty()) {
                sendTextMessage(chatId, "✅ Нет новостей для анализа. Запустите /parse");
                return;
            }

            sendTextMessage(chatId, "⏳ Запускаю анализ %d новостей...\n📝 Промпт: %s".formatted(
                    unanalyzedNews.size(), firstPrompt.name()));

            AnalysisResultDTO result = analysisService.analyzeNews(firstPrompt.id(), user.id(), unanalyzedNews);

            String analysisText = result.analysisText();
            if (analysisText != null && analysisText.length() > 4000) {
                analysisText = analysisText.substring(0, 4000) + "\n\n... (обрезано)";
            }

            sendTextMessage(chatId, "✅ Анализ завершён!\n\n📝 Результат:\n" + analysisText);
        } catch (Exception e) {
            logger.error("Ошибка при анализе", e);
            sendTextMessage(chatId, "❌ Ошибка при анализе: " + e.getMessage());
        }
    }

    /**
     * Команда /news - Получить новости/результаты анализа.
     */
    private void handleNews(Long chatId, Long telegramId) {
        logger.info("Команда /news: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<PromptDTO> prompts = promptService.getUserPrompts(user.id());

            if (prompts.isEmpty()) {
                sendTextMessage(chatId, "📰 Нет новостей. Создайте промпт (/addprompt) и запустите парсинг (/parse)");
                return;
            }

            // Для простоты показываем новости первого промпта
            PromptDTO firstPrompt = prompts.get(0);
            List<NewsDTO> news = newsService.getNewsByPrompt(firstPrompt.id(), user.id());

            if (news.isEmpty()) {
                sendTextMessage(chatId, "📰 Новостей пока нет. Запустите /parse");
                return;
            }

            StringBuilder sb = new StringBuilder("📰 **Последние новости** (%d):\n\n".formatted(news.size()));
            int count = Math.min(5, news.size());
            for (int i = 0; i < count; i++) {
                NewsDTO n = news.get(i);
                sb.append("**%d. %s**\n".formatted(i + 1, n.title() != null ? n.title() : "Без заголовка"));
                sb.append("👁 %d | 🔄 %d | ❤️ %d\n".formatted(
                        n.viewsCount(), n.forwardsCount(), n.likesCount()));
                sb.append("📊 Engagement: %.2f\n\n".formatted(n.engagementScore()));
            }

            sendTextMessage(chatId, sb.toString());
        } catch (Exception e) {
            logger.error("Ошибка при получении новостей", e);
            sendTextMessage(chatId, "❌ Ошибка при получении новостей");
        }
    }

    /**
     * Команда /version - Информация о сборке.
     */
    private void handleVersion(Long chatId) {
        String version = """
                🔧 **MsQwen v1.0-SNAPSHOT**

                Сборка: 1.0
                Дата: 2026-04-07
                Java: 25
                Spring Boot: 3.2.0
                """;

        sendTextMessage(chatId, version);
    }

    /**
     * Команда /addprompt - Начать диалог добавления промпта.
     */
    private void handleAddPrompt(Long chatId, Long telegramId) {
        logger.info("Команда /addprompt: userId={}", telegramId);

        DialogContext context = new DialogContext(DialogState.WAITING_PROMPT_NAME, telegramId);
        dialogContexts.put(telegramId, context);

        sendTextMessage(chatId, """
                📝 **Добавление нового промпта**

                Введите название промпта:
                (например: "Анализ политики", "Экономика")

                /cancel - отменить
                """);
    }

    /**
     * Команда /deleteprompt - Показать промпты для удаления.
     */
    private void handleDeletePrompt(Long chatId, Long telegramId) {
        logger.info("Команда /deleteprompt: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<PromptDTO> prompts = promptService.getUserPrompts(user.id());

            if (prompts.isEmpty()) {
                sendTextMessage(chatId, "📝 У вас нет промптов для удаления");
                return;
            }

            var promptItems = prompts.stream()
                    .map(p -> new TelegramKeyboardFactory.PromptItem(p.id(), p.name()))
                    .toList();

            sendTextMessageWithKeyboard(chatId,
                    "Выберите промпт для удаления:",
                    TelegramKeyboardFactory.createPromptSelection(promptItems, "delete"));
        } catch (Exception e) {
            logger.error("Ошибка при получении промптов", e);
            sendTextMessage(chatId, "❌ Ошибка при получении промптов");
        }
    }

    /**
     * Команда /addchannel - Начать диалог добавления канала.
     */
    private void handleAddChannel(Long chatId, Long telegramId) {
        logger.info("Команда /addchannel: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<PromptDTO> prompts = promptService.getUserPrompts(user.id());

            if (prompts.isEmpty()) {
                sendTextMessage(chatId, "❌ Сначала создайте промпт командой /addprompt");
                return;
            }

            // Для простоты используем первый промпт
            DialogContext context = new DialogContext(DialogState.WAITING_CHANNEL_NAME, telegramId);
            context.setPromptId(prompts.get(0).id());
            dialogContexts.put(telegramId, context);

            sendTextMessage(chatId, """
                    📡 **Добавление нового канала**
                    Промпт: %s

                    Введите название канала:
                    (например: "PolitNews", "TechCrunch")

                    /cancel - отменить
                    """.formatted(prompts.get(0).name()));
        } catch (Exception e) {
            logger.error("Ошибка при добавлении канала", e);
            sendTextMessage(chatId, "❌ Ошибка при добавлении канала");
        }
    }

    /**
     * Команда /deletechannel - Показать каналы для удаления.
     */
    private void handleDeleteChannel(Long chatId, Long telegramId) {
        logger.info("Команда /deletechannel: userId={}", telegramId);

        try {
            var user = userService.getUserByTelegramId(telegramId);
            List<ChannelDTO> channels = channelService.getAllUserChannels(user.id());

            if (channels.isEmpty()) {
                sendTextMessage(chatId, "📡 У вас нет каналов для удаления");
                return;
            }

            var channelItems = channels.stream()
                    .map(c -> new TelegramKeyboardFactory.ChannelItem(c.id(), c.name()))
                    .toList();

            sendTextMessageWithKeyboard(chatId,
                    "Выберите канал для удаления:\n⚠️ Все новости канала будут удалены!",
                    TelegramKeyboardFactory.createChannelSelection(channelItems, "delete"));
        } catch (Exception e) {
            logger.error("Ошибка при получении каналов", e);
            sendTextMessage(chatId, "❌ Ошибка при получении каналов");
        }
    }

    /**
     * Команда /cancel - Отменить диалог.
     */
    private void handleCancel(Long chatId, Long telegramId) {
        dialogContexts.remove(telegramId);
        sendTextMessage(chatId, "❌ Диалог отменён.");
    }

    /**
     * Обрабатывает сообщения в рамках диалога.
     */
    private void handleDialogMessage(Long chatId, Long userId, String text, DialogContext context) {
        logger.debug("Обработка сообщения в диалоге: userId={}, state={}", userId, context.state());

        switch (context.state()) {
            case WAITING_PROMPT_NAME -> handlePromptNameInput(chatId, userId, text, context);
            case WAITING_PROMPT_DESCRIPTION -> handlePromptDescriptionInput(chatId, userId, text, context);
            case WAITING_PROMPT_TEXT -> handlePromptTextInput(chatId, userId, text, context);
            case WAITING_CHANNEL_NAME -> handleChannelNameInput(chatId, userId, text, context);
            case WAITING_CHANNEL_HANDLE -> handleChannelHandleInput(chatId, userId, text, context);
            case WAITING_CHANNEL_DESCRIPTION -> handleChannelDescriptionInput(chatId, userId, text, context);
            default -> sendTextMessage(chatId, "❌ Неизвестное состояние диалога");
        }
    }

    /**
     * Обрабатывает ввод названия промпта.
     */
    private void handlePromptNameInput(Long chatId, Long userId, String text, DialogContext context) {
        context.setPromptName(text);
        context.setState(DialogState.WAITING_PROMPT_DESCRIPTION);

        sendTextMessage(chatId, """
                ✅ Название сохранено: **%s**

                Введите описание промпта (или "-"):
                """.formatted(text));
    }

    /**
     * Обрабатывает ввод описания промпта.
     */
    private void handlePromptDescriptionInput(Long chatId, Long userId, String text, DialogContext context) {
        context.setPromptDescription("-".equals(text) ? null : text);
        context.setState(DialogState.WAITING_PROMPT_TEXT);

        sendTextMessage(chatId, """
                ✅ Описание сохранено

                Введите текст промпта для анализа новостей:
                (этот текст будет использоваться ИИ для анализа)
                """);
    }

    /**
     * Обрабатывает ввод текста промпта и сохраняет промпт.
     */
    private void handlePromptTextInput(Long chatId, Long userId, String text, DialogContext context) {
        try {
            PromptDTO created = promptService.createPrompt(
                    userId,
                    context.getPromptName(),
                    context.getPromptDescription(),
                    text
            );

            dialogContexts.remove(userId);

            sendTextMessage(chatId, """
                    ✅ **Промпт создан!**

                    Название: %s
                    ID: %d

                    Теперь добавьте каналы командой /addchannel
                    """.formatted(created.name(), created.id()));
        } catch (Exception e) {
            logger.error("Ошибка при создании промпта", e);
            sendTextMessage(chatId, "❌ Ошибка при создании промпта: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает ввод названия канала.
     */
    private void handleChannelNameInput(Long chatId, Long userId, String text, DialogContext context) {
        context.setChannelName(text);
        context.setState(DialogState.WAITING_CHANNEL_HANDLE);

        sendTextMessage(chatId, """
                ✅ Название сохранено: **%s**

                Введите handle канала (например: @durov или durev):
                """.formatted(text));
    }

    /**
     * Обрабатывает ввод handle канала.
     */
    private void handleChannelHandleInput(Long chatId, Long userId, String text, DialogContext context) {
        String handle = text.replace("@", "").trim();
        context.setChannelHandle(handle);
        context.setChannelUrl("https://t.me/" + handle);
        context.setState(DialogState.WAITING_CHANNEL_DESCRIPTION);

        sendTextMessage(chatId, """
                ✅ Handle сохранено: @%s

                Введите описание канала (или "-"):
                """.formatted(handle));
    }

    /**
     * Обрабатывает ввод описания канала и сохраняет канал.
     */
    private void handleChannelDescriptionInput(Long chatId, Long userId, String text, DialogContext context) {
        try {
            ChannelDTO created = channelService.createChannel(
                    context.getPromptId(),
                    userId,
                    context.getChannelName(),
                    context.getChannelHandle(),
                    context.getChannelUrl(),
                    "-".equals(text) ? null : text
            );

            dialogContexts.remove(userId);

            sendTextMessage(chatId, """
                    ✅ **Канал добавлен!**

                    Название: %s
                    Handle: @%s
                    ID: %d

                    Теперь запустите парсинг командой /parse
                    """.formatted(created.name(), created.telegramHandle(), created.id()));
        } catch (Exception e) {
            logger.error("Ошибка при создании канала", e);
            sendTextMessage(chatId, "❌ Ошибка при создании канала: " + e.getMessage());
        }
    }

    /**
     * Обработчик действий главного меню.
     */
    private void handleMenuAction(Long chatId, Long userId, String action) {
        switch (action) {
            case "prompts" -> handlePrompts(chatId, userId);
            case "channels" -> handleChannels(chatId, userId);
            case "parse" -> handleParse(chatId, userId);
            case "analyze" -> handleAnalyze(chatId, userId);
            case "news" -> handleNews(chatId, userId);
            case "stats" -> handleStatus(chatId, userId);
            default -> sendTextMessage(chatId, "❓ Неизвестное действие");
        }
    }

    /**
     * Обработчик подтверждений.
     */
    private void handleConfirmation(Long chatId, Long userId, String[] parts) {
        if (parts.length < 4) {
            sendTextMessage(chatId, "❌ Некорректное подтверждение");
            return;
        }

        String action = parts[1]; // delete
        String entityType = parts[2]; // prompt, channel
        String entityIdStr = parts[3];

        if (!"delete".equals(action)) {
            return;
        }

        try {
            Long entityId = Long.parseLong(entityIdStr);

            switch (entityType) {
                case "prompt" -> {
                    promptService.deletePrompt(entityId, userId);
                    sendTextMessage(chatId, "✅ Промпт удалён вместе со всеми каналами и новостями");
                }
                case "channel" -> {
                    channelService.deleteChannel(entityId, userId);
                    sendTextMessage(chatId, "✅ Канал удалён вместе со всеми новостями");
                }
                default -> sendTextMessage(chatId, "❌ Неизвестный тип сущности");
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении: entityType={}, id={}", entityType, entityIdStr, e);
            sendTextMessage(chatId, "❌ Ошибка при удалении: " + e.getMessage());
        }
    }

    /**
     * Обработчик навигации (пагинация).
     */
    private void handleNavigation(Long chatId, Long userId, String[] parts) {
        // TODO: Реализовать пагинацию новостей
        sendTextMessage(chatId, "📰 Пагинация в разработке");
    }

    /**
     * Отправляет текстовое сообщение.
     */
    private void sendTextMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            message.setParseMode("Markdown");

            // TODO: Использовать настоящий TelegramBot для отправки
            logger.info("Отправка сообщения: chatId={}, text={}", chatId, text);
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения", e);
        }
    }

    /**
     * Отправляет сообщение с inline клавиатурой.
     */
    private void sendTextMessageWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            message.setParseMode("Markdown");
            message.setReplyMarkup(keyboard);

            // TODO: Использовать настоящий TelegramBot для отправки
            logger.info("Отправка сообщения с клавиатурой: chatId={}, text={}", chatId, text);
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения с клавиатурой", e);
        }
    }

    /**
     * Состояние диалога для многошаговых операций.
     */
    enum DialogState {
        WAITING_PROMPT_NAME,
        WAITING_PROMPT_DESCRIPTION,
        WAITING_PROMPT_TEXT,
        WAITING_CHANNEL_NAME,
        WAITING_CHANNEL_HANDLE,
        WAITING_CHANNEL_DESCRIPTION
    }

    /**
     * Контекст диалога для хранения состояния и данных.
     */
    static class DialogContext {
        private DialogState state;
        private final Long userId;
        private final LocalDateTime createdAt;
        private String promptName;
        private String promptDescription;
        private Long promptId;
        private String channelName;
        private String channelHandle;
        private String channelUrl;

        DialogContext(DialogState state, Long userId) {
            this.state = state;
            this.userId = userId;
            this.createdAt = LocalDateTime.now();
        }

        boolean isExpired(int timeoutMinutes) {
            return LocalDateTime.now().isAfter(createdAt.plusMinutes(timeoutMinutes));
        }

        // Getters and Setters

        DialogState state() { return state; }
        void setState(DialogState state) { this.state = state; }
        Long userId() { return userId; }
        LocalDateTime createdAt() { return createdAt; }

        String getPromptName() { return promptName; }
        void setPromptName(String promptName) { this.promptName = promptName; }

        String getPromptDescription() { return promptDescription; }
        void setPromptDescription(String promptDescription) { this.promptDescription = promptDescription; }

        Long getPromptId() { return promptId; }
        void setPromptId(Long promptId) { this.promptId = promptId; }

        String getChannelName() { return channelName; }
        void setChannelName(String channelName) { this.channelName = channelName; }

        String getChannelHandle() { return channelHandle; }
        void setChannelHandle(String channelHandle) { this.channelHandle = channelHandle; }

        String getChannelUrl() { return channelUrl; }
        void setChannelUrl(String channelUrl) { this.channelUrl = channelUrl; }
    }
}
