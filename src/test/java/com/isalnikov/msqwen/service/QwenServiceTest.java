package com.isalnikov.msqwen.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit тесты для {@link QwenService}.
 *
 * @author isalnikov
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class QwenServiceTest {

    @InjectMocks
    private QwenService qwenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(qwenService, "qwenCliPath", "echo");
        ReflectionTestUtils.setField(qwenService, "timeoutSeconds", 30);
    }

    @Test
    void sendToQwen_shouldReturnOutput() {
        // echo просто выводит текст, это эмуляция успешного вызова
        String result = qwenService.sendToQwen("test prompt");

        assertNotNull(result);
        assertTrue(result.contains("test prompt"));
    }

    @Test
    void sendToQwen_shouldHandleTimeout() {
        ReflectionTestUtils.setField(qwenService, "timeoutSeconds", 1);
        // sleep 10 должен вызвать таймаут
        ReflectionTestUtils.setField(qwenService, "qwenCliPath", "sleep");

        assertThrows(RuntimeException.class, () -> qwenService.sendToQwen("10"));
    }

    @Test
    void executeCommand_shouldReturnOutput() {
        String result = qwenService.executeCommand("echo hello");

        assertNotNull(result);
        assertTrue(result.contains("hello"));
    }
}
