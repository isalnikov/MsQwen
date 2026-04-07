package com.isalnikov.msqwen.bot;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit тесты для {@link TelegramKeyboardFactory}.
 *
 * @author isalnikov
 * @version 1.0
 */
class TelegramKeyboardFactoryTest {

    @Test
    void testCreateMainMenu() {
        var markup = TelegramKeyboardFactory.createMainMenu();

        assertNotNull(markup);
        assertNotNull(markup.getKeyboard());
        // Главное меню должно иметь несколько рядов
        assertTrue(markup.getKeyboard().size() >= 3);
    }

    @Test
    void testCreatePromptSelection() {
        var prompts = List.of(
                new TelegramKeyboardFactory.PromptItem(1L, "Prompt 1"),
                new TelegramKeyboardFactory.PromptItem(2L, "Prompt 2")
        );

        var markup = TelegramKeyboardFactory.createPromptSelection(prompts, "delete");

        assertNotNull(markup);
        // 2 промпта + 1 кнопка отмены
        assertEquals(3, markup.getKeyboard().size());
    }

    @Test
    void testCreatePromptSelection_Empty() {
        var markup = TelegramKeyboardFactory.createPromptSelection(List.of(), "delete");

        assertNotNull(markup);
        // Только кнопка отмены
        assertEquals(1, markup.getKeyboard().size());
    }

    @Test
    void testCreateChannelSelection() {
        var channels = List.of(
                new TelegramKeyboardFactory.ChannelItem(1L, "Channel 1"),
                new TelegramKeyboardFactory.ChannelItem(2L, "Channel 2"),
                new TelegramKeyboardFactory.ChannelItem(3L, "Channel 3")
        );

        var markup = TelegramKeyboardFactory.createChannelSelection(channels, "delete");

        assertNotNull(markup);
        // 3 канала + 1 кнопка отмены
        assertEquals(4, markup.getKeyboard().size());
    }

    @Test
    void testCreateConfirmation() {
        var markup = TelegramKeyboardFactory.createConfirmation("prompt", 1L);

        assertNotNull(markup);
        // 2 ряда: подтвердить и отменить
        assertEquals(2, markup.getKeyboard().size());
    }

    @Test
    void testCreatePagination_FirstPage() {
        var markup = TelegramKeyboardFactory.createPagination(0, 5, "news");

        assertNotNull(markup);
        // Только вперёд (нет назад на первой странице)
        assertEquals(1, markup.getKeyboard().size());
    }

    @Test
    void testCreatePagination_MiddlePage() {
        var markup = TelegramKeyboardFactory.createPagination(2, 5, "news");

        assertNotNull(markup);
        // Назад + вперёд
        assertEquals(1, markup.getKeyboard().size());
    }

    @Test
    void testCreatePagination_LastPage() {
        var markup = TelegramKeyboardFactory.createPagination(4, 5, "news");

        assertNotNull(markup);
        // Только назад (нет вперёд на последней странице)
        assertEquals(1, markup.getKeyboard().size());
    }

    @Test
    void testCreateButton() {
        var button = TelegramKeyboardFactory.createButton("Test", "action:data");

        assertNotNull(button);
        assertEquals("Test", button.getText());
        assertEquals("action:data", button.getCallbackData());
    }

    @Test
    void testPromptItemRecord() {
        var item = new TelegramKeyboardFactory.PromptItem(1L, "Test");

        assertEquals(1L, item.id());
        assertEquals("Test", item.name());
    }

    @Test
    void testChannelItemRecord() {
        var item = new TelegramKeyboardFactory.ChannelItem(2L, "Test Channel");

        assertEquals(2L, item.id());
        assertEquals("Test Channel", item.name());
    }
}
