package org.ovirt.engine.core.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.config.ConfigValues;

public class TimeZoneTypeTest {

    private static final TimeZoneType windows = TimeZoneType.WINDOWS_TIMEZONE;
    private static final TimeZoneType general = TimeZoneType.GENERAL_TIMEZONE;

    @Test
    public void testWindowsTimeZones() {
        Map<String, String> windowsTimeZones = new HashMap<>();
        windowsTimeZones.put("GMT Standard Time", "(GMT) GMT Standard Time");
        windows.init(windowsTimeZones);
        Map<String, String> map = windows.getTimeZoneList();
        assertNotNull(map);
        assertEquals("(GMT) GMT Standard Time", map.get("GMT Standard Time"));
    }

    @Test
    public void testWindowsTimeZonesSort() {
        Map<String, String> windowsTimeZones = new HashMap<>();
        windowsTimeZones.put("North Asia Standard Time", "(GMT+07:00) North Asia Standard Time");
        windowsTimeZones.put("Alaskan Standard Time", "(GMT-09:00) Alaskan Standard Time");
        windowsTimeZones.put("Turkey Standard Time", "(GMT+03:00) Turkey Standard Time");
        windows.init(windowsTimeZones);
        List<String> sortedTimezoneValues = new ArrayList<>(windows.getTimeZoneList().values());
        assertEquals("(GMT-09:00) Alaskan Standard Time", sortedTimezoneValues.get(0));
        assertEquals("(GMT+03:00) Turkey Standard Time", sortedTimezoneValues.get(1));
        assertEquals("(GMT+07:00) North Asia Standard Time", sortedTimezoneValues.get(2));
    }

    @Test
    public void testGeneralTimeZones() {
        Map<String, String> generalTimeZones = new HashMap<>();
        generalTimeZones.put("Asia/Jerusalem", "(GMT+02:00) Israel Standard Time");
        general.init(generalTimeZones);
        Map<String, String> map = general.getTimeZoneList();
        assertNotNull(map);
        assertNull(map.get("Asia/Riyadh78"));
        assertEquals("(GMT+02:00) Israel Standard Time", map.get("Asia/Jerusalem"));
    }

    @Test
    public void testGeneralTimeZonesSort() {
        Map<String, String> generalTimeZones = new HashMap<>();
        generalTimeZones.put("North Asia Standard Time", "(GMT+07:00) North Asia Standard Time");
        generalTimeZones.put("Alaskan Standard Time", "(GMT-09:00) Alaskan Standard Time");
        generalTimeZones.put("Turkey Standard Time", "(GMT+03:00) Turkey Standard Time");
        general.init(generalTimeZones);
        List<String> sortedTimezoneValues = new ArrayList<>(general.getTimeZoneList().values());
        assertEquals("(GMT-09:00) Alaskan Standard Time", sortedTimezoneValues.get(0));
        assertEquals("(GMT+03:00) Turkey Standard Time", sortedTimezoneValues.get(1));
        assertEquals("(GMT+07:00) North Asia Standard Time", sortedTimezoneValues.get(2));
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
}
