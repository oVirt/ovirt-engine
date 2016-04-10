package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public abstract class CommandCallback {

    public CommandCallback() {}

    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
    }

    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
    }

    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
    }

    public void onEvent(Guid cmdId, List<Guid> childCmdIds, Object eventData) {
    }

    public boolean isTriggeredByEvent() {
        return false;
    }

    /**
     * This method is used to indicate whether the callback doPolling()
     * method should be called when the command status is EXECUTION_FAILED
     * This method is temporary and should be removed when all callbacks will
     * have unified status handling.
     */
    //TODO: remove when callbacks parent classes and status handling is unified
    public boolean pollOnExecutionFailed() {
        return false;
    }

    public boolean shouldRepeatEndMethodsOnFail(Guid cmdId) {
        return false;
    }
}
