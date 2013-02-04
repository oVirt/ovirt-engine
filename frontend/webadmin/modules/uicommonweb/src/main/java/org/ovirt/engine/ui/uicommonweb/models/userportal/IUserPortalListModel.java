package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleModelErrorEventListener;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleSelectionContext;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VncConsoleModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class IUserPortalListModel extends ListWithDetailsModel implements UserSelectedDisplayProtocolManager
{

    private boolean canConnectAutomatically;

    private UICommand editConsoleCommand;

    private final HashMap<Guid, ArrayList<ConsoleModel>> cachedConsoleModels;

    private static final int SPICE_INDEX = 0;
    private static final int VNC_INDEX = 1;
    private static final int RDP_INDEX = 2;

    private static final List<VmOsType> vmOsTypeWithoutSpiceDriverSupport = Arrays.asList(VmOsType.Windows8,
            VmOsType.Windows8x64,
            VmOsType.Windows2012x64);

    public IUserPortalListModel() {
        cachedConsoleModels = new HashMap<Guid, ArrayList<ConsoleModel>>();
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

    protected void updateConsoleModel(UserPortalItemModel item) {
        if (item.getEntity() != null)
        {
            Object tempVar = item.getEntity();
            VM vm = (VM) ((tempVar instanceof VM) ? tempVar : null);
            if (vm == null)
            {
                return;
            }

            // Caching console model if needed
            if (!cachedConsoleModels.containsKey(vm.getId()))
            {
                SpiceConsoleModel spiceConsoleModel = new SpiceConsoleModel();
                spiceConsoleModel.getErrorEvent().addListener(this);
                spiceConsoleModel.setParentModel(this);
                spiceConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(this));

                VncConsoleModel vncConsoleModel = new VncConsoleModel();
                vncConsoleModel.setParentModel(this);

                RdpConsoleModel rdpConsoleModel = new RdpConsoleModel();
                rdpConsoleModel.getErrorEvent().addListener(new ConsoleModelErrorEventListener(this));

                cachedConsoleModels.put(vm.getId(),
                        new ArrayList<ConsoleModel>(Arrays.asList(new ConsoleModel[] {
                                spiceConsoleModel, vncConsoleModel, rdpConsoleModel })));

                updateDefaultSelectedConsoleProtocol(vm);
            } else if (selectionContextChanged(vm)) {
                // if new data comes which has changed the selection context, (e.g. the OS type changed)
                // recalculate the default selected protocol
                updateDefaultSelectedConsoleProtocol(vm);
            }

            // Getting cached console model
            ArrayList<ConsoleModel> cachedModels = cachedConsoleModels.get(vm.getId());
            for (ConsoleModel cachedModel : cachedModels)
            {
                cachedModel.setEntity(null);
                cachedModel.setEntity(vm);
            }

            // Set default console by vm's display type
            item.setDefaultConsole(determineConsoleModelFromVm(vm, cachedModels));

            // Update additional console
            if (AsyncDataProvider.IsWindowsOsType(vm.getVmOs()))
            {
                item.setAdditionalConsole(cachedModels.get(RDP_INDEX));
                item.setHasAdditionalConsole(true);
            }
            else
            {
                item.setAdditionalConsole(null);
                item.setHasAdditionalConsole(false);
            }
        }
    }

    private boolean selectionContextChanged(VM vm) {
        ConsoleSelectionContext newContext = new ConsoleSelectionContext(vm.getVmOs(), vm.getDefaultDisplayType());
        ConsoleModel selectedConsole = resolveSelectedConsoleModel(vm.getId());

        if (selectedConsole == null) {
            return true;
        }

        return !newContext.equals(selectedConsole.getSelectionContext());
    }

    protected ConsoleModel determineConsoleModelFromVm(VM vm, ArrayList<ConsoleModel> cachedModels) {
        return vm.getDefaultDisplayType() == DisplayType.vnc ? cachedModels.get(VNC_INDEX) : cachedModels.get(SPICE_INDEX);
    }

    protected void updateDefaultSelectedConsoleProtocol(VM vm) {
        // for wind8+ guests the RDP is selected, for all other OS the spice
        if (vm.getId() == null) {
            return;
        }

        ArrayList<ConsoleModel> cachedModels = cachedConsoleModels.get(vm.getId());
        if (cachedModels == null) {
            return;
        }

        deselectUserSelectedProtocol(vm.getId());

        boolean isWindowsExplorer = getConfigurator().isClientWindownsExplorer();

        if (vmOsTypeWithoutSpiceDriverSupport.contains(vm.getOs()) && isWindowsExplorer) {
            cachedModels.get(RDP_INDEX).setUserSelected(true);
        } else {
            determineConsoleModelFromVm(vm, cachedModels).setUserSelected(true);
        }

        setupSelectionContext(vm);
    }

    private void setupSelectionContext(VM vm) {
        for (ConsoleModel model : cachedConsoleModels.get(vm.getId())) {
            model.setSelectionContext(new ConsoleSelectionContext(vm.getVmOs(), vm.getDefaultDisplayType()));
        }
    }

    @Override
    public void setSelectedProtocol(ConsoleProtocol protocol, UserPortalItemModel item) {
        Guid vmId = item.getEntity() instanceof VM ? ((VM) item.getEntity()).getId() : null;
        if (vmId == null) {
            return;
        }

        deselectUserSelectedProtocol(vmId);

        for (ConsoleModel model : cachedConsoleModels.get(vmId)) {
            if (protocol.isBackedBy(model.getClass())) {
                model.setUserSelected(true);
                break;
            }
        }
    }

    protected void deselectUserSelectedProtocol(Guid vmId) {
        for (ConsoleModel model : cachedConsoleModels.get(vmId)) {
            model.setUserSelected(false);
        }
    }

    @Override
    public ConsoleProtocol resolveSelectedProtocol(UserPortalItemModel item) {
        Guid vmId = item.getEntity() instanceof VM ? ((VM) item.getEntity()).getId() : null;
        if (vmId == null) {
            return null;
        }

        ConsoleModel selectedConsoleModel = resolveSelectedConsoleModel(vmId);
        return selectedConsoleModel == null ? null
                : ConsoleProtocol.getProtocolByModel(selectedConsoleModel.getClass());
    }

    private ConsoleModel resolveSelectedConsoleModel(Guid vmId) {
        for (ConsoleModel model : cachedConsoleModels.get(vmId)) {
            if (model.isUserSelected()) {
                return model;
            }
        }

        return null;
    }
}
