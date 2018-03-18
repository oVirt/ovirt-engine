package org.ovirt.engine.core.bll.network.macpool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.iterators.UnmodifiableIterator;

/**
 * Associative array counting instances of given object.
 * @param <T> class of instances being count.
 */
class ObjectCounter<T> implements Iterable<T>{

    private Map<T, Counter> map = new HashMap<>();
    private final boolean allowDuplicate;

    ObjectCounter(boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
    }

    /**
     * add instance if possible, incrementing number of its occurrences.
     * @param key instance to add.
     * @return true if instance was added  && count incremented.
     */
    public boolean increase(T key) {
        return increase(key, allowDuplicate);
    }

    public boolean increase(T key, boolean allowDuplicate) {
        Counter counter = map.get(key);
        if (counter == null) {
            map.put(key, new Counter());
            return true;
        } else if (allowDuplicate) {
            counter.increase();
            return true;
        } else {
            return false;
        }
    }

    /**
     * decrements number of its occurrences, removing instance if possible(count reaches zero).
     *
     * @param key instance to remove.
     */
    public void decrease(T key) {
        Counter counter = map.get(key);
        if (counter == null) {
            return;
        }

        int count = counter.decrease();
        if (count == 0) {
            map.remove(key);
        } else if (count < 0) {
            throw new IllegalStateException("count underflow.");
        }
    }

    /**
     * @param key instance to look for
     * @return true if there's at least one occurrence of given instance.
     */
    public boolean contains(T key) {
        return map.containsKey(key);
    }

    /**
     * @param key instance to look for
     * @return number of occurrences of given instance. When instance was not added
     */
    public int count(T key) {
        final Counter counter = map.get(key);
        return counter == null ? 0 : counter.toInt();
    }

    public boolean containsDuplicates() {
        return map.values().stream().anyMatch(counter -> counter.toInt() > 1);
    }

    public boolean containsCounts() {
        return map.values().stream().anyMatch(counter -> counter.toInt() > 0);
    }

    /**
     * @return unmodifiable iterator over all 'registered' instances (i.e. all instances having count >= 0).
     */
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

        public void increase() {
            count++;
        }

        public int decrease() {
            count--;
            return count;
        }

        public int toInt() {
            return count;
        }
    }
}
