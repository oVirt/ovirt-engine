package org.ovirt.engine.ui.uicompat;

public interface IFrontendQueryAsyncCallback {
	void OnSuccess(FrontendQueryAsyncResult result);
	void OnFailure(FrontendQueryAsyncResult result);
}
