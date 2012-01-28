package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public interface IVdsAsyncCommand {
    void Rerun();

    void RunningSucceded();

    boolean getAutoStart();

    Guid getAutoStartVdsId();

    /**
     * Assures the Job/Step are completed.
     */
    void reportCompleted();
}
