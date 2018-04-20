package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ovirt.engine.core.common.ActionUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import com.google.gwt.core.client.Scheduler;

public class VmInterfaceListModel extends SearchableListModel<VM, VmNetworkInterface> {

    private UICommand privateNewCommand;
    private UICommand privateEditCommand;
    private UICommand privateRemoveCommand;

    public VmInterfaceListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHelpTag(HelpTag.network_interfaces);
        setHashName("network_interfaces"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        initSelectionGuestAgentData(getSelectedItem());
        updateActionAvailability();
    }

    private List<VmGuestAgentInterface> guestAgentData;

    private List<VmGuestAgentInterface> selectionGuestAgentData;

    private Map<Guid, List<VmNicFilterParameter>> mapNicFilterParameter;

    public UICommand getNewCommand() {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value) {
        privateNewCommand = value;
    }


    @Override
    public UICommand getEditCommand() {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value) {
        privateEditCommand = value;
    }

    public UICommand getRemoveCommand() {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value) {
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

    public Map<Guid, List<VmNicFilterParameter>> getMapNicFilterParameter() {
        return mapNicFilterParameter;
    }

    public void setMapNicFilterParameter(Map<Guid, List<VmNicFilterParameter>> mapNicFilterParameter) {
        this.mapNicFilterParameter = mapNicFilterParameter;
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        if (getEntity() != null) {
            getSearchCommand().execute();
        }

        updateActionAvailability();
    }

    @Override
    protected void syncSearch() {
        if (getEntity() == null) {
            return;
        }

        final VM vm = getEntity();

        // Initialize guest agent data
        AsyncDataProvider.getInstance().getVmGuestAgentInterfacesByVmId(new AsyncQuery<>(result -> {
            setGuestAgentData(result);
            VmInterfaceListModel.super.syncSearch(QueryType.GetVmInterfacesByVmId, new IdQueryParameters(vm.getId()),
                    new AsyncQuery<>(returnValue -> updateNetworkFilterParameterMap(returnValue.getReturnValue())));
        }), vm.getId());
    }

    private void updateNetworkFilterParameterMap(List<VmNetworkInterface> vmInterfaces) {
        List<QueryType> queryTypes = new ArrayList<>();
        List<QueryParametersBase> queryParametersBases = new ArrayList<>();

        vmInterfaces.stream().forEach(iface -> {
            queryTypes.add(QueryType.GetVmInterfaceFilterParametersByVmInterfaceId);
            queryParametersBases.add(new IdQueryParameters(iface.getId()));
        });

        final IFrontendMultipleQueryAsyncCallback callback = multiResult -> {
            Map<Guid, List<VmNicFilterParameter>> networkFilterMap = new HashMap<>(vmInterfaces.size());
            for (int i = 0; i < multiResult.getReturnValues().size(); i++) {
                List<VmNicFilterParameter> params = multiResult.getReturnValues().get(i).getReturnValue();
                networkFilterMap.put(vmInterfaces.get(i).getId(), params);
            }
            setMapNicFilterParameter(networkFilterMap);
            setItems(vmInterfaces);
        };

        if (vmInterfaces.isEmpty()) {
            setItems(vmInterfaces);
        }

        Frontend.getInstance().runMultipleQueries(queryTypes, queryParametersBases, callback);
    }

    private void newEntity() {
        if (getWindow() != null) {
            return;
        }

        VmInterfaceModel model =
                NewVmInterfaceModel.createInstance(getEntity().getStaticData(),
                        getEntity().getStatus(),
                        getEntity().getStoragePoolId(),
                        getEntity().getClusterCompatibilityVersion(),
                        (ArrayList<VmNetworkInterface>) getItems(),
                        this);
        setWindow(model);
    }

    private void edit() {
        if (getWindow() != null) {
            return;
        }

        VmInterfaceModel model =
                EditVmInterfaceModel.createInstance(getEntity().getStaticData(), getEntity(),
                        getEntity().getClusterCompatibilityVersion(),
                        (ArrayList<VmNetworkInterface>) getItems(),
                        getSelectedItem(), this);
        setWindow(model);
    }

    private void remove() {
        if (getWindow() != null) {
            return;
        }
        RemoveVmInterfaceModel model = new RemoveVmInterfaceModel(this, getSelectedItems(), false);
        setWindow(model);
    }

    @Override
    protected void selectedItemsChanged() {
        super.selectedItemsChanged();
        updateActionAvailability();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status")) { //$NON-NLS-1$
            updateActionAvailability();
        }
    }

