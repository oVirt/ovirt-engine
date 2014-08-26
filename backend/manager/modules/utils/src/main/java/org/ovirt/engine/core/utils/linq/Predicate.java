package org.ovirt.engine.core.utils.linq;

public interface Predicate<T> {
    boolean eval(T t);
}
