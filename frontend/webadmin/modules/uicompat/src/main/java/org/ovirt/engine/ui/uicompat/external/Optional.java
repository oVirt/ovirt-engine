package org.ovirt.engine.ui.uicompat.external;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Lightweight imitation of Java 8 java.util.Optional
 */
public class Optional<T> {

    private static final Optional<?> EMPTY = new Optional<>();

    private final T value;

    private Optional() {
        this.value = null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> empty() {
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }


    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    public static <T> Optional<T> ofNullable(T value) {
        if (value == null) {
            return empty();
        }
        return of(value);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Optional<?> optional = (Optional<?>) o;
        return Objects.equals(value, optional.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return isPresent()
               ? ("Optional{" + value + '}')
               : "Optional.empty";
    }
}
