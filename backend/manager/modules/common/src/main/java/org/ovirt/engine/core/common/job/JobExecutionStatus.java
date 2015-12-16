package org.ovirt.engine.core.common.job;

/**
 * A representation of statues for {@link Job} and {@link Step} entities.
 */
public enum JobExecutionStatus {
    STARTED,
    FINISHED,
    FAILED,
    ABORTED,
    UNKNOWN;

}
