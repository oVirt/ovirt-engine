package org.ovirt.engine.ui.uicompat;

public interface IAsyncCallback<T>
{
	void OnSuccess(T result);
	void OnFailure(T result);
}