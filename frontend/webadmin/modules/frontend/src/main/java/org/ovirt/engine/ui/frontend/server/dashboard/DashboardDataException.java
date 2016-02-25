package org.ovirt.engine.ui.frontend.server.dashboard;

import javax.servlet.ServletException;

public class DashboardDataException extends ServletException {

    public DashboardDataException() {
        super();
    }

    public DashboardDataException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public DashboardDataException(String message) {
        super(message);
    }

    public DashboardDataException(Throwable rootCause) {
        super(rootCause);
    }
}
