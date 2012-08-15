package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public interface IVdsAsyncCommand {
    void rerun();

    void runningSucceded();

    boolean getAutoStart();

    Guid getAutoStartVdsId();

    /**
     * Assures the Job/Step are completed.
     */
    void reportCompleted();

    void onPowerringUp();
}
