package org.ovirt.engine.core.compat;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class KeyValuePairCompat<K, V> implements Map.Entry<K, V>, Serializable {

    private static final long serialVersionUID = 3550666497489591122L;

    private K key;
    private V value;

    public KeyValuePairCompat() {
    }

    public KeyValuePairCompat(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V oldV = value;
        this.value = value;
        return oldV;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                key,
                value
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyValuePairCompat)) {
            return false;
        }
        KeyValuePairCompat other = (KeyValuePairCompat) obj;
        return Objects.equals(key, other.key)
                && Objects.equals(value, other.value);
    }

    @Override
    public final String toString() {
        return key + "=" + value;
    }

}
