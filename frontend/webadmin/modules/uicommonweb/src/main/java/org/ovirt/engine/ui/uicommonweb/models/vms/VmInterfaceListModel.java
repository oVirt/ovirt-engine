package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.ActivateDeactivateVmNicParameters;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class VmInterfaceListModel extends SearchableListModel
{

    private boolean isHotPlugSupported;

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    private UICommand privateActivateCommand;

    public UICommand getActivateCommand()
    {
        return privateActivateCommand;
    }

    private void setActivateCommand(UICommand value)
    {
        privateActivateCommand = value;
    }

    private UICommand privateDactivateCommand;

    public UICommand getDeactivateCommand()
    {
        return privateDactivateCommand;
    }

    private void setDeactivateCommand(UICommand value)
    {
        privateDactivateCommand = value;
    }

    public VmInterfaceListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHashName("network_interfaces"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$
        setActivateCommand(new UICommand("Activate", this)); //$NON-NLS-1$
        setDeactivateCommand(new UICommand("Deactivate", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }

        UpdateActionAvailability();
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }
        VM vm = (VM) getEntity();
        super.SyncSearch(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(vm.getId()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VM vm = (VM) getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetVmInterfacesByVmId,
                new GetVmByVmIdParameters(vm.getId())));
        setItems(getAsyncResult().getData());
    }

    private void New()
    {
        VM vm = (VM) getEntity();

        if (getWindow() != null)
        {
            return;
        }

        ArrayList<VmNetworkInterface> interfaces = Linq.<VmNetworkInterface> Cast(getItems());
        String newNicName = DataProvider.GetNewNicName(interfaces);

        VmInterfaceModel model = new VmInterfaceModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().newNetworkInterfaceTitle());
        model.setHashName("new_network_interface_vms"); //$NON-NLS-1$
        model.setIsNew(true);
        model.getNicType().setItems(DataProvider.GetNicTypeList(vm.getVmOs(), false));
        model.getNicType().setSelectedItem(DataProvider.GetDefaultNicType(vm.getVmOs()));
        model.getName().setEntity(newNicName);
        model.getMAC().setIsChangable(false);

        model.getPlugged().setIsChangable(isHotPlugSupported);
        model.getPlugged().setEntity(true);

        Version v31 = new Version(3, 1);
        boolean isLessThan31 = vm.getVdsGroupCompatibilityVersion().compareTo(v31) < 0;

        model.getPortMirroring().setIsChangable(!isLessThan31);
        model.getPortMirroring().setEntity(false);

        final UICommand okCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        // wait for data to fetch
        okCommand.setIsExecutionAllowed(false);
        model.getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        model.getCommands().add(cancelCommand);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model1, Object result1)
            {
                VmInterfaceListModel vmInterfaceListModel = (VmInterfaceListModel) model1;
                VmInterfaceModel vmInterfaceModel = (VmInterfaceModel) vmInterfaceListModel.getWindow();

                ArrayList<Network> networks = new ArrayList<Network>();
                for (Network a : (ArrayList<Network>) result1)
                {
                    if (a.getCluster().getstatus() == NetworkStatus.Operational && a.isVmNetwork())
                    {
                        networks.add(a);
                    }
                }

                if (vmInterfaceModel.getIsNew())
                {
                    vmInterfaceModel.getNetwork().setItems(networks);
                    vmInterfaceModel.getNetwork().setSelectedItem(networks.size() > 0 ? networks.get(0) : null);
                }
                else
                {
                    VmNetworkInterface nic = (VmNetworkInterface) vmInterfaceListModel.getSelectedItem();

                    vmInterfaceModel.getNetwork().setItems(networks);
                    vmInterfaceModel.getNetwork().setSelectedItem(null);
                    for (Network a : networks)
                    {
                        if (StringHelper.stringsEqual(a.getname(), nic.getNetworkName()))
                        {
                            vmInterfaceModel.getNetwork().setSelectedItem(a);
                            break;
                        }
                    }
                }
                // fetch completed
                okCommand.setIsExecutionAllowed(true);
            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, vm.getVdsGroupId());
    }

    private void Edit()
    {
        VM vm = (VM) getEntity();
        VmNetworkInterface nic = (VmNetworkInterface) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        VmInterfaceModel model = new VmInterfaceModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().editNetworkInterfaceTitle());
        model.setHashName("edit_network_interface_vms"); //$NON-NLS-1$

        Integer selectedNicType = nic.getType();
        ArrayList<VmInterfaceType> nicTypes =
                DataProvider.GetNicTypeList(vm.getVmOs(),
                        VmInterfaceType.forValue(selectedNicType) == VmInterfaceType.rtl8139_pv);
        model.getNicType().setItems(nicTypes);

        if (selectedNicType == null || !nicTypes.contains(VmInterfaceType.forValue(selectedNicType)))
        {
            selectedNicType = DataProvider.GetDefaultNicType(vm.getVmOs()).getValue();
        }

        model.getNicType().setSelectedItem(VmInterfaceType.forValue(selectedNicType));

        model.getName().setEntity(nic.getName());
        model.getMAC().setIsChangable(false);
        model.getMAC().setEntity(nic.getMacAddress());
        model.getPlugged().setIsAvailable(false);

        Version v31 = new Version(3, 1);
        boolean isLessThan31 = vm.getVdsGroupCompatibilityVersion().compareTo(v31) < 0;

        model.getPortMirroring().setIsChangable(!isLessThan31);
        model.getPortMirroring().setEntity(nic.isPortMirroring());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model1, Object result1)
            {
                VmInterfaceListModel vmInterfaceListModel = (VmInterfaceListModel) model1;
                VmInterfaceModel vmInterfaceModel = (VmInterfaceModel) vmInterfaceListModel.getWindow();

                ArrayList<Network> networks = new ArrayList<Network>();
                for (Network a : (ArrayList<Network>) result1)
                {
                    if (a.getCluster().getstatus() == NetworkStatus.Operational && a.isVmNetwork())
                    {
                        networks.add(a);
                    }
                }

                if (vmInterfaceModel.getIsNew())
                {
                    vmInterfaceModel.getNetwork().setItems(networks);
                    vmInterfaceModel.getNetwork().setSelectedItem(networks.size() > 0 ? networks.get(0) : null);
                }
                else
                {
                    VmNetworkInterface nic1 = (VmNetworkInterface) vmInterfaceListModel.getSelectedItem();

                    vmInterfaceModel.getNetwork().setItems(networks);
                    for (Network a : networks)
                    {
                        if (StringHelper.stringsEqual(a.getname(), nic1.getNetworkName()))
                        {
                            vmInterfaceModel.getNetwork().setSelectedItem(a);
                            break;
                        }
                    }
                }

            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, vm.getVdsGroupId());

        UICommand tempVar = new UICommand("OnSave", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnSave()
    {
        VM vm = (VM) getEntity();
        VmInterfaceModel model = (VmInterfaceModel) getWindow();

        VmNetworkInterface nic =
                model.getIsNew() ? new VmNetworkInterface() : (VmNetworkInterface) Cloner.clone(getSelectedItem());

        if (model.getProgress() != null)
        {
            return;
        }

        if (!model.Validate())
        {
            return;
        }

        // Save changes.
        nic.setName((String) model.getName().getEntity());
        nic.setNetworkName(((Network) model.getNetwork().getSelectedItem()).getname());
        nic.setPortMirroring((Boolean) model.getPortMirroring().getEntity());
        if (model.getNicType().getSelectedItem() == null)
        {
            nic.setType(null);
        }
        else
        {
            nic.setType(((VmInterfaceType) model.getNicType().getSelectedItem()).getValue());
        }
        nic.setMacAddress(model.getMAC().getIsChangable() ? (model.getMAC().getEntity() == null ? null
                : ((String) (model.getMAC().getEntity())).toLowerCase()) : model.getIsNew() ? "" : nic.getMacAddress()); //$NON-NLS-1$

        if (model.getIsNew())
        {
            nic.setActive((Boolean) model.getPlugged().getEntity());
        }

        model.StartProgress(null);

        Frontend.RunAction(model.getIsNew() ? VdcActionType.AddVmInterface : VdcActionType.UpdateVmInterface,
                new AddVmInterfaceParameters(vm.getId(), nic),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        VmInterfaceListModel localModel = (VmInterfaceListModel) result.getState();
                        localModel.PostOnSave(result.getReturnValue());

                    }
                },
                this);
    }

    public void PostOnSave(VdcReturnValueBase returnValue)
    {
        VmInterfaceModel model = (VmInterfaceModel) getWindow();

        model.StopProgress();

        if (returnValue != null && returnValue.getSucceeded())
        {
            Cancel();
        }
    }

    private void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        RemoveVmInterfaceModel model = new RemoveVmInterfaceModel(this, getSelectedItems(), false);
        setWindow(model);
    }

    private void activate(boolean activate) {
        VM vm = (VM) getEntity();

        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems()) {
            VmNetworkInterface nic = (VmNetworkInterface) item;
            nic.setActive(activate);

            ActivateDeactivateVmNicParameters params =
                    new ActivateDeactivateVmNicParameters(nic.getId(), activate ? PlugAction.PLUG : PlugAction.UNPLUG);
            params.setVmId(vm.getId());
            paramerterList.add(params);
        }

        Frontend.RunMultipleAction(VdcActionType.ActivateDeactivateVmNic, paramerterList,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {
                    }
                },
                this);
    }

    private void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        VM vm = (VM) getEntity();

        ArrayList<VM> items = new ArrayList<VM>();
        if (vm != null)
        {
            items.add(vm);
        }

        getNewCommand().setIsExecutionAllowed(vm != null
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.AddVmInterface));
        getEditCommand().setIsExecutionAllowed(vm != null
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.UpdateVmInterface)
                && (getSelectedItems() != null && getSelectedItems().size() == 1));
        getRemoveCommand().setIsExecutionAllowed(vm != null
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RemoveVmInterface) && canRemoveNics()
                && (getSelectedItems() != null && getSelectedItems().size() > 0));
        getActivateCommand().setIsExecutionAllowed(vm != null
                && (getSelectedItems() != null && getSelectedItems().size() > 0) && isActivateCommandAvailable(true));
        getDeactivateCommand().setIsExecutionAllowed(vm != null
                && (getSelectedItems() != null && getSelectedItems().size() > 0) && isActivateCommandAvailable(false));
    }

    private boolean canRemoveNics() {
        VM vm = (VM) getEntity();
        if (VMStatus.Down.equals(vm.getStatus())) {
            return true;
        }

        if (!isHotPlugSupported) {
            return false;
        }

        ArrayList<VmNetworkInterface> nics =
                getSelectedItems() != null ? Linq.<VmNetworkInterface> Cast(getSelectedItems())
                        : new ArrayList<VmNetworkInterface>();

        for (VmNetworkInterface nic : nics)
        {
            if (nic.isActive())
            {
                return false;
            }
        }

        return true;
    }

    private boolean isActivateCommandAvailable(boolean active) {
        if (!isHotPlugSupported) {
            return false;
        }

        ArrayList<VmNetworkInterface> nics =
                getSelectedItems() != null ? Linq.<VmNetworkInterface> Cast(getSelectedItems())
                        : new ArrayList<VmNetworkInterface>();

        for (VmNetworkInterface nic : nics)
        {
            if (nic.isActive() == active)
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }
        else if (command == getActivateCommand())
        {
            activate(true);
        }
        else if (command == getDeactivateCommand())
        {
            activate(false);
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    @Override
    protected String getListName() {
        return "VmInterfaceListModel"; //$NON-NLS-1$
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    protected void updateIsHotPlugAvailable()
    {
        if (getEntity() == null)
        {
            return;
        }
        VM vm = (VM) getEntity();
        Version clusterCompatibilityVersion = vm.getVdsGroupCompatibilityVersion() != null
                ? vm.getVdsGroupCompatibilityVersion() : new Version();

        isHotPlugSupported =
                (Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.HotPlugEnabled,
                        clusterCompatibilityVersion.toString());
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value);
        updateIsHotPlugAvailable();
    }
}
