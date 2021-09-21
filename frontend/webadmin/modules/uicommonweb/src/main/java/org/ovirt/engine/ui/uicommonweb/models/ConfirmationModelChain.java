package org.ovirt.engine.ui.uicommonweb.models;

import java.util.LinkedList;

import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;

/**
 * Takes a series of confirmation dialogs, executes them one by one. If the user confirmed all of the dialogs,
 * the provided success callback would be executed. If user cancelled any of the dialogs or none was
 * required, nothing happens.
 *
 */
public class ConfirmationModelChain implements ICommandTarget {

    public interface ConfirmationModelChainItem {

        default void init(Runnable callback) {
            callback.run();
        }

        boolean isRequired();

        ConfirmationModel getConfirmation();

        default void onConfirm() { }
    }

    private LinkedList<ConfirmationModelChainItem> confirmations = new LinkedList<>();

    private ConfirmationModelChainItem currentChainItem;

    private Model parentModel;

    private Runnable successCallback;

    private UICommand okCommand;

    private UICommand cancelCommand;

    public void addConfirmation(ConfirmationModelChainItem confirmation) {
        confirmations.add(confirmation);
    }

    public void execute(Model parent, Runnable callback) {
        this.parentModel = parent;
        this.successCallback = callback;

        moveToNextConfirmation();
    }

    private void moveToNextConfirmation() {
        if (confirmations.isEmpty()) {
            successCallback.run();
            return;
        }

        currentChainItem = confirmations.pollFirst();
        currentChainItem.init(this::processConfirmation);
    }

    private void processConfirmation() {
        if (!currentChainItem.isRequired()) {
            moveToNextConfirmation();
            return;
        }

        ConfirmationModel confirmation = currentChainItem.getConfirmation();
        confirmation.getCommands().add(getOkCommand());
        confirmation.getCommands().add(getCancelCommand());
        parentModel.setConfirmWindow(confirmation);
    }

    @Override
    public void executeCommand(UICommand command) {
        if (getOkCommand() == command) {
            currentChainItem.onConfirm();
            parentModel.setConfirmWindow(null);
            moveToNextConfirmation();
        } else if (getCancelCommand() == command) {
            parentModel.setConfirmWindow(null);
        }
    }

    @Override
    public void executeCommand(UICommand uiCommand, Object... parameters) {
        executeCommand(uiCommand);
    }

    public UICommand getOkCommand() {
        if (okCommand == null) {
            okCommand = UICommand.createDefaultOkUiCommand("OKConfirmation", this); //$NON-NLS-1$
        }
        return okCommand;
    }

    public void setOkCommand(UICommand okCommand) {
        this.okCommand = okCommand;
    }

    public UICommand getCancelCommand() {
        if (cancelCommand == null) {
            cancelCommand = UICommand.createCancelUiCommand("CancelConfirmation", this); //$NON-NLS-1$
        }
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }
}
