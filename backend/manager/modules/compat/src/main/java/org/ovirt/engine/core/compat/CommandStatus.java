package org.ovirt.engine.core.compat;

public enum CommandStatus {
    UNKNOWN,
    NOT_STARTED,
    ACTIVE, // the execute methods on command base has been invoked
    ACTIVE_SYNC, // used by synchronous commands to indicate that the sync command is executing
    ACTIVE_ASYNC, // used by async commands to indicate that async command has been submitted
    FAILED,
    FAILED_RESTARTED, // set by command executor to indicate that the sync command did not complete
                      // and the server was restarted
    SUCCEEDED;
}
