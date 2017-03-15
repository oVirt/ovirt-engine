package org.ovirt.engine.ssoreg.core;

public class ExitException extends RuntimeException {
    private int exitCode;

    public ExitException() {
        this(0);
    }

    public ExitException(int exitCode) {
        this(null, exitCode);
    }

    public ExitException(String msg, int exitCode) {
        super(msg);
        this.exitCode = exitCode;
    }

    public int getExitCode() {
        return exitCode;
    }
}
