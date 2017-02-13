package org.ovirt.engine.ui.gwtaop;

import java.io.OutputStream;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JavaLoggingConfig {

    public JavaLoggingConfig() {
        // This removes all log handlers, including default console handler
        LogManager.getLogManager().reset();

        // Use custom console handler that outputs its messages to std-out
        Logger.getLogger("").addHandler(new ConsoleHandler() {
            @Override
            protected synchronized void setOutputStream(OutputStream out) throws SecurityException {
                // Can't do this via constructor
                super.setOutputStream(System.out);
            }
        });
    }

}
