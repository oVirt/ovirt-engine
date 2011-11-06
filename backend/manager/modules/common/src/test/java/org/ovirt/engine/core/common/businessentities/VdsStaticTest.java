package org.ovirt.engine.core.common.businessentities;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.queries.ValueObjectMap;

public class VdsStaticTest {

    @Test
    public void emptyStringMakesEmptyMap() {
        final String empty = "";
        ValueObjectMap map = VdsStatic.PmOptionsStringToMap(empty);
        mapIsEmpty(map);
    }

    private void mapIsEmpty(final ValueObjectMap map) {
        assertEquals(0, map.getValuePairs().size());
    }

}
