package org.ovirt.engine.core.compat.backendcompat;

public enum CommandExecutionStatus {
    UNKNOWN,     // the CommandEntity for the commands is not in database
    EXECUTED,    // the command's execute method completed execution
    NOT_EXECUTED; // the command's execute method did not complete execution
}
