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
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.frontend.IFrontendEventsHandler;
import org.ovirt.engine.ui.frontend.Message;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.inject.Inject;

public class FrontendEventsHandlerImpl implements IFrontendEventsHandler {

    private final ErrorPopupManager errorPopupManager;
    private final static CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public FrontendEventsHandlerImpl(ErrorPopupManager errorPopupManager) {
        this.errorPopupManager = errorPopupManager;
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
    public void runActionExecutionFailed(VdcActionType action, VdcFault fault) {
        if (isRaiseErrorModalPanel(action, fault)) {
            errorPopupManager.show(messages.uiCommonRunActionExecutionFailed(
                    EnumTranslator.getInstance().translate(action), fault.getMessage()));
        }
    }

    @Override
    public void runMultipleActionFailed(VdcActionType action, List<VdcReturnValueBase> returnValues) {
        List<VdcActionType> actions = new ArrayList<VdcActionType>();
        for (int i = 0; i < returnValues.size(); i++) {
            actions.add(action);
        }

        runMultipleActionsFailed(actions, returnValues);
    }

    @Override
    public void runMultipleActionsFailed(List<VdcActionType> actions, List<VdcReturnValueBase> returnValues) {

        List<Message> errors = new ArrayList<Message>();

        int actionNum = 0;
        for (VdcReturnValueBase v : returnValues) {
            if (isRaiseErrorModalPanel(actions.get(actionNum++), v.getFault())) {
                String description =
                        (v.getDescription() != null && !"".equals(v.getDescription().trim())) || returnValues.size() == 1 ? v.getDescription() : ConstantsManager.getInstance().getConstants().action() + " " + actionNum; //$NON-NLS-1$ //$NON-NLS-2$
                if (!v.getCanDoAction()) {
                    for (String canDo : v.getCanDoActionMessages()) {
                        errors.add(new Message(description, canDo));
                    }
                } else {
                    errors.add(new Message(description, v.getFault().getMessage()));
                }
            }
        }

        errorPopupManager.show(messages.uiCommonRunActionFailed(ErrorMessageFormatter.formatMessages(errors)));
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
