package org.ovirt.engine.core.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.ovirt.engine.core.common.interfaces.ErrorTranslator;

public class ErrorTranslatorTest extends TestCase {

    private static final String TEST_KEY_NO_REPLACEMENT = "TEST_KEY_NO_REPLACEMENT";
    private static final String TEST_KEY_WITH_REPLACEMENT = "TEST_KEY_WITH_REPLACEMENT";

    public void testNoStringSubstitutionWithoutSuffix() {
        doTestNoStringSubstitution("AppErrors");
    }

    public void testNoStringSubstitutionWithSuffix() {
        doTestNoStringSubstitution("AppErrors.properties");
    }

    private void doTestNoStringSubstitution(String name) {
        ErrorTranslator et = new ErrorTranslatorImpl(name);
        String error = et.TranslateErrorTextSingle(TEST_KEY_NO_REPLACEMENT);
        assertEquals("String should equal", "VM not found", error);
    }

    public void testNoStringSubstitutionWithList() {
        ErrorTranslator et = new ErrorTranslatorImpl("AppErrors");
        List<String> error = et.TranslateErrorText(Arrays.asList(TEST_KEY_NO_REPLACEMENT));
        assertTrue("Size", error.size() == 1);
        assertEquals("String should equal", "VM not found", error.get(0));
    }

    public void testStringSubstitutionWithList() {
        ErrorTranslator et = new ErrorTranslatorImpl("AppErrors");
        List<String> error = et.TranslateErrorText(Arrays.asList(TEST_KEY_WITH_REPLACEMENT,
                "$action SOMEACTION", "$type SOME Type"));
        String result = "Cannot SOMEACTION SOME Type. VM's Image doesn't exist.";
        assertTrue("Size", error.size() == 1);
        assertEquals("String should equal", result, error.get(0));
    }

    public void testLocaleSpecificWithoutSuffix() {
        doTestLocaleSpecific("AppErrors");
    }

    public void testLocaleSpecificWithSuffix() {
        doTestLocaleSpecific("AppErrors.properties");
    }

    private void doTestLocaleSpecific(String name) {
        Locale locale = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMAN);
            ErrorTranslator et = new ErrorTranslatorImpl(name);
            List<String> errors = et.TranslateErrorText(Arrays.asList(TEST_KEY_NO_REPLACEMENT));
            assertEquals("Unexpected Size", 1, errors.size());
            assertEquals("String should equal", "Desktop nicht gefunden", errors.get(0));
            String error = et.TranslateErrorTextSingle(TEST_KEY_NO_REPLACEMENT, Locale.GERMAN);
            assertEquals("String should equal", "Desktop nicht gefunden", error);
        } finally {
            Locale.setDefault(locale);
        }
    }

    public void testLocaleOverrideWithoutSuffix() {
        doTestLocaleOverride("AppErrors");
    }

    public void testLocaleOverrideWithSuffix() {
        doTestLocaleOverride("AppErrors.properties");
    }

    private void doTestLocaleOverride(String name) {
        ErrorTranslator et = new ErrorTranslatorImpl(name);
        List<String> errors = et.TranslateErrorText(Arrays.asList(TEST_KEY_NO_REPLACEMENT), Locale.ITALIAN);
        assertEquals("Unexpected Size", 1, errors.size());
        assertEquals("String should equal", "Impossibile trovare il desktop", errors.get(0));
        String error = et.TranslateErrorTextSingle(TEST_KEY_NO_REPLACEMENT, Locale.ITALIAN);
        assertEquals("String should equal", "Impossibile trovare il desktop", error);
    }
}
