package org.ovirt.engine.core.common.utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

public class Pair<T, K> implements Serializable {
    private static final long serialVersionUID = -6761191157220811690L;

    private T first;
    private K second;

    public Pair() {
    }

    public Pair(T first, K second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Pair<T, K> original) {
        this.first = original.getFirst();
        this.second = original.getSecond();
    }

    public void setFirst(T value) {
        this.first = value;
    }

    public T getFirst() {
        return this.first;
    }

    public void setSecond(K value) {
        this.second = value;
    }

    public K getSecond() {
        return this.second;
    }

    @Override
    public String toString() {
        return "<" + first + ", " + second + '>';
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                hash(first),
                hash(second));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Pair)) {
            return false;
        }
        Pair<?, ?> other = (Pair<?, ?>) obj;

        return equals(first, other.first) && equals(second, other.second);
    }

    private boolean equals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }

        if (obj1 == null || obj2 == null) {
            return false;
        }

        if (obj1.getClass().isArray()) {
            return obj2.getClass().isArray() && Objects.deepEquals(obj1, obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    private int hash(Object obj) {
        if (obj != null && obj.getClass().isArray()) {
            return Arrays.hashCode((Object[]) obj);
        }
        return Objects.hashCode(obj);
    }
}
