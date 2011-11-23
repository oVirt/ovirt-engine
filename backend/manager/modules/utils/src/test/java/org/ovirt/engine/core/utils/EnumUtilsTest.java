package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.utils.EnumUtils;

public class EnumUtilsTest {

    protected enum EnumForTest {
        ENUM1, ENUM2
    }

    @Test
    public void convertToStringWithSpaces() {
        assertEquals("Hello There", EnumUtils.ConvertToStringWithSpaces("HelloThere"));
    }

    @Test
    public void nameOrNullForNull() throws Exception {
        assertNull(EnumUtils.<EnumForTest>nameOrNull(null));
    }

    @Test
    public void nameOrNullForEnum() throws Exception {
        assertEquals(EnumForTest.ENUM1.name(), EnumUtils.nameOrNull(EnumForTest.ENUM1));
    }
}
