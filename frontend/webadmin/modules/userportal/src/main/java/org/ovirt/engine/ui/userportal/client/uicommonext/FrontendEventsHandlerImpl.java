package org.ovirt.engine.ui.userportal.client.uicommonext;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.IFrontendEventsHandler;

public class FrontendEventsHandlerImpl implements IFrontendEventsHandler {

	@Override
	public Boolean isRaiseErrorModalPanel(VdcActionType actionType) {
		if (actionType == VdcActionType.LoginUser) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public Boolean isRaiseErrorModalPanel(VdcQueryType queryType) {
		return true;
	}

	@Override
	public void runActionFailed(List<VdcReturnValueBase> returnValues) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runActionExecutionFailed(VdcActionType action, VdcFault fault) {
		// TODO Auto-generated method stub

	}

	@Override
	public void runQueryFailed(List<VdcQueryReturnValue> returnValue) {
		// TODO Auto-generated method stub

	}

	@Override
	public void publicConnectionClosed(Exception ex) {
		// TODO Auto-generated method stub

	}
}
