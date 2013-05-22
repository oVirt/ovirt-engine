package org.ovirt.engine.ui.frontend;

public interface IAsyncConverter<T> {
    public T Convert(Object returnValue, AsyncQuery asyncQuery);
}
