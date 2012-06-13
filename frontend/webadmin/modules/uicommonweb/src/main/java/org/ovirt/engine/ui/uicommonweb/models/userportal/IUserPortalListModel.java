package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.vm_pools;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class IUserPortalListModel extends ListWithDetailsModel
{

    private boolean canConnectAutomatically;

    private UICommand editConsoleCommand;

    public IUserPortalListModel() {
        setEditConsoleCommand(new UICommand("NewServer", this)); //$NON-NLS-1$
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
            OnPropertyChanged(new PropertyChangedEventArgs("CanConnectAutomatically")); //$NON-NLS-1$
        }
    }

    public UICommand getEditConsoleCommand() {
        return editConsoleCommand;
    }

    private void setEditConsoleCommand(UICommand editConsoleCommand) {
        this.editConsoleCommand = editConsoleCommand;
    }

    public abstract void OnVmAndPoolLoad();

    protected HashMap<Guid, vm_pools> poolMap;

    public vm_pools ResolveVmPoolById(Guid id)
    {
        return poolMap.get(id);
    }

    // Return a list of VMs with status 'UP'
    public ArrayList<UserPortalItemModel> GetStatusUpVms(Iterable items)
    {
        return GetUpVms(items, true);
    }

    // Return a list of up VMs
    public ArrayList<UserPortalItemModel> GetUpVms(Iterable items)
    {
        return GetUpVms(items, false);
    }

    private ArrayList<UserPortalItemModel> GetUpVms(Iterable items, boolean onlyVmStatusUp)
    {
        ArrayList<UserPortalItemModel> upVms = new ArrayList<UserPortalItemModel>();
        if (items != null)
        {
            for (Object item : items)
            {
                UserPortalItemModel userPortalItemModel = (UserPortalItemModel) item;
                Object tempVar = userPortalItemModel.getEntity();
                VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
                if (vm == null)
                {
                    continue;
                }
                if ((onlyVmStatusUp && vm.getstatus() == VMStatus.Up)
                        || (!onlyVmStatusUp && userPortalItemModel.getDefaultConsole().IsVmUp()))
                {
                    upVms.add(userPortalItemModel);
                }
            }
        }
        return upVms;
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (command == getEditConsoleCommand()) {
            editConsole();
        } else if (StringHelper.stringsEqual(command.getName(), "OnEditConsoleSave")) { //$NON-NLS-1$
            onEditConsoleSave();
        } else if (StringHelper.stringsEqual(command.getName(), "Cancel")) {//$NON-NLS-1$
            cancel();
        }
    }

    private void onEditConsoleSave() {
        cancel();
    }

    private void editConsole() {
        if (getWindow() != null) {
            return;
        }

        UserPortalConsolePopupModel model = new UserPortalConsolePopupModel();
        model.setModel(this);
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

    protected void cancel()
    {
        Frontend.Unsubscribe();
        setWindow(null);
        setConfirmWindow(null);
    }
}
