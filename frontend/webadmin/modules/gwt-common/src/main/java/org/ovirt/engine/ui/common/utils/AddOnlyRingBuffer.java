package org.ovirt.engine.ui.common.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple ring buffer implementation that only permits adding new elements.
 * <p>
 * A ring buffer is <em>First In, First Out</em> (FIFO) data structure with fixed capacity.
 * When adding new element into full buffer, the oldest element is replaced with newly added
 * element. Since elements are removed in same order in which they were added, there's no need
 * to re-arrange elements within the underlying (linear buffer) data representation.
 *
 * @param <T>
 *            Type of element managed by the buffer.
 */
public class AddOnlyRingBuffer<T> {

    /**
     * Delegate interface representing a linear buffer to store the actual data.
     *
     * @param <T>
     *            Type of element managed by the buffer.
     */
    public interface LinearBuffer<T> {

        /**
         * Write an element at given index.
         */
        void write(int index, T element);

        /**
         * Read an element at given index.
         */
        T read(int index);

    }

    private final int capacity;
    private final LinearBuffer<T> delegate;

    private int head = 0;
    private int size = 0;

    public AddOnlyRingBuffer(int capacity, LinearBuffer<T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("delegate cannot be null"); //$NON-NLS-1$
        } else if (capacity < 1) {
            throw new IllegalArgumentException("capacity must be positive"); //$NON-NLS-1$
        }

        this.capacity = capacity;
        this.delegate = delegate;
    }

    /**
     * Adds the given element to the ring buffer.
     * <p>
     * Performs in constant time O(1).
     *
     * @return Oldest element removed due to buffer being full, {@code null} otherwise.
     */
    public T add(T element) {
        T old = null;

        // Buffer not full yet, don't move the head
        if (size < capacity) {
            int index = head + size;
            size += 1;
            delegate.write(index, element);
        } else {
            // Buffer is full, need to move the head
            int index = head;
            head = (head + 1) % capacity;
            old = delegate.read(index);
            delegate.write(index, element);
        }

        return old;
    }

    /**
     * Returns the list of elements present in the ring buffer.
     * <p>
     * Performs in linear time O(n).
     */
    public List<T> list() {
        List<T> result = new ArrayList<>(size);

        for (int offset = 0; offset < size; offset += 1) {
            int index = (head + offset) % capacity;
            result.add(delegate.read(index));
        }

        return result;
    }

    /**
     * Returns the head index pointing to oldest element in the ring buffer.
     */
    public int head() {
        return head;
    }

    /**
     * Returns the number of elements present in the ring buffer.
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if the ring buffer size is zero.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns {@code true} if the ring buffer reached its capacity.
     */
    public boolean isFull() {
        return size() == capacity;
    }

    /**
     * Resets the state of the ring buffer.
     */
    public void reset(int newHead, int newSize) {
        // 0 <= newHead < capacity
        if (newHead < 0 || newHead >= capacity) {
            throw new IllegalArgumentException("newHead out of bounds"); //$NON-NLS-1$
        } else if (newSize < 0 || newSize > capacity) {
            // 0 <= newSize <= capacity
            throw new IllegalArgumentException("newSize out of bounds"); //$NON-NLS-1$
        }

        this.head = newHead;
        this.size = newSize;
    }

}
