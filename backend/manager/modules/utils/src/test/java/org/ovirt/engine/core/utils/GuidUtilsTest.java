package org.ovirt.engine.core.utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

public class GuidUtilsTest {
    @Test
    public void testGuidListValues() {
        String listValues = "e61f7070-cd52-46ca-88c2-686e1c70fe44,1eaa381a-fbf9-4ef5-bec2-6e4337f85d66";
        List<Guid> stringList = GuidUtils.getGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<>();
        expectedList.add(new Guid("e61f7070-cd52-46ca-88c2-686e1c70fe44"));
        expectedList.add(new Guid("1eaa381a-fbf9-4ef5-bec2-6e4337f85d66"));
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testGuidListValuesWithOneGuid() {
        String listValues = "e61f7070-cd52-46ca-88c2-686e1c70fe44";
        List<Guid> stringList = GuidUtils.getGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<>();
        expectedList.add(new Guid("e61f7070-cd52-46ca-88c2-686e1c70fe44"));
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testEmptyGuidListValues() {
        String listValues = "";
        List<Guid> stringList = GuidUtils.getGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<>();
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testNullGuidListValues() {
        List<Guid> stringList = GuidUtils.getGuidListFromString(null);
        List<Guid> expectedList = new ArrayList<>();
        assertEquals(expectedList, stringList);
    }
}
