package org.ovirt.engine.ui.frontend;

public interface IAsyncConverter<T> {
    public T convert(Object returnValue, AsyncQuery asyncQuery);
}
