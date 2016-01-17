package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.ovirt.engine.core.common.TimeZoneType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParams;

public class GetDefaultTimeZoneQueryTest extends AbstractUserQueryTest<TimeZoneQueryParams, GetDefaultTimeZoneQuery<TimeZoneQueryParams>> {

    /** The default time zone for the test */
    private static final String DEFAULT_WINDOWS_TIME_ZONE = "Israel Standard Time";
    private static final String DEFAULT_GENERAL_TIME_ZONE = "Asia/Jerusalem";

    @Test
    public void testQueryDefaultWindowsTimeZone() {
        mcr.mockConfigValue(ConfigValues.DefaultWindowsTimeZone, DEFAULT_WINDOWS_TIME_ZONE);
        when(getQueryParameters().getTimeZoneType()).thenReturn(TimeZoneType.WINDOWS_TIMEZONE);
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        String result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong default time zone: " + result, DEFAULT_WINDOWS_TIME_ZONE, result);
    }

    @Test
    public void testQueryDefaultGeneralTimeZone() {
        mcr.mockConfigValue(ConfigValues.DefaultGeneralTimeZone, DEFAULT_GENERAL_TIME_ZONE);
        when(getQueryParameters().getTimeZoneType()).thenReturn(TimeZoneType.GENERAL_TIMEZONE);
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        String result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong default time zone: " + result, DEFAULT_GENERAL_TIME_ZONE, result);
    }
}
