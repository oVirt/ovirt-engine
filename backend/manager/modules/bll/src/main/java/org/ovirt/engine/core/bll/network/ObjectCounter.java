package org.ovirt.engine.core.bll.network;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.iterators.UnmodifiableIterator;

class ObjectCounter<T> implements Iterable<T>{

    private Map<T, Counter> map = new HashMap<>();
    private final boolean allowDuplicate;

    ObjectCounter(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }

    public boolean add(T key) {
        return add(key, allowDuplicate);
    }

    public boolean add(T key, boolean allowDuplicate) {
        Counter counter = map.get(key);
        if (counter == null) {
            map.put(key, new Counter());
            return true;
        } else if (allowDuplicate) {
            counter.increment();
            return true;
        } else {
            return false;
        }
    }


    public void remove(T key) {
        Counter counter = map.get(key);
        if (counter == null) {
            return;
        }

        int count = counter.decrement();
        if (count == 0) {
            map.remove(key);
        } else if (count < 0) {
            throw new IllegalStateException("count underflow.");
        }
    }

    public boolean contains(T key) {
        return map.containsKey(key);
    }

    public int count(T key) {
        final Counter counter = map.get(key);
        return counter == null ? 0 : counter.toInt();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return UnmodifiableIterator.decorate(map.keySet().iterator());
    }

    private static class Counter {
        private int count;

        private Counter() {
            this(1);
        }

        public Counter(int initialValue) {
            setCount(initialValue);
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void increment() {
            count++;
        }

        public int decrement() {
            count--;
            return count;
        }

        public int toInt() {
            return count;
        }
    }
}
