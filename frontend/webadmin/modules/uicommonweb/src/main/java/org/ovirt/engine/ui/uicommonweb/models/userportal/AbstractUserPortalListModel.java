package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleModelsCache;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;

public abstract class AbstractUserPortalListModel extends ListWithDetailsModel {
    private UICommand editConsoleCommand;

    protected ConsoleModelsCache consoleModelsCache;

    public AbstractUserPortalListModel() {
        setEditConsoleCommand(new UICommand("NewServer", this)); //$NON-NLS-1$
    }

    protected Iterable filterVms(List all) {
        List<VM> result = new LinkedList<VM>();
        for (Object o : all) {
            if (o instanceof VM) {
                result.add((VM) o);
            }
        }
        return result;
    }

    public List<VmConsoles> getAutoConnectableConsoles() {
        List<VmConsoles> autoConnectableConsoles = new LinkedList<VmConsoles>();

        for (Object item : items) {
            UserPortalItemModel upItem = (UserPortalItemModel) item;

            if (!upItem.isPool() && upItem.getVmConsoles().canConnectToConsole()) {
                autoConnectableConsoles.add(upItem.getVmConsoles());
            }
        }

        return autoConnectableConsoles;
    }

    public boolean getCanConnectAutomatically() {
        return getAutoConnectableConsoles().size() == 1;
    }

    public UICommand getEditConsoleCommand() {
        return editConsoleCommand;
    }

    private void setEditConsoleCommand(UICommand editConsoleCommand) {
        this.editConsoleCommand = editConsoleCommand;
    }

    public abstract void onVmAndPoolLoad();

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getEditConsoleCommand()) {
            editConsole();
        } else if ("OnEditConsoleSave".equals(command.getName())) { //$NON-NLS-1$
            onEditConsoleSave();
        } else if (Model.CANCEL_COMMAND.equals(command.getName())) {
            cancel();
        }
    }

    private void onEditConsoleSave() {
        cancel();
    }

    private void editConsole() {
        if (getWindow() != null || ((UserPortalItemModel) getSelectedItem()).getVmConsoles() == null) {
            return;
        }

        ConsolePopupModel model = new ConsolePopupModel();
        model.setVmConsoles(((UserPortalItemModel) getSelectedItem()).getVmConsoles());
        model.setHelpTag(HelpTag.editConsole);
        model.setHashName("editConsole"); //$NON-NLS-1$
        setWindow(model);

        UICommand saveCommand = UICommand.createDefaultOkUiCommand("OnEditConsoleSave", this); //$NON-NLS-1$
        model.getCommands().add(saveCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(cancelCommand);
    }

    protected abstract ConsoleContext getConsoleContext();

    protected void cancel()
    {
        setWindow(null);
        setConfirmWindow(null);
    }

}
