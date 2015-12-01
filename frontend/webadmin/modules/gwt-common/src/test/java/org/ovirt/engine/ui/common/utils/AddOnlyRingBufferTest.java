package org.ovirt.engine.ui.common.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.ui.common.utils.AddOnlyRingBuffer.LinearBuffer;

@RunWith(MockitoJUnitRunner.class)
public class AddOnlyRingBufferTest {

    private class StringArrayBuffer implements LinearBuffer<String> {

        private final String[] array = new String[CAPACITY];

        @Override
        public void write(int index, String element) {
            array[index] = element;
        }

        @Override
        public String read(int index) {
            return array[index];
        }

    }

    private static final int CAPACITY = 5;

    private AddOnlyRingBuffer<String> tested;

    @Before
    public void setUp() {
        tested = new AddOnlyRingBuffer<>(CAPACITY, new StringArrayBuffer());
    }

    String[] testedListToArray() {
        return tested.list().toArray(new String[0]);
    }

    @Test
    public void bufferEmpty() {
        assertArrayEquals(testedListToArray(), new String[0]);
        assertEquals(tested.head(), 0);
        assertEquals(tested.size(), 0);
        assertTrue(tested.isEmpty());
        assertFalse(tested.isFull());
    }

    @Test
    public void bufferBelowCapacity() {
        assertNull(tested.add("A")); //$NON-NLS-1$
        assertNull(tested.add("B")); //$NON-NLS-1$
        assertArrayEquals(testedListToArray(), new String[] { "A", "B" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(tested.head(), 0);
        assertEquals(tested.size(), 2);
        assertFalse(tested.isEmpty());
        assertFalse(tested.isFull());
    }

    @Test
    public void bufferAtCapacity() {
        assertNull(tested.add("A")); //$NON-NLS-1$
        assertNull(tested.add("B")); //$NON-NLS-1$
        assertNull(tested.add("C")); //$NON-NLS-1$
        assertNull(tested.add("D")); //$NON-NLS-1$
        assertNull(tested.add("E")); //$NON-NLS-1$
        assertArrayEquals(testedListToArray(), new String[] { "A", "B", "C", "D", "E" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        assertEquals(tested.head(), 0);
        assertEquals(tested.size(), 5);
        assertFalse(tested.isEmpty());
        assertTrue(tested.isFull());
    }

    @Test
    public void bufferBeyondCapacity() {
        assertNull(tested.add("A")); //$NON-NLS-1$
        assertNull(tested.add("B")); //$NON-NLS-1$
        assertNull(tested.add("C")); //$NON-NLS-1$
        assertNull(tested.add("D")); //$NON-NLS-1$
        assertNull(tested.add("E")); //$NON-NLS-1$
        assertEquals("A", tested.add("F")); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("B", tested.add("G")); //$NON-NLS-1$ //$NON-NLS-2$
        assertArrayEquals(testedListToArray(), new String[] { "C", "D", "E", "F", "G" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        assertEquals(tested.head(), 2);
        assertEquals(tested.size(), 5);
        assertFalse(tested.isEmpty());
        assertTrue(tested.isFull());
    }

    @Test
    public void bufferReset() {
        assertNull(tested.add("A")); //$NON-NLS-1$
        assertNull(tested.add("B")); //$NON-NLS-1$
        assertNull(tested.add("C")); //$NON-NLS-1$
        assertNull(tested.add("D")); //$NON-NLS-1$
        tested.reset(1, 2);
        assertArrayEquals(testedListToArray(), new String[] { "B", "C" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(tested.head(), 1);
        assertEquals(tested.size(), 2);
        assertFalse(tested.isEmpty());
        assertFalse(tested.isFull());
    }

    @Test(expected = IllegalArgumentException.class)
    public void bufferReset_headLowerBound() {
        tested.reset(-1, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bufferReset_headUpperBound() {
        tested.reset(CAPACITY, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bufferReset_sizeLowerBound() {
        tested.reset(1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bufferReset_sizeUpperBound() {
        tested.reset(1, CAPACITY + 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorInvariants_capacityLowerBound() {
        new AddOnlyRingBuffer<>(0, new StringArrayBuffer());
    }

    @Test(expected = NullPointerException.class)
    public void constructorInvariants_delegateNull() {
        new AddOnlyRingBuffer<String>(CAPACITY, null);
    }

}
