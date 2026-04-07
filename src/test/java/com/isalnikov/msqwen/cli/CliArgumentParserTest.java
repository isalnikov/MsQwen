package com.isalnikov.msqwen.cli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit тесты для {@link CliArgumentParser}.
 *
 * @author isalnikov
 * @version 1.0
 */
class CliArgumentParserTest {

    @Test
    void testParseKeyValueArguments() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=1",
                "--prompt.id=5",
                "--target=news"
        });

        assertEquals("1", parser.get("user.id"));
        assertEquals("5", parser.get("prompt.id"));
        assertEquals("news", parser.get("target"));
    }

    @Test
    void testParseFlags() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--help",
                "--version",
                "--parse"
        });

        assertTrue(parser.hasFlag("help"));
        assertTrue(parser.hasFlag("version"));
        assertTrue(parser.hasFlag("parse"));
    }

    @Test
    void testMixedArguments() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--help",
                "--user.id=1",
                "--verbose"
        });

        assertTrue(parser.hasFlag("help"));
        assertTrue(parser.hasFlag("verbose"));
        assertEquals("1", parser.get("user.id"));
    }

    @Test
    void testGetLong() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=12345"
        });

        assertEquals(12345L, parser.getLong("user.id"));
    }

    @Test
    void testGetLong_InvalidValue() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=abc"
        });

        assertNull(parser.getLong("user.id"));
    }

    @Test
    void testGetLong_MissingKey() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{});

        assertNull(parser.getLong("user.id"));
    }

    @Test
    void testHas() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=1"
        });

        assertTrue(parser.has("user.id"));
        assertFalse(parser.has("prompt.id"));
    }

    @Test
    void testGetOrDefault() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=1"
        });

        assertEquals("1", parser.getOrDefault("user.id", "default"));
        assertEquals("default", parser.getOrDefault("missing.key", "default"));
    }

    @Test
    void testGet() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=1"
        });

        assertEquals("1", parser.get("user.id"));
        assertNull(parser.get("missing.key"));
    }

    @Test
    void testGetAll() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=1",
                "--prompt.id=2"
        });

        var all = parser.getAll();
        assertEquals(2, all.size());
        assertEquals("1", all.get("user.id"));
        assertEquals("2", all.get("prompt.id"));
    }

    @Test
    void testNullArgs() {
        CliArgumentParser parser = new CliArgumentParser(null);

        assertEquals(0, parser.getAll().size());
    }

    @Test
    void testEmptyArgs() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{});

        assertEquals(0, parser.getAll().size());
    }

    @Test
    void testValueWithEqualsSign() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--url=https://example.com?a=1&b=2"
        });

        assertEquals("https://example.com?a=1&b=2", parser.get("url"));
    }

    @Test
    void testToString() {
        CliArgumentParser parser = new CliArgumentParser(new String[]{
                "--user.id=1"
        });

        String result = parser.toString();
        assertNotNull(result);
        assertTrue(result.contains("user.id"));
        assertTrue(result.contains("1"));
    }
}
