package org.ovirt.engine.core.utils.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class UnsupportedLocaleHelperTest {
    static final List<String> unvalidatedUnsupportedLocales = new ArrayList<>();

    @ClassRule
    public static MockConfigRule mcr =
    new MockConfigRule(mockConfig(ConfigValues.UnsupportedLocalesFilterOverrides, unvalidatedUnsupportedLocales));

    List<String> allLocales;

    @Before
    public void setUp() throws Exception {
        allLocales = getAllLocales();
        unvalidatedUnsupportedLocales.clear();
    }

    @Test
    public void testGetDisplayLocales() {
        List<String> displayLocales = new ArrayList<>();
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, displayLocales, new ArrayList<>());
        assertNotNull("Result should not be null", locales);
        assertEquals("There should be 8 locales", 8, locales.size());
    }

    @Test
    public void testGetDisplayLocalesUnsupported() {
        List<String> displayLocales = new ArrayList<>();
        List<String> unSupportedLocales = new ArrayList<>();
        unSupportedLocales.add("pt_BR");
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, displayLocales, unSupportedLocales);
        assertNotNull("Result should not be null", locales);
        assertEquals("There should be 7 locales", 7, locales.size());
        assertFalse("Locales should not contain 'pt_BR'", locales.contains("pt_BR"));
    }

    @Test
    public void testGetDisplayLocalesWithUnsupportedHiding2() {
        List<String> unSupportedLocales = new ArrayList<>();
        unSupportedLocales.add("de_DE");
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, new ArrayList<>(), unSupportedLocales);
        assertNotNull("Result should not be null", locales);
        assertEquals("There should be 7 locales", 7, locales.size());
        assertFalse("Locales should not contain 'de_DE'", locales.contains("de_DE"));
    }

    @Test
    public void testGetDisplayLocalesWithUnsupportedShowing() {
        List<String> unSupportedLocales = new ArrayList<>();
        unSupportedLocales.add("de_DE");
        List<String> displayUnsupported = new ArrayList<>();
        displayUnsupported.add("de_DE");
        List<String> locales = UnsupportedLocaleHelper.getDisplayedLocales(allLocales, displayUnsupported,
                unSupportedLocales);
        assertNotNull("Result should not be null", locales);
        assertEquals("There should be 8 locales", 8, locales.size());
        assertTrue("Locales should contain 'de_DE'", locales.contains("de_DE"));
    }

    @Test
    public void testGetLocalesKeysUnSupported() {
        unvalidatedUnsupportedLocales.add("ko_KR");
        List<String> locales = UnsupportedLocaleHelper.getLocalesKeys(ConfigValues.UnsupportedLocalesFilterOverrides);
        assertNotNull("Result should not be null", locales);
        assertEquals("There should be 1 locales", 1, locales.size());
        assertEquals("Locale should be ko_KR", "ko_KR", locales.get(0));
    }

    @Test
    public void testGetLocalesKeysWithInvalid() {
        unvalidatedUnsupportedLocales.add("ko_KR");
        unvalidatedUnsupportedLocales.add("abcdds");
        List<String> locales = UnsupportedLocaleHelper.getLocalesKeys(ConfigValues.UnsupportedLocalesFilterOverrides);
        assertNotNull("Result should not be null", locales);
        assertEquals("There should be 1 locales", 1, locales.size());
        assertEquals("Locale should be ko_KR", "ko_KR", locales.get(0));
    }

    @Test
    public void testGetLocalesKeysDisplayLocalesEmpty() {
        List<String> locales = UnsupportedLocaleHelper.getLocalesKeys(ConfigValues.UnsupportedLocalesFilterOverrides);
        assertNotNull("Result should not be null", locales);
        assertEquals("There should be 0 locales", 0, locales.size());
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
