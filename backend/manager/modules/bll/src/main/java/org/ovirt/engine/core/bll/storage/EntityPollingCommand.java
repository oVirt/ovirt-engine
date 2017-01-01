package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;

public interface EntityPollingCommand {
    HostJobStatus poll();

    /**
     * Method to perform job fencing using the command entity (if supported).
     */
    default void attemptToFenceJob() {}
}
