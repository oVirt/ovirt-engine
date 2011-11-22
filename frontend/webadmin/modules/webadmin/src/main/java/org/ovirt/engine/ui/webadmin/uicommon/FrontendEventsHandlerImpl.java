package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.IFrontendEventsHandler;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.system.ErrorPopupManager;

import com.google.inject.Inject;

public class FrontendEventsHandlerImpl implements IFrontendEventsHandler {

    private final ErrorPopupManager errorPopupManager;
    private final ApplicationMessages messages;

    @Inject
    public FrontendEventsHandlerImpl(ErrorPopupManager errorPopupManager, ApplicationMessages messages) {
        this.errorPopupManager = errorPopupManager;
        this.messages = messages;
    }

    @Override
    public Boolean isRaiseErrorModalPanel(VdcActionType actionType) {
        return actionType != VdcActionType.LoginUser;
    }

    @Override
    public Boolean isRaiseErrorModalPanel(VdcQueryType queryType) {
        return false;
    }

    @Override
    public void runActionFailed(List<VdcReturnValueBase> returnValues) {
        errorPopupManager.show(
                messages.uiCommonRunActionFailed(ErrorMessageFormatter.formatReturnValues(returnValues)));
    }

    @Override
    public void runActionExecutionFailed(VdcActionType action, VdcFault fault) {
        errorPopupManager.show(
                messages.uiCommonRunActionExecutionFailed(action.toString(), fault.getMessage()));
    }

    @Override
    public void runQueryFailed(List<VdcQueryReturnValue> returnValue) {
        errorPopupManager.show(
                messages.uiCommonRunQueryFailed(
                        returnValue != null ? ErrorMessageFormatter.formatQueryReturnValues(returnValue) : "null"));
    }

    @Override
    public void publicConnectionClosed(Exception ex) {
        errorPopupManager.show(
                messages.uiCommonPublicConnectionClosed(ex.getLocalizedMessage()));
    }

}
