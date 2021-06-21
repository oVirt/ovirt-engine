package org.ovirt.engine.core.utils.timezone;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TimeZoneReaderTest {
    @BeforeAll
    public static void init() throws URISyntaxException {
        Path directoryPath =
                new File(TimeZoneReader.class.getResource("/" + TimeZoneReader.INSTANCE.DIR_NAME).toURI().getPath())
                        .toPath();
        TimeZoneReader.INSTANCE.init(directoryPath);
    }

    @Test
    public void testUnknownTimezone() {
        // should not be able to parse unknown time zone ids
        String invalidTimeZoneKey = "koko";
        assertEquals(null, TimeZoneReader.INSTANCE.getGeneralTimezones().get(invalidTimeZoneKey));
        assertEquals(null, TimeZoneReader.INSTANCE.getWindowsTimezones().get(invalidTimeZoneKey));
        assertEquals(null, TimeZoneReader.INSTANCE.getWindowsToJavaTimezones().get(invalidTimeZoneKey));
    }
    @Test
    public void testGeneralTimezones() {
        assertEquals(2, TimeZoneReader.INSTANCE.getGeneralTimezones().size());
        assertEquals("(GMT-09:00) Alaskan Standard Time",
                TimeZoneReader.INSTANCE.getGeneralTimezones().get("America/Anchorage"));
        assertEquals("(GMT+06:00) Central Asia Standard Time",
                TimeZoneReader.INSTANCE.getGeneralTimezones().get("Asia/Dhaka"),
                "timezones file extension should work");
    }

    @Test
    public void testWindowsTimezones() {
        assertEquals(2, TimeZoneReader.INSTANCE.getWindowsTimezones().size());
        assertEquals("(GMT-09:00) Alaskan Standard Time", TimeZoneReader.INSTANCE.getWindowsTimezones().get("Alaskan Standard Time"));
        assertEquals("(GMT+06:00) Central Asia Standard Time",
                TimeZoneReader.INSTANCE.getWindowsTimezones().get("Central Asia Standard Time"),
                "timezones file extension should work");
    }

    @Test
    public void testWindowsToJavaTimezones() {
        assertEquals(2, TimeZoneReader.INSTANCE.getWindowsToJavaTimezones().size());
        assertEquals("America/Anchorage", TimeZoneReader.INSTANCE.getWindowsToJavaTimezones().get("Alaskan Standard Time"));
        assertEquals("Asia/Dhaka",
                TimeZoneReader.INSTANCE.getWindowsToJavaTimezones().get("Central Asia Standard Time"),
                "timezones file extension should work");
    }
}
