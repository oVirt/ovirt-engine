package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

public class GuidUtilsTest {
    @Test
    public void toByteArray() {
        final byte[] byteArray = GuidUtils.ToByteArray(UUID.randomUUID());
        Assert.assertNotNull(byteArray);
        Assert.assertEquals(16, byteArray.length);
    }

    @Test
    public void toByteArrayAllNoll() {
        final byte[] allNullArray = GuidUtils.ToByteArray(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        Assert.assertNotNull(allNullArray);
        for (int i = 0; i < 16; i++) {
            Assert.assertEquals(0, allNullArray[i]);
        }
    }

    @Test
    public void testGuidListValues() {
        String listValues = "e61f7070-cd52-46ca-88c2-686e1c70fe44,1eaa381a-fbf9-4ef5-bec2-6e4337f85d66";
        List<Guid> stringList = GuidUtils.getGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<Guid>();
        expectedList.add(new Guid("e61f7070-cd52-46ca-88c2-686e1c70fe44"));
        expectedList.add(new Guid("1eaa381a-fbf9-4ef5-bec2-6e4337f85d66"));
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testGuidListValuesWithOneGuid() {
        String listValues = "e61f7070-cd52-46ca-88c2-686e1c70fe44";
        List<Guid> stringList = GuidUtils.getGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<Guid>();
        expectedList.add(new Guid("e61f7070-cd52-46ca-88c2-686e1c70fe44"));
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testEmptyGuidListValues() {
        String listValues = "";
        List<Guid> stringList = GuidUtils.getGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<Guid>();
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testNullGuidListValues() {
        List<Guid> stringList = GuidUtils.getGuidListFromString(null);
        List<Guid> expectedList = new ArrayList<Guid>();
        assertEquals(expectedList, stringList);
    }
}
