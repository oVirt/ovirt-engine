package org.ovirt.engine.ui.uicompat;

public interface IAsyncCallback<T>
{
    void onSuccess(T result);
    void onFailure(T result);
}