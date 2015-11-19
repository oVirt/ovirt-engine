package org.ovirt.engine.api.extensions;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Type safe map.
 * Keys are bundle of uuid and type, each value added is checked against key
 * type.
 */
public class ExtMap implements ConcurrentMap<ExtKey, Object>, Cloneable, Serializable {

    private static final long serialVersionUID = 4065309872012801647L;

    /**
     * Wrapped map.
     * Wrapper and not inhertence per assumption of future
     * exposure if interface changes.
     */
    private ConcurrentMap<ExtKey, Object> map;

    /*
     * Object
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ExtMap)) {
            return false;
        }
        ExtMap other = (ExtMap) obj;
        return Objects.equals(map, other.map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExtMap clone() {
        return new ExtMap(map);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        boolean first = true;
        StringBuilder ret = new StringBuilder(1024);
        ret.append("{");
        for (Map.Entry<ExtKey, Object> entry : map.entrySet()) {
            if (first) {
                first = false;
            } else {
                ret.append(", ");
            }
            ret.append(entry.getKey());
            ret.append("=");
            if ((entry.getKey().getFlags() & ExtKey.Flags.SENSITIVE) != 0) {
                ret.append("***");
            } else if ((entry.getKey().getFlags() & ExtKey.Flags.SKIP_DUMP) != 0) {
                ret.append("*skip*");
            } else {
                ret.append(entry.getValue());
            }
        }
        ret.append("}");
        return ret.toString();
    }

    /*
     * Map Interface
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<ExtKey, Object>> entrySet() {
        return map.entrySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Object key) {
        return map.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<ExtKey> keySet() {
        return map.keySet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(ExtKey key, Object value) {
        if (value  == null) {
            return map.remove(key);
        }
        else {
            checkKeyValue(key, value);
            return map.put(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<? extends ExtKey, ? extends Object> m) {
        for (Map.Entry<? extends ExtKey, ? extends Object> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(Object key) {
        return map.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> values() {
        return map.values();
    }

    /*
     * ConcurrentMap interface
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public Object putIfAbsent(ExtKey key, Object value) {
        if (value == null) {
            return null;
        } else {
            checkKeyValue(key, value);
            return map.putIfAbsent(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object key, Object value) {
        return map.remove(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object replace(ExtKey key, Object value) {
        if (value == null) {
            return map.remove(key);
        } else {
            checkKeyValue(key, value);
            return map.replace(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean replace(ExtKey key, Object oldValue, Object newValue) {
        if (newValue == null) {
            return map.remove(key, oldValue);
        } else {
            checkKeyValue(key, newValue);
            return map.replace(key, oldValue, newValue);
        }
    }

    /*
     * ExtMap
     */

    /**
     * Constructs an empty ExtMap with the specified initial capacity and load factor.
     * @param initialCapacity the initial capacity.
     * @param loadFactor the load factor.
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor is nonpositive.
     *
     */
    public ExtMap(int initialCapacity, float loadFactor) {
        map = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Constructs an empty ExtMap with the specified initial capacity and the default load factor (0.75).
     * @param initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative or the load factor is nonpositive.
     */
    public ExtMap(int initialCapacity) {
        this(initialCapacity, (float)0.75);
    }

    /**
     * Constructs an empty ExtMap with the default initial capacity (16) and the default load factor (0.75).
     */
    public ExtMap() {
        this(16);
    }

    /**
     * Constructs a new ExtMap with the same mappings as the specified Map.
     * The ExtMap is created with default load factor (0.75) and an initial
     * capacity sufficient to hold the mappings in the specified Map.
     * @param m the map whose mappings are to be placed in this map.
     * @throws NullPointerException if the specified map is null.
     */
    public ExtMap(Map<ExtKey, Object> m) {
        this(m.size());
        map.putAll(m);
    }

    /**
     * Multiple put.
     * Usable for adding multiple entries:
     * <pre>{@code
     * ExtMap = new ExtMap().mput(key1, value1).mput(key2, value2);
     * }</pre>
     * @param key key with which the specified value is to be associated.
     * @param value value to be associated with the specified key.
     * @return this.
     */
    public ExtMap mput(ExtKey key, Object value) {
        put(key, value);
        return this;
    }

    /**
     * Multiple putAll.
     * Usable for adding multiple entries:
     * <pre>{@code
     * ExtMap = new ExtMap().mputAll(map1).mputAll(map2);
     * }</pre>
     * @param m map to add.
     * @return this.
     */
    public ExtMap mput(Map<? extends ExtKey, ? extends Object> m) {
        putAll(m);
        return this;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * Safe version of get().
     * <pre>{@code
     * Integer i = <Integer> map.get(key1, Integer);
     * }</pre>
     * @param key key with which the specified value is to be associated.
     * @param type expected type.
     * @param defaultValue default value to return.
     * @param <T> type of return and default value, inferred
     * @return Value.
     */
    public <T> T get(ExtKey key, Class<T> type, T defaultValue) {
        if (!key.getType().isAssignableFrom(type)) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannnot assign key '%s' into type '%s'",
                    key,
                    type
                )
            );
        }
        if (defaultValue != null && !key.getType().isAssignableFrom(defaultValue.getClass())) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannnot assign default value of '%s' into type '%s'",
                    defaultValue.getClass(),
                    key
                )
            );
        }

        T value = type.cast(map.get(key));
        if (value == null) {
            value = defaultValue;
        }

        return value;
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * Safe version of get().
     * @param key key with which the specified value is to be associated.
     * @param type expected type.
     * @param <T> type of return value, inferred
     * @return Value.
     * @see #get(ExtKey key, Class type, Object defaultValue)
     */
    public <T> T get(ExtKey key, Class<T> type) {
        return get(key, type, null);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * Unsafe method of get with cast.
     * <pre>{@code
     * Integer i = <Integer> map.get(key1);
     * }</pre>
     * @param key key.
     * @param <T> type of return value
     * @return Value.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ExtKey key) {
        return (T)map.get(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or default.
     * Unsafe method of get with cast.
     * <pre>{@code
     * Integer i = <Integer> map.get(key1, 5);
     * }</pre>
     * @param key key.
     * @param defaultValue default value.
     * @param <T> type of return and default value, inferred
     * @return Value.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(ExtKey key, Object defaultValue) {
        if (defaultValue != null && !key.getType().isAssignableFrom(defaultValue.getClass())) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannnot assign default value of '%s' into type '%s'",
                    defaultValue.getClass(),
                    key
                )
            );
        }
        Object value = get(key);
        if (value == null) {
            value = defaultValue;
        }
        return (T)value;
    }

    /**
     * Check if value matches key type.
     */
    private void checkKeyValue(ExtKey key, Object value) {
        if (!key.getType().isInstance(value)) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannnot assign type '%s' into key '%s'",
                    value.getClass(),
                    key
                )
            );
        }
    }
}
