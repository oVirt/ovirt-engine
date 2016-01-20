package org.ovirt.engine.core.common.utils.pm;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class PowerManagementUtilsTest {

    @Test
    public void emptyStringMakesEmptyMap() {
        final String empty = "";
        Map<String, String> map = PowerManagementUtils.pmOptionsStringToMap(empty);
        mapIsEmpty(map);
    }

    private void mapIsEmpty(final Map<String, String> map) {
        assertEquals(0, map.size());
    }
}
