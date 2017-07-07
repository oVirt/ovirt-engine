package org.ovirt.engine.ui.frontend;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;

public interface IFrontendEventsHandler {
    Boolean isRaiseErrorModalPanel(ActionType action, EngineFault fault);

    Boolean isRaiseErrorModalPanel(QueryType queryType);

    void runActionExecutionFailed(ActionType action, EngineFault fault);

    void runMultipleActionFailed(ActionType action, List<ActionReturnValue> returnValues);

    void runMultipleActionsFailed(Map<ActionType, List<ActionReturnValue>> failedActionsMap, MessageFormatter messageFormatter);

    void runMultipleActionsFailed(List<ActionType> actions, List<ActionReturnValue> returnValues);

    void runQueryFailed(List<QueryReturnValue> returnValue);

    void publicConnectionClosed(Exception ex);

    interface MessageFormatter {
        String format(String message);
    }
}
