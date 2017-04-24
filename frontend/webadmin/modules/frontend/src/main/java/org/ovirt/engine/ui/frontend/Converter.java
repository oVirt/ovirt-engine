package org.ovirt.engine.ui.frontend;

public interface Converter<T, S> {
    T convert(S source);
}
