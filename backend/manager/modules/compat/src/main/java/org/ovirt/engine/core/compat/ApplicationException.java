package org.ovirt.engine.core.compat;

public class ApplicationException extends RuntimeException {

    private static final long serialVersionUID = 3398093471155070951L;

    public ApplicationException() {
    }

    public ApplicationException(String string) {
        super(string);
    }

    public ApplicationException(Throwable ex) {
        super(ex);
    }

    public ApplicationException(String string, Throwable ex) {
        super(string, ex);
    }

}
