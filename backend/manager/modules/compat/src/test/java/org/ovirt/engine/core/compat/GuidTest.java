package org.ovirt.engine.core.compat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Tests Guid functionality
 */
public class GuidTest {

    @Test
    public void testCompareTo() {
        Guid guid1 = new Guid("5b411bc1-c220-4421-9abd-cfa484aecb6e");
        Guid guid2 = new Guid("5b411bc1-c220-4421-9abd-cfa484aecb6f");
        assertTrue(guid1.compareTo(guid2) < 0);
        assertEquals(0, guid1.compareTo(guid1));
        assertTrue(guid2.compareTo(guid1) > 0);
    }

    @Test
    public void testStringCreation() {
        Guid guid = Guid.newGuid();
        Guid guidFromString = new Guid(guid.toString());
        assertEquals(guidFromString, guid);

        guidFromString = Guid.createGuidFromString(guid.toString());
        assertEquals(guidFromString, guid);
        guidFromString = Guid.createGuidFromString(null);
        assertNull(guidFromString);

        guidFromString = Guid.createGuidFromStringDefaultEmpty(guid.toString());
        assertEquals(guidFromString, guid);
        guidFromString = Guid.createGuidFromStringDefaultEmpty(null);
        assertEquals(Guid.Empty, guidFromString);
    }

    @Test
    public void testToByteArray() {
        final byte[] byteArray = Guid.newGuid().toByteArray();
        assertNotNull(byteArray);
        assertEquals(16, byteArray.length);
    }

    @Test
    public void testToByteArrayAllNull() {
        final byte[] allNullArray = Guid.Empty.toByteArray();
        assertNotNull(allNullArray);
        for (int i = 0; i < 16; i++) {
            assertEquals(0, allNullArray[i]);
        }
    }

    @Test
    public void testGuidListValues() {
        String listValues = "e61f7070-cd52-46ca-88c2-686e1c70fe44,1eaa381a-fbf9-4ef5-bec2-6e4337f85d66";
        List<Guid> stringList = Guid.createGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<>();
        expectedList.add(new Guid("e61f7070-cd52-46ca-88c2-686e1c70fe44"));
        expectedList.add(new Guid("1eaa381a-fbf9-4ef5-bec2-6e4337f85d66"));
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testGuidListValuesWithOneGuid() {
        String listValues = "e61f7070-cd52-46ca-88c2-686e1c70fe44";
        List<Guid> stringList = Guid.createGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<>();
        expectedList.add(new Guid("e61f7070-cd52-46ca-88c2-686e1c70fe44"));
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testEmptyGuidListValues() {
        String listValues = "";
        List<Guid> stringList = Guid.createGuidListFromString(listValues);
        List<Guid> expectedList = new ArrayList<>();
        assertEquals(expectedList, stringList);
    }

    @Test
    public void testNullGuidListValues() {
        List<Guid> stringList = Guid.createGuidListFromString(null);
        List<Guid> expectedList = new ArrayList<>();
        assertEquals(expectedList, stringList);
    }
}
