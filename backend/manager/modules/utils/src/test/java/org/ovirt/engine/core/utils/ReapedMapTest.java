/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.utils.ReapedMap.IdAwareReference;


public class ReapedMapTest {

    private static final String[] NUMBERS = { "one", "two", "three", "four", "five" };

    private static final NumberMapper MAPPER = new NumberMapper();

    private ReapedMap<String, Integer> map;

    @Test
    public void testReapingWithoutGC() throws Exception {
        map = new ReapedMap<>(1000);
        populate(1, 2, 3);
        assertSizes(3, 0);
        assertExpected(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        map.reapable("one");
        assertSizes(1, 2);
        assertExpected(1, 2, 3);
        assertEquals(Integer.valueOf(3), map.remove("three"));
        assertSizes(1, 1);
        assertExpected(1, 2);
        assertNull(map.get("three"));
        Thread.sleep(1100);
        assertExpected(1);
        assertNull(map.get("one"));
        assertExpected(2);
        assertSizes(1, 0);
        populate(4, 5);
        map.clear();
    }

    @Test
    public void testReapingOnGetWithGC() {
        setUpGCExpectations(5);

        populate(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        assertExpected(3);
        assertSizes(1, 1);
        assertNull(map.get("three"));
    }

    @Test
    public void testReapingOnGetWithAccessBasedAging() throws Exception {
        setUpAccessBaseAgingExpectations();

        populate(1, 2, 3);
        assertSizes(3, 0);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        for (int i = 0; i < 6; i++) {
            Thread.sleep(250);
            assertExpected(i == 0 ? 1 : 3);
        }
        assertSizes(1, 1);
        assertNull(map.get("one"));
        assertExpected(3);
        Thread.sleep(500);
        assertExpected(3);
        assertSizes(1, 1);

        Thread.sleep(1200);
        assertExpected(2);
        assertSizes(1, 0);
        assertNull(map.get("three"));
    }

    @Test
    public void testReapingOnPutWithGC() {
        setUpGCExpectations(5);

        populate(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        populate(4);
        assertSizes(2, 1);
        assertNull(map.get("three"));
    }

    @Test
    public void testReapingOnRemoveWithGC() {
        setUpGCExpectations(5);

        populate(1, 2, 3);
        map.reapable("one");
        map.reapable("three");
        assertSizes(1, 2);
        map.remove("two");
        assertSizes(0, 1);
        assertNull(map.get("three"));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setUpGCExpectations(final int gcAfter) {
        ReferenceQueue<Integer> queue = mock(ReferenceQueue.class);
        map = new ReapedMap<>(10000, false, queue);

        final IdAwareReference ref = mock(IdAwareReference.class);
        when(ref.getKey()).thenReturn("three").thenReturn(null);

        // the gcAfter queue poll simulates a GC event and triggers deletion
        // on the reapable map
        when(queue.poll()).thenAnswer(new Answer<Reference<Integer>>() {
            private int times = 0;

            @Override
            public Reference<Integer> answer(InvocationOnMock invocation) {
                return times++ == gcAfter ? ref : null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setUpAccessBaseAgingExpectations() {
        ReferenceQueue<Integer> queue = mock(ReferenceQueue.class);
        map = new ReapedMap<>(1000, true, queue);
    }

    private void populate(Integer ... values) {
        for (Integer v: values) {
            map.put(MAPPER.getKey(v), v);
        }
    }

    private void assertSizes(int i, int j) {
        assertEquals(i, map.size(), "unexpected primary map size");
        assertEquals(j, map.reapableSize(), "unexpected secondary map size");
    }

    private void assertExpected(Integer ... values) {
        for (Integer v: values) {
            assertEquals(v, map.get(MAPPER.getKey(v)));
        }
    }

    private static class NumberMapper {
        public String getKey(Integer i) {
            return i <= NUMBERS.length ? NUMBERS[i-1] : null;
        }
    }
}
