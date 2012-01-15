package org.ovirt.engine.ui.frontend;

public interface IAsyncConverter {
    public Object Convert(Object returnValue, AsyncQuery asyncQuery);
}
