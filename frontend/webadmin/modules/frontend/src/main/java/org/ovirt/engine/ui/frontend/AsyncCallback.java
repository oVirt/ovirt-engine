package org.ovirt.engine.ui.frontend;

public interface AsyncCallback<T> {
    void onSuccess(T returnValue);
}
