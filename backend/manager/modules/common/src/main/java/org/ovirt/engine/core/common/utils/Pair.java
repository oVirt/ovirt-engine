package org.ovirt.engine.core.common.utils;

import java.io.Serializable;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (first == null) {
            if (other.first != null)
                return false;
        } else if (!first.equals(other.first))
            return false;
        if (second == null) {
            if (other.second != null)
                return false;
        } else if (!second.equals(other.second))
            return false;
        return true;
    }
}
