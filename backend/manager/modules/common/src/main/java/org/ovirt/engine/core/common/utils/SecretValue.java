package org.ovirt.engine.core.common.utils;

import java.io.Serializable;
import java.util.Objects;

public class SecretValue<T> implements Serializable {
    private static final long serialVersionUID = -7894728002078425194L;

    private T value;

    public T getValue() {
        return value;
    }

    public SecretValue(T value) {
        this.value = value;
    }

    private SecretValue() {
    }

    public String toString() {
        return "***";
    }

    /**
     * @return Whether {@code secret} or its value is {@code null}.
     */
    public static boolean isNull(SecretValue<?> secret) {
        return secret == null || secret.getValue() == null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SecretValue)) {
            return false;
        }
        return Objects.equals(value, ((SecretValue<?>) obj).getValue());
    }
}
