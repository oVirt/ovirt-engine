package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.interfaces.ErrorTranslator;

public class ErrorTranslatorTest {

    private static final String TEST_KEY_NO_REPLACEMENT = "TEST_KEY_NO_REPLACEMENT";
    private static final String TEST_KEY_WITH_REPLACEMENT = "TEST_KEY_WITH_REPLACEMENT";
    private static final String FILENAME = "TestAppErrors";
    private static final String FILENAME_WITH_SUFFIX = FILENAME + ".properties";

    @Test
    public void testNoStringSubstitutionWithoutSuffix() {
        doTestNoStringSubstitution(FILENAME);
    }

    @Test
    public void testNoStringSubstitutionWithSuffix() {
        doTestNoStringSubstitution(FILENAME_WITH_SUFFIX);
    }

    private static void doTestNoStringSubstitution(String name) {
        Locale locale = Locale.ENGLISH;
        try {
            Locale.setDefault(Locale.ENGLISH);
            ErrorTranslator et = new ErrorTranslatorImpl(name);
            String error = et.translateErrorTextSingle(TEST_KEY_NO_REPLACEMENT);
            assertEquals("VM not found", error, "String should equal");
        } finally {
            Locale.setDefault(locale);
        }
    }

    @Test
    public void testNoStringSubstitutionWithList() {
        ErrorTranslator et = new ErrorTranslatorImpl(FILENAME);
        List<String> error = et.translateErrorText(Collections.singletonList(TEST_KEY_NO_REPLACEMENT));
        assertEquals(1, error.size(), "Size");
        assertEquals("VM not found", error.get(0), "String should equal");
    }

    @Test
    public void testStringSubstitutionWithList() {
        ErrorTranslator et = new ErrorTranslatorImpl(FILENAME);
        List<String> error = et.translateErrorText(Arrays.asList(TEST_KEY_WITH_REPLACEMENT,
                "$action SOMEACTION", "$type SOME Type"));
        String result = "Cannot SOMEACTION SOME Type. VM's Image doesn't exist.";
        assertEquals(1, error.size(), "Size");
        assertEquals(result, error.get(0), "String should equal");
    }

    @Test
    public void testLocaleSpecificWithoutSuffix() {
        doTestLocaleSpecific(FILENAME);
    }

    @Test
    public void testLocaleSpecificWithSuffix() {
        doTestLocaleSpecific(FILENAME_WITH_SUFFIX);
    }

    private static void doTestLocaleSpecific(String name) {
        Locale locale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMAN);
            ErrorTranslator et = new ErrorTranslatorImpl(name);
            List<String> errors = et.translateErrorText(Collections.singletonList(TEST_KEY_NO_REPLACEMENT));
            assertEquals(1, errors.size(), "Unexpected Size");
            assertEquals("Desktop nicht gefunden", errors.get(0), "String should equal");
            String error = et.translateErrorTextSingle(TEST_KEY_NO_REPLACEMENT, Locale.GERMAN);
            assertEquals("Desktop nicht gefunden", error, "String should equal");
        } finally {
            Locale.setDefault(locale);
        }
    }

    @Test
    public void testLocaleOverrideWithoutSuffix() {
        doTestLocaleOverride(FILENAME);
    }

    @Test
    public void testLocaleOverrideWithSuffix() {
        doTestLocaleOverride(FILENAME_WITH_SUFFIX);
    }

    private static void doTestLocaleOverride(String name) {
        ErrorTranslator et = new ErrorTranslatorImpl(name);
        List<String> errors = et.translateErrorText(Collections.singletonList(TEST_KEY_NO_REPLACEMENT), Locale.ITALIAN);
        assertEquals(1, errors.size(), "Unexpected Size");
        assertEquals("Impossibile trovare il desktop", errors.get(0), "String should equal");
        String error = et.translateErrorTextSingle(TEST_KEY_NO_REPLACEMENT, Locale.ITALIAN);
        assertEquals("Impossibile trovare il desktop", error, "String should equal");
    }
}
