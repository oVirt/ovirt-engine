/*
* Copyright (c) 2010 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.core.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.utils.ReapedMap.IdAwareReference;


public class ReapedMapTest extends Assert {

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
    public void testReapingOnGetWithGC() throws Exception {
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
    public void testReapingOnPutWithGC() throws Exception {
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
    public void testReapingOnRemoveWithGC() throws Exception {
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
            public Reference<Integer> answer(InvocationOnMock invocation) throws Throwable {
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
        assertEquals("unexpected primary map size", i, map.size());
        assertEquals("unexpected secondary map size", j, map.reapableSize());
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
