package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TimeZone;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.TimeZoneQueryParams;
import org.ovirt.engine.core.vdsbroker.vdsbroker.SysprepHandler;

public class GetTimeZonesQueryTest extends AbstractSysprepQueryTest<TimeZoneQueryParams, GetTimeZonesQuery<TimeZoneQueryParams>> {

    /**
     * initializing the {@link #MockConfigRule} because there's a static initialization in the
     * {@link #GetTimeZonesQuery}, otherwise the mocking doesn't take place.
     */
    @Override
    public void setUp() throws Exception {
        mcr.starting(null);
        super.setUp();
    }

    @Test
    public void testExecuteQuery() {
        when(getQueryParameters().isWindowsOS()).thenReturn(true);
        getQuery().executeQueryCommand();

        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertEquals("Wrong number of time zones", SysprepHandler.timeZoneIndex.size(), result.size());

    }

    @Test
    public void queryWindowsTimeZones() {
        when(getQueryParameters().isWindowsOS()).thenReturn(true);
        getQuery().executeQueryCommand();
        Map<String, String> map = (Map<String, String>) getQuery().getQueryReturnValue().getReturnValue();

        assertFalse(map.get("Asia/Riyadh87") != null);
        assertTrue(TimeZone.getTimeZone(map.get("E. Australia Standard Time")) != null);
    }

    @Test
    public void queryGenralTimeZones() {
        when(getQueryParameters().isWindowsOS()).thenReturn(false);
        getQuery().getParameters().setWindowsOS(false);
        getQuery().executeQueryCommand();
        Map<String, String> map = ((Map<String, String>) getQuery().getQueryReturnValue().getReturnValue());

        assertFalse(map.get("E. Australia Standard Time") != null);
        assertTrue(TimeZone.getTimeZone(map.get("Asia/Riyadh87")) != null);
    }

}
