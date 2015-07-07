package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.utils.EnumUtils;

public class EnumUtilsTest {

    protected enum EnumForTesting {
        ENUM1, ENUM2
    }

    @Test
    public void nameOrNullForNull() {
        assertNull(EnumUtils.<EnumForTesting>nameOrNull(null));
    }

    @Test
    public void nameOrNullForEnum() {
        assertEquals(EnumForTesting.ENUM1.name(), EnumUtils.nameOrNull(EnumForTesting.ENUM1));
    }
}
