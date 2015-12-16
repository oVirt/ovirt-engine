package org.ovirt.engine.core.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Models a strongly-referenced primary hash map, coupled with a reapable secondary map based on soft-referenced values
 * (rather than keys, as is the case with the java.util.WeakHashMap).
 *
 * Use this like a normal hash map, but when values need no longer be retained they should be marked as reapable so that
 * they are made eligible for garbage collection. Entries are finally evicted if not already GC'd before the expiry of
 * the reapAfter timeout (calculated either from the point at which it was marked reapable or the last time of access).
 * Freeing of the entry prior to this timeout expiring is at the discretion of the garbage collector and depends on
 * whether the JVM is experiencing memory pressure, the type of JVM selected (the client JVM uses much more aggressive
 * GC policies that the server variant) and also the JVM options controlling the heap size.
 *
 * REVISIT: inherited entrySet() etc. don't take account of secondary
 *
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
public class ReapedMap<K, V> extends HashMap<K, V> {

    static final long serialVersionUID = 12345678987654321L;

    private static Long DEFAULT_REAP_AFTER = 10 * 60 * 1000L; // 10 minutes

    private long reapAfter;
    private boolean accessBasedAging;
    private ReferenceQueue<V> queue;

    // Secondary Map, note:
    // - keys are strongly referenced, as GC of corresponding values
    // will trigger their release
    // - reap requires a predictable iteration order (based on insertion order)
    // hence the use of LinkedHasMap
    //
    LinkedHashMap<K, IdAwareReference<K, V>> reapableMap;

    public ReapedMap() {
        this(DEFAULT_REAP_AFTER);
    }

    /**
     * @param reapAfter
     *            entries become eligible for reaping after this duration (ms)
     */
    public ReapedMap(long reapAfter) {
        this(reapAfter, false);
    }

    /**
     * @param reapAfter
     *            entries become eligible for reaping after this duration (ms)
     * @param accessBasedAging
     *            reset reapAfter timeout on each access
     */
    public ReapedMap(long reapAfter, boolean accessBasedAging) {
        this(reapAfter, accessBasedAging, new ReferenceQueue<>());
    }

    /**
     * Package-protected constructor intended for test use.
     *
     * @param reapAfter
     *            entries become eligible for reaping after this duration (ms)
     * @param accessBasedAging
     *            reset reapAfter timeout on each access
     * @param queue
     *            reference queue to avoid leaked mappings in case where aggressive GC eats referent before it is reaped
     */
    ReapedMap(long reapAfter, boolean accessBasedAging, ReferenceQueue<V> queue) {
        this.reapAfter = reapAfter;
        this.accessBasedAging = accessBasedAging;
        this.queue = queue;
        reapableMap = new LinkedHashMap<>();
    }

    @Override
    public synchronized V get(Object key) {
        V ret = super.get(key);
        if (ret == null) {
            IdAwareReference<K, V> ref = accessBasedAging ? reapableMap.remove(key) : reapableMap.get(key);
            if (ref != null) {
                if (ref.isEnqueued()) {
                    ref.clear();
                    reapableMap.remove(key);
                } else {
                    ret = ref.get();
                    if (ret == null) {
                        reapableMap.remove(key);
                    } else if (accessBasedAging) {
                        // re-insert on timestamp reset so
                        // as to maintain insertion order
                        reapableMap.put(ref.key, ref.reset());
                    }
                }
            }
        }
        reap();
        return ret;
    }

    @Override
    public synchronized V put(K k, V v) {
        reap();
        return super.put(k, v);
    }

    @Override
    public synchronized V remove(Object key) {
        V ret = super.remove(key);
        if (ret == null) {
            IdAwareReference<K, V> ref = reapableMap.remove(key);
            if (ref != null) {
                if (ref.isEnqueued()) {
                    ref.clear();
                } else {
                    ret = ref.get();
                }
            }
        }
        reap();
        return ret;
    }

    @Override
    public synchronized void clear() {
        super.clear();
        reapableMap.clear();
        while (queue.poll() != null) {
            // do nothing
        }
    }

    /**
     * Mark a key as being reapable, caching corresponding soft reference to corresponding value in the secondary map.
     */
    public synchronized void reapable(K k) {
        V v = super.remove(k);
        if (v != null) {
            reapableMap.put(k, new IdAwareReference<>(k, v, queue));
        }
        reap();
    }

    /**
     * @return the size of the secondary map
     */
    public synchronized long reapableSize() {
        return reapableMap.size();
    }

    /**
     * Reap <i>before</i> additive operations, <i>after</i> for neutral and destructive ones.
     */
    private synchronized void reap() {

        // reap entries older than age permitted
        //
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<K, IdAwareReference<K, V>>> entries = reapableMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<K, IdAwareReference<K, V>> entry = entries.next();
            IdAwareReference<K, V> v = entry.getValue();
            if (now - v.timestamp > reapAfter) {
                entries.remove();
                entry.getValue().clear();
                entry.setValue(null);
            } else {
                // guaranteed iteration on insertion order => no older entries
                //
                break;
            }
        }

        // poll reference queue for GC-pending references to trigger
        // reaping of referent
        //
        Object ref = null;
        while ((ref = queue.poll()) != null) {
            @SuppressWarnings("unchecked")
            IdAwareReference<K, V> value = (IdAwareReference<K, V>) ref;
            reapableMap.remove(value.getKey());
        }
    }

    /**
     * Encapsulate key and timestamp (the latter is used for eager reaping). The reference queue provides access to
     * finalizable instances of the reference type, not the class wrapping it. Hence we must extend SoftReference as
     * opposed to encapsulating it.
     */
    static class IdAwareReference<T, S> extends SoftReference<S> {
        long timestamp;
        T key;

        IdAwareReference(T key, S value, ReferenceQueue<S> queue) {
            super(value, queue);
            this.key = key;
            timestamp = System.currentTimeMillis();
        }

        public T getKey() {
            return key;
        }

        public boolean equals(Object other) {
            boolean ret = false;
            S one = null;
            ret = other == this
                    || (other instanceof SoftReference<?>
                            && (one = get()) != null
                      && one.equals(((SoftReference<?>) other).get()));
            return ret;
        }

        public int hashCode() {
            S one = get();
            return one != null ? one.hashCode() : 0;
        }

        private IdAwareReference<T, S> reset() {
            timestamp = System.currentTimeMillis();
            return this;
        }
    }
}
