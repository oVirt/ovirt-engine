package org.ovirt.engine.core.compat;

public enum CommandStatus {
    UNKNOWN(false),
    NOT_STARTED(false),
    ACTIVE(true), // the execute methods on command base has been invoked
    FAILED(true),
    EXECUTION_FAILED(true), // set by command executor to indicate that the command execution wasn't completed
    SUCCEEDED(true),
    ENDED_SUCCESSFULLY(false), // indicates that the command endSuccessfully() was executed
    ENDED_WITH_FAILURE(false); // indicates that the command endWithFailure() was executed

    private boolean isDuringExecution;

    CommandStatus(boolean isDuringExecution) {
        this.isDuringExecution = isDuringExecution;
    }

    public boolean isDuringExecution() {
        return isDuringExecution;
    }
}
