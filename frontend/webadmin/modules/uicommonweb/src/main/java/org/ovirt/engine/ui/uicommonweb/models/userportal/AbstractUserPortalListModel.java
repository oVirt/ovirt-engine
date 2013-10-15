package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleModelsCache;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.VmConsoles;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class AbstractUserPortalListModel extends ListWithDetailsModel {
    private boolean canConnectAutomatically;

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

    public boolean getCanConnectAutomatically()
    {
        return canConnectAutomatically;
    }

    public void setCanConnectAutomatically(boolean value)
    {
        if (canConnectAutomatically != value)
        {
            canConnectAutomatically = value;
            onPropertyChanged(new PropertyChangedEventArgs("CanConnectAutomatically")); //$NON-NLS-1$
        }
    }

    public UICommand getEditConsoleCommand() {
        return editConsoleCommand;
    }

    private void setEditConsoleCommand(UICommand editConsoleCommand) {
        this.editConsoleCommand = editConsoleCommand;
    }

    public abstract void onVmAndPoolLoad();

    protected HashMap<Guid, VmPool> poolMap;

    public VmPool resolveVmPoolById(Guid id)
    {
        return poolMap.get(id);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getEditConsoleCommand()) {
            editConsole();
        } else if (StringHelper.stringsEqual(command.getName(), "OnEditConsoleSave")) { //$NON-NLS-1$
            onEditConsoleSave();
        } else if (StringHelper.stringsEqual(command.getName(), Model.CANCEL_COMMAND)) {
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
        model.setHashName("editConsole"); //$NON-NLS-1$
        setWindow(model);

        UICommand saveCommand = new UICommand("OnEditConsoleSave", this); //$NON-NLS-1$
        saveCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        saveCommand.setIsDefault(true);
        model.getCommands().add(saveCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);
    }

    protected abstract ConsoleContext getConsoleContext();

    protected void cancel()
    {
        Frontend.getInstance().unsubscribe();
        setWindow(null);
        setConfirmWindow(null);
    }

}
