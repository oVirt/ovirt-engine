package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ovirt.engine.core.common.utils.EnumUtils;

public class EnumUtilsTest {

    @Test
    public void convertToStringWithSpaces() {
        assertEquals("Hello There", EnumUtils.ConvertToStringWithSpaces("HelloThere"));
    }
}
