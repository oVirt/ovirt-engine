package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.businessentities.CommandEntity;

public interface CommandCoordinator extends TaskHelper, AsyncCommandCallback, CommandCRUDOperations, AsyncTaskCRUDOperations, CommandScheduler {

    /**
     * Subscribes the given command for an event by its given event key
     *
     * @param eventKey
     *            the event key to subscribe
     * @param commandEntity
     *            the subscribed command, which its callback will be invoked upon event
     */
    void subscribe(String eventKey, CommandEntity commandEntity);
}
