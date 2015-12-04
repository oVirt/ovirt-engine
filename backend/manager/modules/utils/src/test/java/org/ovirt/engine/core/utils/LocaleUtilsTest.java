package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;

import org.junit.Test;

public class LocaleUtilsTest {

    @Test
    public void testGetLocaleFromString() {
        assertNull("The locale should be null", LocaleUtils.getLocaleFromString(null));
        assertNull("The locale should be null", LocaleUtils.getLocaleFromString("notalocale"));
        assertNull("The locale should be null", LocaleUtils.getLocaleFromString("index.html"));
        assertEquals("The locale should be jp", Locale.JAPANESE, LocaleUtils.getLocaleFromString("ja"));
    }

    @Test
    public void testGetLocaleFromStringStringWithDefault() {
        assertEquals("The locale should be en-US", Locale.US, LocaleUtils.getLocaleFromString(null, true));
        assertEquals("The locale should be en-US", Locale.US, LocaleUtils.getLocaleFromString("notalocale", true));
        assertEquals("The locale should be en-US", Locale.US, LocaleUtils.getLocaleFromString("index.html", true));
        assertEquals("The locale should be jp", Locale.JAPANESE, LocaleUtils.getLocaleFromString("ja", true));
    }


}
