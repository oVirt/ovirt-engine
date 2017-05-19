package org.ovirt.engine.ui.common.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.system.ApplicationFocusChangeEvent;
import org.ovirt.engine.ui.common.system.ApplicationFocusManager;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.logging.client.SimpleRemoteLogHandler;
import com.google.inject.Inject;

/**
 * Bound as GIN eager singleton (and therefore executed just before GWTP's
 * {@linkplain com.gwtplatform.mvp.client.Bootstrapper#onBootstrap onBootstrap}),
 * it sets up the logging infrastructure.
 */
public class ApplicationLogManager {

    private final Logger rootLogger;
    private final Logger remoteLogger;

    private final ApplicationFocusManager applicationFocusManager;

    private Throwable lastError;
    private int sameErrorCount = 1;

    @Inject
    public ApplicationLogManager(EventBus eventBus,
            ApplicationFocusManager applicationFocusManager,
            LocalStorageLogHandler localStorageLogHandler) {
        this.rootLogger = Logger.getLogger(""); //$NON-NLS-1$
        this.remoteLogger = Logger.getLogger("remote"); //$NON-NLS-1$
        this.applicationFocusManager = applicationFocusManager;

        // Configure root logger
        localStorageLogHandler.init();
        rootLogger.addHandler(localStorageLogHandler);

        // Configure remote logger
        remoteLogger.addHandler(new SimpleRemoteLogHandler());

        // Enable/disable log handlers when the application window gains/looses its focus
        eventBus.addHandler(ApplicationFocusChangeEvent.getType(),
                event -> localStorageLogHandler.setActive(event.isInFocus()));
    }

    /**
     * Logs an uncaught exception that would normally escape the application code.
     *
     * @see com.google.gwt.core.client.GWT.UncaughtExceptionHandler
     */
    public void logUncaughtException(Throwable t) {
        boolean sameAsLastError = compareWithLastError(t);
        if (sameAsLastError) {
            sameErrorCount++;
        } else {
            sameErrorCount = 1;
        }

        String logMessage = sameAsLastError
                ? "Uncaught exception (" + sameErrorCount + "x)" //$NON-NLS-1$ //$NON-NLS-2$
                : "Uncaught exception"; //$NON-NLS-1$

        // Log locally
        rootLogger.log(Level.SEVERE, logMessage, t); //$NON-NLS-1$

        // Log remotely
        if (applicationFocusManager.isInFocus() && !sameAsLastError) {
            remoteLogger.log(Level.SEVERE, logMessage, t); //$NON-NLS-1$
        }
    }

    /**
     * Returns {@code true} if {@code t} is effectively the same as {@link #lastError}.
     *
     * @see #compareStackTraces
     */
    boolean compareWithLastError(Throwable t) {
        if (lastError == null) {
            lastError = t;
            return false;
        }

        boolean equalStackTraces = compareStackTraces(lastError, t);
        lastError = t;
        return equalStackTraces;
    }

    /**
     * Returns {@code true} if {@code t1} and {@code t2} have equal stack traces.
     */
    boolean compareStackTraces(Throwable t1, Throwable t2) {
        StackTraceElement[] st1 = t1.getStackTrace();
        StackTraceElement[] st2 = t2.getStackTrace();

        if (st1.length != st2.length) {
            return false;
        }

        for (int i = 0; i < st1.length; i++) {
            StackTraceElement e1 = st1[i];
            StackTraceElement e2 = st2[i];

            if (!e1.toString().equals(e2.toString())) {
                return false;
            }
        }

        return true;
    }

}
