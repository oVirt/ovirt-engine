package org.ovirt.engine.ui.frontend;

public interface Converter<T> {
    T convert(Object source);
}
