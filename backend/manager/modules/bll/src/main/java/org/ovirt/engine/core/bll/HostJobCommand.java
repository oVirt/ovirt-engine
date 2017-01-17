package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.HostJobInfo.HostJobStatus;
import org.ovirt.engine.core.common.errors.EngineError;

public interface HostJobCommand {

    /**
     * This method lets the command to inspect the job error and return a different status for the job according to the
     * error
     */
    HostJobStatus handleJobError(EngineError error);

    /**
     * This methods let the command to fail immediately when the job status is unknown or couldn't be determined.
     * Useful in cases in which we don't want to wait and we don't care to fail the operation and let the user to retry.
     */
    boolean failJobWithUndeterminedStatus();

}
