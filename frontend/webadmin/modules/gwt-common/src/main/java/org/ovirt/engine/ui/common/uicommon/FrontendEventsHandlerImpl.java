package org.ovirt.engine.ui.common.uicommon;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.frontend.IFrontendEventsHandler;
import org.ovirt.engine.ui.uicompat.EnumTranslator;
import org.ovirt.engine.ui.uicompat.Translator;

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
        Translator vdcActionTypeTranslator = EnumTranslator.Create(VdcActionType.class);

        errorPopupManager.show(
                messages.uiCommonRunActionExecutionFailed(vdcActionTypeTranslator.containsKey(action) ? vdcActionTypeTranslator.get(action)
                        : action.toString(), fault.getMessage()));
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
