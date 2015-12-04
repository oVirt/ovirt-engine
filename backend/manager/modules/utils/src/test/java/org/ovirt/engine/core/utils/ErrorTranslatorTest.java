package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.junit.Test;
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
            assertEquals("String should equal", "VM not found", error);
        } finally {
            Locale.setDefault(locale);
        }
    }

    @Test
    public void testNoStringSubstitutionWithList() {
        ErrorTranslator et = new ErrorTranslatorImpl(FILENAME);
        List<String> error = et.translateErrorText(Arrays.asList(TEST_KEY_NO_REPLACEMENT));
        assertTrue("Size", error.size() == 1);
        assertEquals("String should equal", "VM not found", error.get(0));
    }

    @Test
    public void testStringSubstitutionWithList() {
        ErrorTranslator et = new ErrorTranslatorImpl(FILENAME);
        List<String> error = et.translateErrorText(Arrays.asList(TEST_KEY_WITH_REPLACEMENT,
                "$action SOMEACTION", "$type SOME Type"));
        String result = "Cannot SOMEACTION SOME Type. VM's Image doesn't exist.";
        assertTrue("Size", error.size() == 1);
        assertEquals("String should equal", result, error.get(0));
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
            List<String> errors = et.translateErrorText(Arrays.asList(TEST_KEY_NO_REPLACEMENT));
            assertEquals("Unexpected Size", 1, errors.size());
            assertEquals("String should equal", "Desktop nicht gefunden", errors.get(0));
            String error = et.translateErrorTextSingle(TEST_KEY_NO_REPLACEMENT, Locale.GERMAN);
            assertEquals("String should equal", "Desktop nicht gefunden", error);
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
        List<String> errors = et.translateErrorText(Arrays.asList(TEST_KEY_NO_REPLACEMENT), Locale.ITALIAN);
        assertEquals("Unexpected Size", 1, errors.size());
        assertEquals("String should equal", "Impossibile trovare il desktop", errors.get(0));
        String error = et.translateErrorTextSingle(TEST_KEY_NO_REPLACEMENT, Locale.ITALIAN);
        assertEquals("String should equal", "Impossibile trovare il desktop", error);
    }
}
