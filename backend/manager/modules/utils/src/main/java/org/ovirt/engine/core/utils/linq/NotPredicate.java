package org.ovirt.engine.core.utils.linq;

import java.util.Objects;

final class NotPredicate<T> implements Predicate<T> {

    private final Predicate<T> predicate;

    NotPredicate(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate cannot be null");
        this.predicate = predicate;
    }

    @Override
    public boolean eval(T value) {
        return !predicate.eval(value);
    }
}
