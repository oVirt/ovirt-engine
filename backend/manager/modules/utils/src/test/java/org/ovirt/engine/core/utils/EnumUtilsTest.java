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

    protected enum EnumForTesting {
        ENUM1, ENUM2
    }

    @Test
    public void convertToStringWithSpaces() {
        assertEquals("Hello There", EnumUtils.ConvertToStringWithSpaces("HelloThere"));
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
    public void testEnumCollectionToStringListForNullInput() {
        assertNull(EnumUtils.enumCollectionToStringList(null));
    }

    @Test
    public void testEnumCollectionToStringList() {
        List<EnumForTesting> enumCollection = new ArrayList<EnumForTesting>();
        enumCollection.add(EnumForTesting.ENUM1);
        enumCollection.add(EnumForTesting.ENUM2);

        List<String> stringList = EnumUtils.enumCollectionToStringList(enumCollection);
        assertNotNull(stringList);
        assertEquals(2, stringList.size());
        assertTrue(stringList.contains(EnumForTesting.ENUM1.name()));
        assertTrue(stringList.contains(EnumForTesting.ENUM2.name()));
    }
}
