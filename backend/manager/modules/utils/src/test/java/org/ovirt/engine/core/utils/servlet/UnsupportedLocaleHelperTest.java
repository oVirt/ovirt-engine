package org.ovirt.engine.core.utils.servlet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class UnsupportedLocaleHelperTest {
    static final List<String> unvalidatedUnsupportedLocales = new ArrayList<>();

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.UnsupportedLocalesFilterOverrides, unvalidatedUnsupportedLocales));
    }

    List<String> allLocales;

    @BeforeEach
    public void setUp() {
        allLocales = getAllLocales();
        unvalidatedUnsupportedLocales.clear();
    }

    @Test
    public void testGetDisplayLocales() {
        List<String> displayLocales = new ArrayList<>();
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, displayLocales, new ArrayList<>());
        assertNotNull(locales, "Result should not be null");
        assertEquals(8, locales.size(), "There should be 8 locales");
    }

    @Test
    public void testGetDisplayLocalesUnsupported() {
        List<String> displayLocales = new ArrayList<>();
        List<String> unSupportedLocales = new ArrayList<>();
        unSupportedLocales.add("pt_BR");
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, displayLocales, unSupportedLocales);
        assertNotNull(locales, "Result should not be null");
        assertEquals(7, locales.size(), "There should be 7 locales");
        assertFalse(locales.contains("pt_BR"), "Locales should not contain 'pt_BR'");
    }

    @Test
    public void testGetDisplayLocalesWithUnsupportedHiding2() {
        List<String> unSupportedLocales = new ArrayList<>();
        unSupportedLocales.add("de_DE");
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, new ArrayList<>(), unSupportedLocales);
        assertNotNull(locales, "Result should not be null");
        assertEquals(7, locales.size(), "There should be 7 locales");
        assertFalse(locales.contains("de_DE"), "Locales should not contain 'de_DE'");
    }

    @Test
    public void testGetDisplayLocalesWithUnsupportedShowing() {
        List<String> unSupportedLocales = new ArrayList<>();
        unSupportedLocales.add("de_DE");
        List<String> displayUnsupported = new ArrayList<>();
        displayUnsupported.add("de_DE");
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, displayUnsupported,
                unSupportedLocales);
        assertNotNull(locales, "Result should not be null");
        assertEquals(8, locales.size(), "There should be 8 locales");
        assertTrue(locales.contains("de_DE"), "Locales should contain 'de_DE'");
    }

    @Test
    public void testGetLocalesKeysUnSupported() {
        unvalidatedUnsupportedLocales.add("ko_KR");
        List<String> locales = UnsupportedLocaleHelper.getLocalesKeys(ConfigValues.UnsupportedLocalesFilterOverrides);
        assertNotNull(locales, "Result should not be null");
        assertEquals(1, locales.size(), "There should be 1 locales");
        assertEquals("ko_KR", locales.get(0), "Locale should be ko_KR");
    }

    @Test
    public void testGetLocalesKeysWithInvalid() {
        unvalidatedUnsupportedLocales.add("ko_KR");
        unvalidatedUnsupportedLocales.add("abcdds");
        List<String> locales = UnsupportedLocaleHelper.getLocalesKeys(ConfigValues.UnsupportedLocalesFilterOverrides);
        assertNotNull(locales, "Result should not be null");
        assertEquals(1, locales.size(), "There should be 1 locales");
        assertEquals("ko_KR", locales.get(0), "Locale should be ko_KR");
    }

    @Test
    public void testGetLocalesKeysDisplayLocalesEmpty() {
        List<String> locales = UnsupportedLocaleHelper.getLocalesKeys(ConfigValues.UnsupportedLocalesFilterOverrides);
        assertNotNull(locales, "Result should not be null");
        assertEquals(0, locales.size(), "There should be 0 locales");
    }

    private List<String> getAllLocales() {
        List<String> result = new ArrayList<>();
        result.add("de_DE");
        result.add("en_US");
        result.add("fr_FR");
        result.add("es_ES");
        result.add("ja_JP");
        result.add("ko_KR");
        result.add("pt_BR");
        result.add("zh_CN");
        return result;
    }
}
