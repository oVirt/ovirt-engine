package org.ovirt.engine.core.compat;

public enum CommandStatus {
    UNKNOWN,
    NOT_STARTED,
    ACTIVE, // the execute methods on command base has been invoked
    FAILED,
    FAILED_RESTARTED, // set by command executor to indicate that the command did not complete
                      // and the server was restarted
    SUCCEEDED,
    ENDED_SUCCESSFULLY, // indicates that the command endSuccessfully() was executed
    ENDED_WITH_FAILURE // indicates that the command endWithFailure() was executed
}
