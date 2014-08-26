package org.ovirt.engine.core.utils.linq;

public class All<T> implements Predicate<T> {
    @Override
    public boolean eval(T t) {
        return true;
    }
}
