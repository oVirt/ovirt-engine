package org.ovirt.engine.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class PairTest {

    @Test
    public void testHashCode() {
        Map<String, Pair<Boolean, String>> m = new HashMap<>();

        Pair<Boolean, String> p1 = new Pair<>(true, "abc");
        Pair<Boolean, String> p2 = new Pair<>(true, "abc");
        m.put("test", p1);

        assertTrue(m.containsValue(p1));
        assertTrue(m.containsValue(p2));
    }

    @Test
    public void testEquals() {
        Pair<Boolean, String> p1 = new Pair<>(true, "abc");
        Pair<Boolean, String> p2 = new Pair<>(true, "abc");
        Pair<Boolean, String> p3 = new Pair<>(false, "abc");

        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
    }

    @Test
    public void testArraysEquality() {
        Pair<Boolean, String[]> p1 = new Pair<>(true, new String[] { "abc", "def" });
        Pair<Boolean, String[]> p2 = new Pair<>(true, new String[] { "abc", "def" });
        Pair<Boolean, String[]> p3 = new Pair<>(false, new String[] { "abc", "xyz" });

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p1, p3);
    }

    @Test
    public void testEqualityOfNullableArguments() {
        Pair<Boolean, String> p1 = new Pair<>(true, null);
        Pair<Boolean, String> p2 = new Pair<>(true, null);
        Pair<Boolean, String> p3 = new Pair<>(null, null);
        Pair<Boolean, String> p4 = new Pair<>(null, null);
        Pair<Boolean, String> p5 = new Pair<>(null, "abc");
        Pair<Boolean, String> p6 = new Pair<>(null, "abc");

        assertEquals(p1, p2);
        assertEquals(p1.hashCode(), p2.hashCode());
        assertNotEquals(p2, p3);
        assertEquals(p3, p4);
        assertEquals(p3.hashCode(), p4.hashCode());
        assertEquals(p5, p6);
        assertEquals(p5.hashCode(), p6.hashCode());
    }
}
