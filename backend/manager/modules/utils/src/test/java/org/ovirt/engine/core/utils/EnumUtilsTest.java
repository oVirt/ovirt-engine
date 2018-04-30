package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
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

    @Test
    public void valeOfWrongCaseSensitive() {
        assertThrows(IllegalArgumentException.class,
                () -> EnumUtils.valueOf(EnumForTesting.class, EnumForTesting.ENUM1.name().toLowerCase(), false));
    }

    @Test
    public void valeOfWrongCaseInsensitive() {
        assertEquals(EnumForTesting.ENUM1,
                EnumUtils.valueOf(EnumForTesting.class, EnumForTesting.ENUM1.name().toLowerCase(), true));
    }

    @Test
    public void valeOfWrongValueSensitive() {
        assertThrows(IllegalArgumentException.class,
                () -> EnumUtils.valueOf(EnumForTesting.class, "no such value!", false));
    }

    @Test
    public void valeOfWrongValueInsensitive() {
        assertThrows(IllegalArgumentException.class,
                () -> EnumUtils.valueOf(EnumForTesting.class, "no such value!", true));
    }
}
