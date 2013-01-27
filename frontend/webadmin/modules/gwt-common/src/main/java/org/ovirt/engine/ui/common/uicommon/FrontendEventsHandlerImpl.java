package org.ovirt.engine.ui.common.uicommon;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.frontend.IFrontendEventsHandler;
import org.ovirt.engine.ui.frontend.Message;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.inject.Inject;

public class FrontendEventsHandlerImpl implements IFrontendEventsHandler {

    private final ErrorPopupManager errorPopupManager;
    private final CommonApplicationMessages messages;

    @Inject
    public FrontendEventsHandlerImpl(ErrorPopupManager errorPopupManager, CommonApplicationMessages messages) {
        this.errorPopupManager = errorPopupManager;
        this.messages = messages;
    }

    @Override
    public Boolean isRaiseErrorModalPanel(VdcActionType actionType, VdcFault fault) {
        return (actionType != VdcActionType.LoginUser) &&
               !(actionType == VdcActionType.VmLogon && fault.getError() == VdcBllErrors.nonresp);
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
        if (isRaiseErrorModalPanel(action, fault))
            errorPopupManager.show(messages.uiCommonRunActionExecutionFailed(
                    EnumTranslator.createAndTranslate(action), fault.getMessage()));
    }

    @Override
    public void runMultipleActionFailed(VdcActionType action,
            List<VdcReturnValueBase> returnValues, List<VdcFault> faults) {
        String actionStr = EnumTranslator.createAndTranslate(action);

        List<Message> errors = new ArrayList<Message>();

        for (VdcReturnValueBase v : returnValues) {
            if (isRaiseErrorModalPanel(action, v.getFault()))
            {
                for (String canDo : v.getCanDoActionMessages()) {
                    Message msg = new Message(v.getDescription(), canDo);
                    errors.add(msg);
                }
            }
        }

        for (VdcFault fault : faults) {
            if (isRaiseErrorModalPanel(action, fault))
            {
                Message msg = new Message(actionStr, fault.getMessage());
                errors.add(msg);
            }
        }

        errorPopupManager.show(ErrorMessageFormatter.formatMessages(errors));
    }

    @Override
    public void runQueryFailed(List<VdcQueryReturnValue> returnValue) {
        errorPopupManager.show(
                messages.uiCommonRunQueryFailed(
                        returnValue != null ? ErrorMessageFormatter.formatQueryReturnValues(returnValue) : "null")); //$NON-NLS-1$
    }

    @Override
    public void publicConnectionClosed(Exception ex) {
        errorPopupManager.show(
                messages.uiCommonPublicConnectionClosed(ex.getLocalizedMessage()));
    }

}
