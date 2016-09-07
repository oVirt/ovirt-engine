package org.ovirt.engine.core.common.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

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

        assertTrue(p1.equals(p2));
        assertFalse(p1.equals(p3));
    }
}
