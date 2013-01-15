package org.ovirt.engine.ui.frontend;

import java.util.MissingResourceException;

import com.google.gwt.core.client.GWT;

public class ConsoleErrorTranslator {

    private static final ConsoleErrors consoleErrors = GWT.create(ConsoleErrors.class);

    public String translateErrorCode(int errorCode) {
        try {
            return consoleErrors.getString("E" + errorCode); //$NON-NLS-1$
        } catch (MissingResourceException e) {
            return Integer.toString(errorCode);
        }
    }
}
