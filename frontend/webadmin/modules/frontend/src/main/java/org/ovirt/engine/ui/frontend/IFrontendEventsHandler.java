package org.ovirt.engine.ui.frontend;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public interface IFrontendEventsHandler {
    Boolean isRaiseErrorModalPanel(VdcActionType action, EngineFault fault);

    Boolean isRaiseErrorModalPanel(VdcQueryType queryType);

    void runActionExecutionFailed(VdcActionType action, EngineFault fault);

    void runMultipleActionFailed(VdcActionType action, List<VdcReturnValueBase> returnValues);

    void runMultipleActionsFailed(Map<VdcActionType, List<VdcReturnValueBase>> failedActionsMap, MessageFormatter messageFormatter);

    void runMultipleActionsFailed(List<VdcActionType> actions, List<VdcReturnValueBase> returnValues);

    void runQueryFailed(List<VdcQueryReturnValue> returnValue);

    void publicConnectionClosed(Exception ex);

    interface MessageFormatter {
        String format(String message);
    }
}
