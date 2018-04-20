package org.ovirt.engine.core.utils.collections;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map decorator for providing copies of keys and values upon invocation of accessor methods. With this decorator the
 * caller will be able to safely update the retrieved objects without worrying that the objects held by the map are
 * altered (as he will get copies and not references to the objects held by the map).
 */
public class CopyOnAccessMap<K, V> implements Map<K, V> {

    private static final int REALLOCATION_FACTOR = 2;
    private Map<K, V> innerMap;

    public CopyOnAccessMap(Map<K, V> innerMap) {
        this.innerMap = innerMap;
    }

    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return innerMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return innerMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return clone(innerMap.get(key));
    }

    @SuppressWarnings("unchecked")
    private <O> O clone(O originalKey) {
        // We use an intermediate buffer to hold the serialized form of the
        // object:
        byte[] buffer = null;

        // Serialize the object to an array of bytes:
        ByteArrayOutputStream bufferOut = null;
        ObjectOutputStream objectOut = null;
        try {
            bufferOut = new ByteArrayOutputStream(512);
            objectOut = new ObjectOutputStream(bufferOut);
            objectOut.writeObject(originalKey);
            buffer = bufferOut.toByteArray();
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException exception) {
                    // Ignored.
                }
            }
        }

        // Create a new instance of the object deserializing it from the
        // buffer:
        ByteArrayInputStream bufferIn = null;
        ObjectInputStream objectIn = null;
        try {
            bufferIn = new ByteArrayInputStream(buffer);
            objectIn = new ObjectInputStream(bufferIn) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass description) throws IOException, ClassNotFoundException {
                    // First try with the context class loader, if that fails
                    // then just call the overridden method:
                    try {
                        return Class.forName(description.getName(), false, Thread.currentThread().getContextClassLoader());
                    } catch (ClassNotFoundException exception) {
                        return super.resolveClass(description);
                    }
                }
            };
            return (O) objectIn.readObject();
        } catch(IOException | ClassNotFoundException exception) {
            throw new RuntimeException(exception);
        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException exception) {
                    // Ignored.
                }
            }
        }
    }

    @Override
    public V put(K key, V value) {
        //The old value is no longer in the map, so no need to protect it from external modifiers, so it is not cloned
        return innerMap.put(clone(key), clone(value));
    }

    @Override
    public V remove(Object key) {
        return innerMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        innerMap.clear();
    }

    @Override
    public Set<K> keySet() {
        Set<K> newSet = new HashSet<>(innerMap.size() * REALLOCATION_FACTOR);
        for (K key : innerMap.keySet()) {
            newSet.add(clone(key));
        }
        return newSet;
    }

    @Override
    public Collection<V> values() {
        List<V> newValues = new ArrayList<>(innerMap.size() * REALLOCATION_FACTOR);
        for (V value : innerMap.values()) {
            newValues.add(clone(value));
        }
        return newValues;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Map<K, V> newHashMap = new HashMap<>(innerMap.size() * REALLOCATION_FACTOR);
        for (Entry<K, V> entry : innerMap.entrySet()) {
            newHashMap.put(clone(entry.getKey()), clone(entry.getValue()));
        }
        return newHashMap.entrySet();
    }

}
