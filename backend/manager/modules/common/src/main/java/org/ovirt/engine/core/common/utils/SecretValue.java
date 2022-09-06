package org.ovirt.engine.core.common.utils;

public class SecretValue<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public SecretValue(T value) {
        this.value = value;
    }

    public String toString() {
        return "***";
    }
}
