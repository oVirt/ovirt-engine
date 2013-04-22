package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.HashMap;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister.ConsoleContext;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleModelsCache;
import org.ovirt.engine.ui.uicommonweb.models.ConsolePopupModel;
import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.HasConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class IUserPortalListModel extends ListWithDetailsModel implements UserSelectedDisplayProtocolManager
{
    private static final ConsoleOptionsFrontendPersister frontendPersister = (ConsoleOptionsFrontendPersister) TypeResolver.getInstance().Resolve(ConsoleOptionsFrontendPersister.class);

    private boolean canConnectAutomatically;

    private UICommand editConsoleCommand;

    protected final ConsoleModelsCache consoleModelsCache;

    public IUserPortalListModel() {
        consoleModelsCache = new ConsoleModelsCache(this);
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
            onPropertyChanged(new PropertyChangedEventArgs("CanConnectAutomatically")); //$NON-NLS-1$
        }
    }

    public UICommand getEditConsoleCommand() {
        return editConsoleCommand;
    }

    private void setEditConsoleCommand(UICommand editConsoleCommand) {
        this.editConsoleCommand = editConsoleCommand;
    }

    public abstract void OnVmAndPoolLoad();

    protected HashMap<Guid, VmPool> poolMap;

    public VmPool ResolveVmPoolById(Guid id)
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
                if ((onlyVmStatusUp && vm.getStatus() == VMStatus.Up)
                        || (!onlyVmStatusUp && userPortalItemModel.getDefaultConsoleModel().IsVmUp()))
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
        } else if (StringHelper.stringsEqual(command.getName(), Model.CANCEL_COMMAND)) {
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

        ConsolePopupModel model = new ConsolePopupModel();
        model.setConsoleContext(getConsoleContext());
        model.setModel((UserPortalItemModel) this.getSelectedItem());
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
        Frontend.Unsubscribe();
        setWindow(null);
        setConfirmWindow(null);
    }

    protected void updateConsoleModel(UserPortalItemModel item) {
        if (item.getEntity() != null)
        {
            Object tempVar = item.getEntity();
            VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
            if (vm == null)
            {
                item.setDefaultConsole(null);
                item.setAdditionalConsole(null);
                return;
            }

            consoleModelsCache.updateConsoleModelsForVm(vm);

            item.setDefaultConsole(consoleModelsCache.determineConsoleModelForVm(vm));
            item.setAdditionalConsole(consoleModelsCache.determineAdditionalConsoleModelForVm(vm));
            if (item.getVM() != null && item.getDefaultConsoleModel() != null) {
                frontendPersister.loadFromLocalStorage(item);
            }
        }
    }

    @Override
    public void setSelectedProtocol(ConsoleProtocol protocol, HasConsoleModel item) {
        consoleModelsCache.setSelectedProtocol(protocol, item);
    }

    @Override
    public ConsoleProtocol resolveSelectedProtocol(HasConsoleModel item) {
        return consoleModelsCache.resolveUserSelectedProtocol(item);
    }
}
