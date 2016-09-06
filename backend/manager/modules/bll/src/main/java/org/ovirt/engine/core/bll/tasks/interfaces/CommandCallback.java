package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public interface CommandCallback {

    default void doPolling(Guid cmdId, List<Guid> childCmdIds) {
    }

    default void onFailed(Guid cmdId, List<Guid> childCmdIds) {
    }

    default void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
    }

    default void onEvent(Guid cmdId, List<Guid> childCmdIds, Object eventData) {
    }

    default boolean isTriggeredByEvent() {
        return false;
    }

    /**
     * This method is used to indicate whether the callback doPolling()
     * method should be called when the command status is EXECUTION_FAILED
     * This method is temporary and should be removed when all callbacks will
     * have unified status handling.
     */
    //TODO: remove when callbacks parent classes and status handling is unified
    default boolean pollOnExecutionFailed() {
        return false;
    }

    default boolean shouldRepeatEndMethodsOnFail(Guid cmdId) {
        return false;
    }
}
