package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.frontend.ConsoleErrorTranslator;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

/**
 * Listens to error events of the ConsoleModel. If one catched, translates the error code to human readable message and
 * displays a popup.
 */
public class ConsoleModelErrorEventListener implements IEventListener, ICommandTarget {

    private ConsoleErrorTranslator consoleErrorCodeTranslator = new ConsoleErrorTranslator();

    private Model parentModel;

    public ConsoleModelErrorEventListener(Model parentModel) {
        super();
        this.parentModel = parentModel;
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        if (ev.equals(ConsoleModel.ErrorEventDefinition)
                && ((sender instanceof SpiceConsoleModel) || (sender instanceof RdpConsoleModel))) {
            consoleModelError(sender, getConsoleModelErrorMessage(sender, (ErrorCodeEventArgs) args));
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
        model.setHashName("console_disconnected"); //$NON-NLS-1$
        model.setMessage(message);

        UICommand tempVar = new UICommand("CancelError", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        if ("CancelError".equals(command.getName())) { //$NON-NLS-1$
            parentModel.setConfirmWindow(null);
        }
    }

    @Override
    public void ExecuteCommand(UICommand uiCommand, Object... parameters) {
        ExecuteCommand(uiCommand);
    }
}
