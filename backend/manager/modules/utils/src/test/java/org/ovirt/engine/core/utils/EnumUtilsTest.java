package org.ovirt.engine.core.utils;

import org.ovirt.engine.core.common.utils.EnumUtils;

import junit.framework.TestCase;

public class EnumUtilsTest extends TestCase {
    public void testConvertToStringWithSpaces() {
        assertEquals("Hello There", EnumUtils.ConvertToStringWithSpaces("HelloThere"));
    }
}
