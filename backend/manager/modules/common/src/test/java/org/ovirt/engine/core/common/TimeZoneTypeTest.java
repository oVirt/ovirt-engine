package org.ovirt.engine.core.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.WindowsJavaTimezoneMapping;

public class TimeZoneTypeTest {

    private static final TimeZoneType windows = TimeZoneType.WINDOWS_TIMEZONE;
    private static final TimeZoneType general = TimeZoneType.GENERAL_TIMEZONE;

    @Test
    public void testWindowsTimeZones() {
        Map<String, String> map = windows.getTimeZoneList();
        assertNotNull(map);
        assertEquals("(GMT) GMT Standard Time", map.get("GMT Standard Time"));
    }

    @Test
    public void testGeneralTimeZones() {
        Map<String, String> map = general.getTimeZoneList();
        assertNotNull(map);
        assertNull(map.get("Asia/Riyadh78"));
        assertEquals("(GMT+02:00) Israel Standard Time", map.get("Asia/Jerusalem"));
    }

    @Test
    public void testDefaultConfigurationValues() {
        assertEquals(ConfigValues.DefaultWindowsTimeZone, windows.getDefaultTimeZoneConfigurationKey());
        assertEquals(ConfigValues.DefaultGeneralTimeZone, general.getDefaultTimeZoneConfigurationKey());
    }

    @Test
    public void testUltimateFallbackValues() {
        assertEquals("GMT Standard Time", windows.getUltimateFallback());
        assertEquals("Etc/GMT", general.getUltimateFallback());
    }

    @Test
    public void testWindowsTimeZonesKeys() {
        final Set<String> windowsTimeZoneKeys = WindowsJavaTimezoneMapping.getKeys();
        for (String timeZoneKey : TimeZoneType.WINDOWS_TIMEZONE.getTimeZoneList().keySet()) {
            assertThat(windowsTimeZoneKeys, hasItem(timeZoneKey));
        }
    }
}
