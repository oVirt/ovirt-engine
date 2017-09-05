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

    @Test
    public void valeOfRightCaseSensitive() {
        assertEquals(EnumForTesting.ENUM1, EnumUtils.valueOf(EnumForTesting.class, EnumForTesting.ENUM1.name(), false));
    }

    @Test
    public void valeOfRightCaseInsensitive() {
        assertEquals(EnumForTesting.ENUM1, EnumUtils.valueOf(EnumForTesting.class, EnumForTesting.ENUM1.name(), true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valeOfWrongCaseSensitive() {
        EnumUtils.valueOf(EnumForTesting.class, EnumForTesting.ENUM1.name().toLowerCase(), false);
    }

    @Test
    public void valeOfWrongCaseInsensitive() {
        assertEquals(EnumForTesting.ENUM1,
                EnumUtils.valueOf(EnumForTesting.class, EnumForTesting.ENUM1.name().toLowerCase(), true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void valeOfWrongValueSensitive() {
        EnumUtils.valueOf(EnumForTesting.class, "no such value!", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void valeOfWrongValueInsensitive() {
        EnumUtils.valueOf(EnumForTesting.class, "no such value!", true);
    }
}
