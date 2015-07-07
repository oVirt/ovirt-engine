package org.ovirt.engine.core.common.businessentities;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

public class VdsStaticTest {

    @Test
    public void emptyStringMakesEmptyMap() {
        final String empty = "";
        Map<String, String> map = VdsStatic.pmOptionsStringToMap(empty);
        mapIsEmpty(map);
    }

    private void mapIsEmpty(final Map<String, String> map) {
        assertEquals(0, map.size());
    }

}
