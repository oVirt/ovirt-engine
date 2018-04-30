package org.ovirt.engine.core.common.utils.pm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testKeyValueStringToMap() {
        final String str = "ipport=666,ssh_options=-oCiphers=+3des-cbc";
        Map<String, String> map = PowerManagementUtils.pmOptionsStringToMap(str);
        assertEquals(2, map.size());
        assertEquals("666", map.get("ipport"));
        assertEquals("-oCiphers=+3des-cbc", map.get("ssh_options"));
    }
}
