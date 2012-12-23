package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
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
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class VmInterfaceListModel extends SearchableListModel
{

    private boolean isHotPlugSupported;
    private UICommand privateNewCommand;
    private UICommand privateEditCommand;
    private UICommand privateRemoveCommand;

    public VmInterfaceListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHashName("network_interfaces"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        initSelectionGeustAgentData(getSelectedItem());
        UpdateActionAvailability();
    }

    private List<VmGuestAgentInterface> guestAgentData;

    private List<VmGuestAgentInterface> selectionGuestAgentData;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }


    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    public List<VmGuestAgentInterface> getGuestAgentData() {
        return guestAgentData;
    }

    public void setGuestAgentData(List<VmGuestAgentInterface> guestAgentData) {
        this.guestAgentData = guestAgentData;
    }

    public List<VmGuestAgentInterface> getSelectionGuestAgentData() {
        return selectionGuestAgentData;
    }

    public void setSelectionGuestAgentData(List<VmGuestAgentInterface> selectedGuestAgentData) {
        this.selectionGuestAgentData = selectedGuestAgentData;
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

        final VM vm = getEntity();

        // Initialize guest agent data
        AsyncQuery getVmGuestAgentInterfacesByVmIdQuery = new AsyncQuery();
        getVmGuestAgentInterfacesByVmIdQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                setGuestAgentData((List<VmGuestAgentInterface>) result);
                VmInterfaceListModel.super.SyncSearch(VdcQueryType.GetVmInterfacesByVmId, new GetVmByVmIdParameters(vm.getId()));
            }
        };
        AsyncDataProvider.getVmGuestAgentInterfacesByVmId(getVmGuestAgentInterfacesByVmIdQuery, vm.getId());
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        VM vm = getEntity();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetVmInterfacesByVmId,
                new GetVmByVmIdParameters(vm.getId())));
        setItems(getAsyncResult().getData());
    }

    private void New()
    {
        VM vm = getEntity();

        if (getWindow() != null)
        {
            return;
        }

        VmInterfaceModel model =
                NewVmInterfaceModel.createInstance(getEntity().getStaticData(),
                        getEntity().getVdsGroupCompatibilityVersion(), (ArrayList<VmNetworkInterface>) getItems(), this);
        setWindow(model);
    }

    private void Edit()
    {
        if (getWindow() != null)
        {
            return;
        }

        VmInterfaceModel model =
                EditVmInterfaceModel.createInstance(getEntity().getStaticData(), getEntity(),
                        getEntity().getVdsGroupCompatibilityVersion(),
                        (ArrayList<VmNetworkInterface>) getItems(),
                        (VmNetworkInterface) getSelectedItem(), this);
        setWindow(model);
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
        VM vm = getEntity();

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
    }

    private boolean canRemoveNics() {
        VM vm = getEntity();
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
    }

    @Override
    protected String getListName() {
        return "VmInterfaceListModel"; //$NON-NLS-1$
    }

    @Override
    protected void OnSelectedItemChanging(Object newValue, Object oldValue) {
        initSelectionGeustAgentData(newValue);
        super.OnSelectedItemChanging(newValue, oldValue);
    }

    private void initSelectionGeustAgentData(Object selectedItem) {
        if (selectedItem == null || getGuestAgentData() == null){
            setSelectionGuestAgentData(null);
            return;
        }
        List<VmGuestAgentInterface> selectionInterfaces = new ArrayList<VmGuestAgentInterface>();

        for (VmGuestAgentInterface guestInterface : getGuestAgentData()) {
            if (StringHelper.stringsEqual(guestInterface.getMacAddress(),
                    ((VmNetworkInterface) selectedItem).getMacAddress())) {
                selectionInterfaces.add(guestInterface);
            }
        }

        setSelectionGuestAgentData(selectionInterfaces);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    protected void updateConfigValues()
    {
        if (getEntity() == null)
        {
            return;
        }
        VM vm = getEntity();
        Version clusterCompatibilityVersion = vm.getVdsGroupCompatibilityVersion() != null
                ? vm.getVdsGroupCompatibilityVersion() : new Version();

        isHotPlugSupported =
                (Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.HotPlugEnabled,
                        clusterCompatibilityVersion.toString());
    }

    @Override
    public VM getEntity() {
        return (VM) super.getEntity();
    }

    @Override
    public void setEntity(Object value) {
        super.setEntity(value);
        updateConfigValues();
    }
}
