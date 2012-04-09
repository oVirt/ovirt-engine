package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void testEnumCollectionToStringListForNullInput() {
        assertNull(EnumUtils.enumCollectionToStringList(null));
    }

    @Test
    public void testEnumCollectionToStringList() {
        List<EnumForTest> enumCollection = new ArrayList<EnumForTest>();
        enumCollection.add(EnumForTest.ENUM1);
        enumCollection.add(EnumForTest.ENUM2);

        List<String> stringList = EnumUtils.enumCollectionToStringList(enumCollection);
        assertNotNull(stringList);
        assertEquals(2, stringList.size());
        assertTrue(stringList.contains(EnumForTest.ENUM1.name()));
        assertTrue(stringList.contains(EnumForTest.ENUM2.name()));
    }
}
