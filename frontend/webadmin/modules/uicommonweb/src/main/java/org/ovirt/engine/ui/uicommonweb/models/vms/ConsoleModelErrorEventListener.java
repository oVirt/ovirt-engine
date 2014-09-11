package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.frontend.ConsoleErrorTranslator;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.IEventListener;

/**
 * Listens to error events of the ConsoleModel. If one catched, translates the error code to human readable message and
 * displays a popup.
 */
public class ConsoleModelErrorEventListener implements IEventListener<ErrorCodeEventArgs>, ICommandTarget {

    private ConsoleErrorTranslator consoleErrorCodeTranslator = new ConsoleErrorTranslator();

    private Model parentModel;

    public ConsoleModelErrorEventListener(Model parentModel) {
        super();
        this.parentModel = parentModel;
    }

    @Override
    public void eventRaised(Event<? extends ErrorCodeEventArgs> ev, Object sender, ErrorCodeEventArgs args) {
        if (ev.matchesDefinition(ConsoleModel.errorEventDefinition)
                && ((sender instanceof SpiceConsoleModel) || (sender instanceof RdpConsoleModel))) {
            consoleModelError(sender, getConsoleModelErrorMessage(sender, args));
        }
    }

    private String getConsoleModelErrorMessage(Object sender, ErrorCodeEventArgs e) {
        String translatedError = consoleErrorCodeTranslator.translateErrorCode(e.getErrorCode());

        if (sender instanceof SpiceConsoleModel) {
            return ConstantsManager.getInstance()
                    .getMessages()
                    .errConnectingVmUsingSpiceMsg(translatedError);
        } else if (sender instanceof RdpConsoleModel) {
            return ConstantsManager.getInstance()
                    .getMessages()
                    .errConnectingVmUsingRdpMsg(translatedError);
        }

        return translatedError;
    }

    private void consoleModelError(Object sender, String message) {
        ConfirmationModel model = new ConfirmationModel();
        if (parentModel.getConfirmWindow() == null) {
            parentModel.setConfirmWindow(model);
        }
        model.setTitle(ConstantsManager.getInstance().getConstants().consoleDisconnectedTitle());
        model.setHelpTag(HelpTag.console_disconnected);
        model.setHashName("console_disconnected"); //$NON-NLS-1$
        model.setMessage(message);

        UICommand tempVar = new UICommand("CancelError", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    @Override
    public void executeCommand(UICommand command) {
        if ("CancelError".equals(command.getName())) { //$NON-NLS-1$
            parentModel.setConfirmWindow(null);
        }
    }

    @Override
    public void executeCommand(UICommand uiCommand, Object... parameters) {
        executeCommand(uiCommand);
    }
}
