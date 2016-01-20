package org.ovirt.engine.core.common.utils.pm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

public class PowerManagementUtilsTest {

    @Test
    public void emptyStringMakesEmptyMap() {
        final String empty = "";
        Map<String, String> map = PowerManagementUtils.pmOptionsStringToMap(empty);
        assertTrue(map.isEmpty());
    }

    @Test
    public void testStringToMap() {
        final String str = "ipport=666,secure=true";
        Map<String, String> map = PowerManagementUtils.pmOptionsStringToMap(str);
        assertEquals(2, map.size());
        assertEquals("666", map.get("ipport"));
        assertEquals("true", map.get("secure"));
    }
}
