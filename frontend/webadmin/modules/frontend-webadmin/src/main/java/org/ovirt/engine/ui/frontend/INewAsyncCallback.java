package org.ovirt.engine.ui.frontend;

import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;

public interface INewAsyncCallback {
	public void OnSuccess(Object model, Object returnValue);
}
