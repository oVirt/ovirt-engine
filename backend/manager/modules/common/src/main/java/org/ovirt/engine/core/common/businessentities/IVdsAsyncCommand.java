package org.ovirt.engine.core.common.businessentities;


public interface IVdsAsyncCommand {
    void rerun();

    void runningSucceded();

    /**
     * Assures the Job/Step are completed.
     */
    void reportCompleted();

    void onPowerringUp();
}
