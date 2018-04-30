package org.ovirt.engine.ui.common.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.ui.common.utils.AddOnlyRingBuffer.LinearBuffer;

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

    @BeforeEach
    public void setUp() {
        tested = new AddOnlyRingBuffer<>(CAPACITY, new StringArrayBuffer());
    }

    String[] testedListToArray() {
        return tested.list().toArray(new String[0]);
    }

    @Test
    public void bufferEmpty() {
        assertArrayEquals(testedListToArray(), new String[0]);
        assertEquals(0, tested.head());
        assertEquals(0, tested.size());
        assertTrue(tested.isEmpty());
        assertFalse(tested.isFull());
    }

    @Test
    public void bufferBelowCapacity() {
        assertNull(tested.add("A")); //$NON-NLS-1$
        assertNull(tested.add("B")); //$NON-NLS-1$
        assertArrayEquals(testedListToArray(), new String[] { "A", "B" }); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals(0, tested.head());
        assertEquals(2, tested.size());
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
        assertEquals(0, tested.head());
        assertEquals(5, tested.size());
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
        assertEquals(2, tested.head());
        assertEquals(5, tested.size());
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
        assertEquals(1, tested.head());
        assertEquals(2, tested.size());
        assertFalse(tested.isEmpty());
        assertFalse(tested.isFull());
    }

    @Test
    public void bufferReset_headLowerBound() {
        assertThrows(IllegalArgumentException.class, () -> tested.reset(-1, 2));
    }

    @Test
    public void bufferReset_headUpperBound() {
        assertThrows(IllegalArgumentException.class, () -> tested.reset(CAPACITY, 2));
    }

    @Test
    public void bufferReset_sizeLowerBound() {
        assertThrows(IllegalArgumentException.class, () -> tested.reset(1, -1));
    }

    @Test
    public void bufferReset_sizeUpperBound() {
        assertThrows(IllegalArgumentException.class, () -> tested.reset(1, CAPACITY + 1));
    }

    @Test
    public void constructorInvariants_capacityLowerBound() {
        assertThrows(IllegalArgumentException.class, () -> new AddOnlyRingBuffer<>(0, new StringArrayBuffer()));
    }

    @Test
    public void constructorInvariants_delegateNull() {
        assertThrows(NullPointerException.class, () -> new AddOnlyRingBuffer<>(CAPACITY, null));
    }

}
