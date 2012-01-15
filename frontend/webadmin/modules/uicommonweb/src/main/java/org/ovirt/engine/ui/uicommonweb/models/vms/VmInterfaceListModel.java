package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.AddVmInterfaceParameters;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.queries.GetVmByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;

@SuppressWarnings("unused")
public class VmInterfaceListModel extends SearchableListModel
{

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

    public VmInterfaceListModel()
    {
        setTitle("Network Interfaces");

        setNewCommand(new UICommand("New", this));
        setEditCommand(new UICommand("Edit", this));
        setRemoveCommand(new UICommand("Remove", this));

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
        super.SyncSearch(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(vm.getvm_guid()));
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VM vm = (VM) getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetVmInterfacesByVmId,
                new GetVmByVmIdParameters(vm.getvm_guid())));
        setItems(getAsyncResult().getData());
    }

    private void New()
    {
        VM vm = (VM) getEntity();

        if (getWindow() != null)
        {
            return;
        }

        java.util.ArrayList<VmNetworkInterface> interfaces = Linq.<VmNetworkInterface> Cast(getItems());
        String newNicName = DataProvider.GetNewNicName(interfaces);

        VmInterfaceModel model = new VmInterfaceModel();
        setWindow(model);
        model.setTitle("New Network Interface");
        model.setHashName("new_network_interface_vms");
        model.setIsNew(true);
        model.getNicType().setItems(DataProvider.GetNicTypeList(vm.getvm_os(), false));
        model.getNicType().setSelectedItem(DataProvider.GetDefaultNicType(vm.getvm_os()));
        model.getName().setEntity(newNicName);
        model.getMAC().setIsChangable(false);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model1, Object result1)
            {
                VmInterfaceListModel vmInterfaceListModel = (VmInterfaceListModel) model1;
                VmInterfaceModel vmInterfaceModel = (VmInterfaceModel) vmInterfaceListModel.getWindow();

                java.util.ArrayList<network> networks = new java.util.ArrayList<network>();
                for (network a : (java.util.ArrayList<network>) result1)
                {
                    if (a.getStatus() == NetworkStatus.Operational)
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
                    for (network a : networks)
                    {
                        if (StringHelper.stringsEqual(a.getname(), nic.getNetworkName()))
                        {
                            vmInterfaceModel.getNetwork().setSelectedItem(a);
                            break;
                        }
                    }
                }

            }
        };
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, vm.getvds_group_id());

        UICommand tempVar = new UICommand("OnSave", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
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
        model.setTitle("Edit Network Interface");
        model.setHashName("edit_network_interface_vms");

        Integer selectedNicType = nic.getType();
        java.util.ArrayList<VmInterfaceType> nicTypes =
                DataProvider.GetNicTypeList(vm.getvm_os(),
                        VmInterfaceType.forValue(selectedNicType) == VmInterfaceType.rtl8139_pv);
        model.getNicType().setItems(nicTypes);

        if (selectedNicType == null || !nicTypes.contains(VmInterfaceType.forValue(selectedNicType)))
        {
            selectedNicType = DataProvider.GetDefaultNicType(vm.getvm_os()).getValue();
        }

        model.getNicType().setSelectedItem(VmInterfaceType.forValue(selectedNicType));

        model.getName().setEntity(nic.getName());
        model.getMAC().setIsChangable(false);
        model.getMAC().setEntity(nic.getMacAddress());

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model1, Object result1)
            {
                VmInterfaceListModel vmInterfaceListModel = (VmInterfaceListModel) model1;
                VmInterfaceModel vmInterfaceModel = (VmInterfaceModel) vmInterfaceListModel.getWindow();

                java.util.ArrayList<network> networks = new java.util.ArrayList<network>();
                for (network a : (java.util.ArrayList<network>) result1)
                {
                    if (a.getStatus() == NetworkStatus.Operational)
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
                    for (network a : networks)
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
        AsyncDataProvider.GetClusterNetworkList(_asyncQuery, vm.getvds_group_id());

        UICommand tempVar = new UICommand("OnSave", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
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
        nic.setNetworkName(((network) model.getNetwork().getSelectedItem()).getname());
        if (model.getNicType().getSelectedItem() == null)
        {
            nic.setType(null);
        }
        else
        {
            nic.setType(((VmInterfaceType) model.getNicType().getSelectedItem()).getValue());
        }
        nic.setMacAddress(model.getMAC().getIsChangable() ? (model.getMAC().getEntity() == null ? null
                : ((String) (model.getMAC().getEntity())).toLowerCase()) : model.getIsNew() ? "" : nic.getMacAddress());

        model.StartProgress(null);

        Frontend.RunAction(model.getIsNew() ? VdcActionType.AddVmInterface : VdcActionType.UpdateVmInterface,
                new AddVmInterfaceParameters(vm.getvm_guid(), nic),
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

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle("Remove Network Interface(s)");
        model.setHashName("remove_network_interface_vms");
        model.setMessage("Network Interface(s)");

        java.util.ArrayList<String> items = new java.util.ArrayList<String>();
        for (Object item : getSelectedItems())
        {
            VmNetworkInterface a = (VmNetworkInterface) item;
            items.add(a.getName());
        }
        model.setItems(items);

        UICommand tempVar = new UICommand("OnRemove", this);
        tempVar.setTitle("OK");
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this);
        tempVar2.setTitle("Cancel");
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    private void OnRemove()
    {
        VM vm = (VM) getEntity();
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null)
        {
            return;
        }

        java.util.ArrayList<VdcActionParametersBase> list = new java.util.ArrayList<VdcActionParametersBase>();
        for (Object item : getSelectedItems())
        {
            VmNetworkInterface a = (VmNetworkInterface) item;
            RemoveVmInterfaceParameters parameters = new RemoveVmInterfaceParameters(vm.getvm_guid(), a.getId());
            list.add(parameters);

        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.RemoveVmInterface, list,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        ConfirmationModel localModel = (ConfirmationModel) result.getState();
                        localModel.StopProgress();
                        Cancel();

                    }
                }, model);
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

        if (e.PropertyName.equals("status"))
        {
            UpdateActionAvailability();
        }
    }

    private void UpdateActionAvailability()
    {
        VM vm = (VM) getEntity();

        java.util.ArrayList<VM> items = new java.util.ArrayList<VM>();
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
                && VdcActionUtils.CanExecute(items, VM.class, VdcActionType.RemoveVmInterface)
                && (getSelectedItems() != null && getSelectedItems().size() > 0));
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
        else if (StringHelper.stringsEqual(command.getName(), "OnSave"))
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove"))
        {
            OnRemove();
        }
    }

    @Override
    protected String getListName() {
        return "VmInterfaceListModel";
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }
}
