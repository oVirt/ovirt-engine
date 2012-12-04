package org.ovirt.engine.ui.userportal.utils;

import org.ovirt.engine.ui.uicommonweb.models.userportal.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalItemModel;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.widget.basic.ConsoleUtils;

import com.google.inject.Inject;

/**
 * Can connect to console, if possible. If not, returns a message describing the problem with connection to the console.
 */
public class ConsoleManager {

    private final ConsoleUtils consoleUtils;
    private final ApplicationMessages messages;

    @Inject
    public ConsoleManager(ConsoleUtils consoleUtils, ApplicationMessages messages) {
        this.consoleUtils = consoleUtils;
        this.messages = messages;
    }

    /**
     * Takes a model a protocol under which to connect. When successful, returns null. When not successful, returns
     * message describing the problem.
     */
    public String connectToConsole(ConsoleProtocol selectedProtocol, UserPortalItemModel model) {
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

    private String showVncConsole(UserPortalItemModel model) {
        model.getDefaultConsole().getConnectCommand().Execute();

        return null;
    }

    private String showRpdConsole(UserPortalItemModel model) {
        if (consoleUtils.canOpenRDPConsole(model)) {
            model.getAdditionalConsole().getConnectCommand().Execute();
        } else if (!consoleUtils.isRDPAvailable()) {
            return createErrorMessage(model, "RDP"); //$NON-NLS-1$
        }

        return null;
    }

    private String showSpiceConsole(UserPortalItemModel model) {
        if (consoleUtils.canOpenSpiceConsole(model)) {
            model.getDefaultConsole().getConnectCommand().Execute();
        } else if (!consoleUtils.isSpiceAvailable()) {
            return createErrorMessage(model, "SPICE"); //$NON-NLS-1$
        }

        return null;
    }

    private String createErrorMessage(UserPortalItemModel model, String protocol) {
        return messages.errorConnectingToConsole(model.getName(), protocol);
    }

}