    private void updateActionAvailability() {
        VM vm = getEntity();

        ArrayList<VM> items = new ArrayList<>();
        if (vm != null) {
            items.add(vm);
        }

        getNewCommand().setIsExecutionAllowed(vm != null
                && ActionUtils.canExecute(items, VM.class, ActionType.AddVmInterface));
        getEditCommand().setIsExecutionAllowed(vm != null
                && ActionUtils.canExecute(items, VM.class, ActionType.UpdateVmInterface)
                && getSelectedItems() != null && getSelectedItems().size() == 1);
        getRemoveCommand().setIsExecutionAllowed(vm != null
                && ActionUtils.canExecute(items, VM.class, ActionType.RemoveVmInterface) && canRemoveNics()
                && getSelectedItems() != null && !getSelectedItems().isEmpty());
    }

    private boolean canRemoveNics() {
        VM vm = getEntity();
        if (VMStatus.Down.equals(vm.getStatus())) {
            return true;
        }

        List<VmNetworkInterface> nics = getSelectedItems() != null ? getSelectedItems()
                : new ArrayList<VmNetworkInterface>();

        for (VmNetworkInterface nic : nics) {
            if (nic.isPlugged()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getNewCommand()) {
            newEntity();
        } else if (command == getEditCommand()) {
            edit();
        } else if (command == getRemoveCommand()) {
            remove();
        }
    }

    @Override
    protected String getListName() {
        return "VmInterfaceListModel"; //$NON-NLS-1$
    }

    @Override
    protected void onSelectedItemChanging(VmNetworkInterface newValue, VmNetworkInterface oldValue) {
        initSelectionGuestAgentData(newValue);
        super.onSelectedItemChanging(newValue, oldValue);
    }

    private void initSelectionGuestAgentData(VmNetworkInterface selectedItem) {
        if (selectedItem == null || getGuestAgentData() == null){
            setSelectionGuestAgentData(null);
            return;
        }
        List<VmGuestAgentInterface> selectionInterfaces = new ArrayList<>();

        for (VmGuestAgentInterface guestInterface : getGuestAgentData()) {
            if (Objects.equals(guestInterface.getMacAddress(), selectedItem.getMacAddress())) {
                selectionInterfaces.add(guestInterface);
            }
        }

        setSelectionGuestAgentData(selectionInterfaces);
    }

    @Override
    protected void onSelectedItemChanged() {
        super.onSelectedItemChanged();
        updateActionAvailability();
    }

    @Override
    public void setItems(Collection<VmNetworkInterface> value) {
        super.setItems(value);
        // We need to defer this so we can give the selection model a chance to resolve the changes and syncing the
        // seletectedItems with the selection model. This is to prevent a situation where we select nothing, due to
        // removing a vnic, which causes a refresh of the list model, and we auto select the first one. The refresh
        // will trigger a check of existing selection in the the patternfly list view. That combined with the auto
        // select the first entity below will select 2 items.
        Scheduler.get().scheduleDeferred(() -> {
            if (getSelectedItem() == null && (getSelectedItems() == null || getSelectedItems().size() == 0)) {
                if (value != null && value.iterator().hasNext()) {
                    getSelectionModel().setSelected(value.iterator().next(), true);
                }
            }
        });
    }
}
