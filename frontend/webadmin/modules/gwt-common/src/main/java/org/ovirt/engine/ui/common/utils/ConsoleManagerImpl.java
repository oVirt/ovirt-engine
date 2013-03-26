package org.ovirt.engine.ui.common.utils;

import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.uicommonweb.ConsoleManager;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;

import com.google.inject.Inject;

/**
 * Can connect to console, if possible. If not, returns a message describing the problem with connection to the console.
 */
public class ConsoleManagerImpl implements ConsoleManager {

    private final ConsoleUtilsImpl consoleUtils;
    private final ConsoleOptionsFrontendPersister consoleOptionsPersister;
    private final CommonApplicationMessages messages;

    @Inject
    public ConsoleManagerImpl(ConsoleUtilsImpl consoleUtils, ConsoleOptionsFrontendPersister persister,
            CommonApplicationMessages messages) {
        this.consoleUtils = consoleUtils;
        this.consoleOptionsPersister = persister;
        this.messages = messages;
    }

    /**
     * Takes a model a protocol under which to connect. When successful, returns null. When not successful, returns
     * message describing the problem.
     */
    @Override
    public String connectToConsole(HasConsoleModel model) {
        consoleOptionsPersister.loadFromLocalStorage(model);

        ConsoleProtocol selectedProtocol = consoleUtils.determineConnectionProtocol(model);

        if (!consoleUtils.canShowConsole(selectedProtocol, model)) {
            return null;
        }

        if (selectedProtocol == ConsoleProtocol.SPICE) {
            return showSpiceConsole(model);
        } else if (selectedProtocol == ConsoleProtocol.RDP) {
            return showRpdConsole(model);
        } else if (selectedProtocol == ConsoleProtocol.VNC) {
            return showVncConsole(model);
        }
        return null;
    }

    private String showVncConsole(HasConsoleModel model) {
        model.getDefaultConsoleModel().getConnectCommand().Execute();

        return null;
    }

    private String showRpdConsole(HasConsoleModel model) {
        if (consoleUtils.canOpenRDPConsole(model)) {
            model.getAdditionalConsoleModel().getConnectCommand().Execute();
        } else if (!consoleUtils.isRDPAvailable()) {
            return createErrorMessage(model, "RDP"); //$NON-NLS-1$
        }

        return null;
    }

    private String showSpiceConsole(HasConsoleModel model) {
        if (consoleUtils.canOpenSpiceConsole(model)) {
            model.getDefaultConsoleModel().getConnectCommand().Execute();
        } else if (!consoleUtils.isSpiceAvailable()) {
            return createErrorMessage(model, "SPICE"); //$NON-NLS-1$
        }

        return null;
    }

    private String createErrorMessage(HasConsoleModel model, String protocol) {
        if (model.isPool()) {
            return messages.connectingToPoolIsNotSupported();
        } else {
            return messages.errorConnectingToConsole(model.getVM().getName(), protocol);
        }
    }

}
