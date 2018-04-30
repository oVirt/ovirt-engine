package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Locale;

import org.junit.jupiter.api.Test;

public class LocaleUtilsTest {

    @Test
    public void testGetLocaleFromString() {
        assertNull(LocaleUtils.getLocaleFromString(null), "The locale should be null");
        assertNull(LocaleUtils.getLocaleFromString("notalocale"), "The locale should be null");
        assertNull(LocaleUtils.getLocaleFromString("index.html"), "The locale should be null");
        assertEquals(Locale.JAPANESE, LocaleUtils.getLocaleFromString("ja"), "The locale should be jp");
    }

    @Test
    public void testGetLocaleFromStringStringWithDefault() {
        assertEquals(Locale.US, LocaleUtils.getLocaleFromString(null, true), "The locale should be en-US");
        assertEquals(Locale.US, LocaleUtils.getLocaleFromString("notalocale", true),
                "The locale should be en-US");
        assertEquals(Locale.US, LocaleUtils.getLocaleFromString("index.html", true),
                "The locale should be en-US");
        assertEquals(Locale.JAPANESE, LocaleUtils.getLocaleFromString("ja", true), "The locale should be jp");
    }


}
