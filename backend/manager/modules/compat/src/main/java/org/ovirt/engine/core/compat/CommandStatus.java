package org.ovirt.engine.core.compat;

public enum CommandStatus {
    UNKNOWN,
    NOT_STARTED,
    ACTIVE, // the execute methods on command base has been invoked
    FAILED,
    FAILED_RESTARTED, // set by command executor to indicate that the command did not complete
                      // and the server was restarted
    SUCCEEDED;
}
