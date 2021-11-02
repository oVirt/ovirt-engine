package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericNameableComparator;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class HostInterfaceListModel extends SearchableListModel<VDS, HostInterfaceLineModel> {

    private UICommand privateSaveNetworkConfigCommand;

    public UICommand getSaveNetworkConfigCommand() {
        return privateSaveNetworkConfigCommand;
    }

    private void setSaveNetworkConfigCommand(UICommand value) {
        privateSaveNetworkConfigCommand = value;
    }

    private UICommand privateSetupNetworksCommand;

    public UICommand getSetupNetworksCommand() {
        return privateSetupNetworksCommand;
    }

    private void setSetupNetworksCommand(UICommand value) {
        privateSetupNetworksCommand = value;
    }

    private ArrayList<VdsNetworkInterface> privateOriginalItems;

    public ArrayList<VdsNetworkInterface> getOriginalItems() {
        return privateOriginalItems;
    }

    public void setOriginalItems(ArrayList<VdsNetworkInterface> value) {
        privateOriginalItems = value;
    }

    public UICommand getSyncAllHostNetworksCommand() {
        return syncAllHostNetworksCommand;
    }

    public void setSyncAllHostNetworksCommand(UICommand privateSyncAllHostNetworkCommand) {
        this.syncAllHostNetworksCommand = privateSyncAllHostNetworkCommand;
    }

    private UICommand syncAllHostNetworksCommand;

    private boolean showVirtualFunctions;

    public boolean isShowVirtualFunctions() {
        return showVirtualFunctions;
    }

    public void setShowVirtualFunctions(boolean showVirtualFunctions) {
        this.showVirtualFunctions = showVirtualFunctions;
    }

    private Map<Guid, Guid> vfToPfMap;

    private boolean isNetworkOperationInProgress;

    public boolean isNetworkOperationInProgress() {
        return isNetworkOperationInProgress;
    }

    @Override
    public Collection<HostInterfaceLineModel> getItems() {
        return super.items;
    }

    @Override
    public void setItems(Collection<HostInterfaceLineModel> value) {
        if (items != value) {
            itemsChanging(value, items);
            items = value;
            itemsChanged();
            getItemsChangedEvent().raise(this, EventArgs.EMPTY);
            onPropertyChanged(new PropertyChangedEventArgs("Items")); //$NON-NLS-1$
        }
    }

    public void setEntity(VDS value) {
        if (super.getEntity() != null && value != null) {
            VDS currentItem = super.getEntity();

            Guid currentItemId = currentItem.getId();
            Guid newItemId = value.getId();

            if (currentItemId.equals(newItemId)) {
                setEntity(value, false);
                updateActionAvailability();
                return;
            }
        }

        super.setEntity(value);
    }

    public HostInterfaceListModel() {
        setTitle(ConstantsManager.getInstance().getConstants().networkInterfacesTitle());
        setHelpTag(HelpTag.network_interfaces);
        setHashName("network_interfaces"); //$NON-NLS-1$

        setSaveNetworkConfigCommand(new UICommand("SaveNetworkConfig", this)); //$NON-NLS-1$
        setSetupNetworksCommand(new UICommand("SetupNetworks", this)); //$NON-NLS-1$
        setSyncAllHostNetworksCommand(new UICommand("SyncAllHostNetworks", this)); //$NON-NLS-1$
        setShowVirtualFunctions(false);

        updateActionAvailability();
    }

    @Override
    public void search() {
        if (getEntity() != null) {
            super.search();
        }
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();

        getSearchCommand().execute();
        updateActionAvailability();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e) {
        super.entityPropertyChanged(sender, e);

        if (e.propertyName.equals("status") || e.propertyName.equals("net_config_dirty")) { //$NON-NLS-1$ //$NON-NLS-2$
            updateActionAvailability();
        }
    }

    @Override
    protected void syncSearch() {
        super.syncSearch();

        IdQueryParameters tempVar = new IdQueryParameters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        queryVirtualFunctionMap(tempVar);
        queryIsHostLockedOnNetworkOperation(tempVar);
    }

    private void queryIsHostLockedOnNetworkOperation(IdQueryParameters idQueryParameters) {
            Frontend.getInstance()
                .runQuery(QueryType.IsHostLockedOnNetworkOperation,
                        idQueryParameters,
                        new AsyncQuery<QueryReturnValue>(returnValue ->
                            isNetworkOperationInProgress = returnValue.getReturnValue())
                        );
    }

    private void queryVirtualFunctionMap(IdQueryParameters idQueryParameters) {
        Frontend.getInstance()
                .runQuery(QueryType.GetVfToPfMapByHostId,
                        idQueryParameters,
                        new AsyncQuery<QueryReturnValue>(returnValue -> {
                            vfToPfMap = Optional.ofNullable((Map<Guid, Guid>) returnValue.getReturnValue())
                                    .orElse(Collections.emptyMap());
                            queryHostInterfaces(idQueryParameters);
                        }));
    }

    private void queryHostInterfaces(IdQueryParameters idQueryParameters) {
        Frontend.getInstance()
                .runQuery(QueryType.GetVdsInterfacesByVdsId,
                        idQueryParameters,
                        new AsyncQuery<QueryReturnValue>(returnValue -> {
                            List<VdsNetworkInterface> items = returnValue.getReturnValue();
                            updateItems(items);
                        }));
    }

    private void updateItems(Iterable<VdsNetworkInterface> source) {
        ArrayList<HostInterfaceLineModel> items = new ArrayList<>();
        setOriginalItems((ArrayList<VdsNetworkInterface>) source);

        List<Bond> nonEmptyBonds = new ArrayList<>();
        List<Nic> independentNics = new ArrayList<>();
        Map<String, List<Nic>> bondToNics = new HashMap<>();
        Map<String, List<Vlan>> nicToVlans = new HashMap<>();

        sortNics();
        classifyNics(nonEmptyBonds, independentNics, bondToNics, nicToVlans);

        // create all bond models
        for (Bond bond : nonEmptyBonds) {
            HostInterfaceLineModel model = lineModelFromBond(bond);
            items.add(model);

            // add contained interface models - should exist, but check just in case
            if (bondToNics.containsKey(bond.getName())) {
                for (Nic nic : bondToNics.get(bond.getName())) {
                    model.getInterfaces().add(hostInterfaceFromNic(nic));
                }
            }

            // add any corresponding VLAN bridge models
            model.getVLans().addAll(gatherVlans(bond, nicToVlans));
        }

        // create all independent NIC models
        for (Nic nic : independentNics) {
            if (isVirtualFunction(nic) && !isShowVirtualFunctions()) {
                continue;
            }
            HostInterfaceLineModel model = lineModelFromInterface(nic);
            model.getInterfaces().add(hostInterfaceFromNic(nic));
            items.add(model);

            // add any corresponding VLAN bridge models
            model.getVLans().addAll(gatherVlans(nic, nicToVlans));
        }

        setItems(items);
        updateActionAvailability();
    }

    private boolean isVirtualFunction(Nic nic) {
        return vfToPfMap.containsKey(nic.getId());
    }

    private List<HostVLan> gatherVlans(VdsNetworkInterface nic, Map<String, List<Vlan>> nicToVlans) {
        List<HostVLan> hostVlanList = new ArrayList<>();
        if (nicToVlans.containsKey(nic.getName())) {
            for (Vlan vlan : nicToVlans.get(nic.getName())) {
                hostVlanList.add(hostVlanFromNic(vlan));
            }
        }
        return hostVlanList;
    }

    private void sortNics() {
        Collections.sort(getOriginalItems(), new LexoNumericNameableComparator<>());
    }

    private void classifyNics(List<Bond> nonEmptyBonds,
            List<Nic> independentNics,
            Map<String, List<Nic>> bondToNics,
            Map<String, List<Vlan>> nicToVlans) {
        for (VdsNetworkInterface nic : getOriginalItems()) {
            if (nic instanceof Bond) {
                nonEmptyBonds.add((Bond) nic);
            } else if (nic instanceof Nic) {
                if (nic.getBondName() == null) {
                    independentNics.add((Nic) nic);
                } else {
                    if (bondToNics.containsKey(nic.getBondName())) {
                        bondToNics.get(nic.getBondName()).add((Nic) nic);
                    } else {
                        List<Nic> nicList = new ArrayList<>();
                        nicList.add((Nic) nic);
                        bondToNics.put(nic.getBondName(), nicList);
                    }
                }
            } else if (nic instanceof Vlan) {
                String nameWithoutVlan = nic.getBaseInterface();
                if (nicToVlans.containsKey(nameWithoutVlan)) {
                    nicToVlans.get(nameWithoutVlan).add((Vlan) nic);
                } else {
                    List<Vlan> vlanList = new ArrayList<>();
                    vlanList.add((Vlan) nic);
                    nicToVlans.put(nameWithoutVlan, vlanList);
                }
            }
        }
    }

    private HostInterfaceLineModel lineModelFromInterface(VdsNetworkInterface nic) {
        HostInterfaceLineModel model = new HostInterfaceLineModel();
        model.setInterfaces(new ArrayList<HostInterface>());
        model.setVLans(new ArrayList<HostVLan>());
        model.setNetworkName(nic.getNetworkName());
        model.setIsManagement(nic.getIsManagement());
        model.setIpv4Address(nic.getIpv4Address());
        model.setIpv6Address(nic.getIpv6Address());

        return model;
    }

    private HostInterfaceLineModel lineModelFromBond(VdsNetworkInterface nic) {
        HostInterfaceLineModel model = lineModelFromInterface(nic);
        model.setInterface(nic);
        model.setIsBonded(true);
        model.setBondName(nic.getName());
        model.setIpv4Address(nic.getIpv4Address());
        model.setIpv6Address(nic.getIpv6Address());

        return model;
    }

    private HostInterface hostInterfaceFromNic(VdsNetworkInterface nic) {
        HostInterface hi = new HostInterface();
        hi.setInterface(nic);
        hi.setName(nic.getName());
        hi.setIpv4Address(nic.getIpv4Address());
        hi.setIpv6Address(nic.getIpv6Address());
        hi.setMAC(nic.getMacAddress());
        hi.setSpeed(nic.getSpeed());
        hi.setRxRate(nic.getStatistics().getReceiveRate());
        hi.setRxTotal(nic.getStatistics().getReceivedBytes());
        hi.setRxDrop(nic.getStatistics().getReceiveDrops());
        hi.setTxRate(nic.getStatistics().getTransmitRate());
        hi.setTxTotal(nic.getStatistics().getTransmittedBytes());
        hi.setTxDrop(nic.getStatistics().getTransmitDrops());
        hi.setStatus(nic.getStatistics().getStatus());
        hi.getPropertyChangedEvent().addListener(this);

        return hi;
    }

    private HostVLan hostVlanFromNic(VdsNetworkInterface nic) {
        HostVLan hv = new HostVLan();
        hv.setInterface(nic);
        hv.setName(nic.getName());
        hv.setNetworkName(nic.getNetworkName());
        hv.setIpv4Address(nic.getIpv4Address());
        hv.setIpv6Address(nic.getIpv6Address());
        hv.getPropertyChangedEvent().addListener(this);

        return hv;
    }

    public void saveNetworkConfig() {
        if (getWindow() != null) {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().saveNetworkConfigurationTitle());
        model.setHelpTag(HelpTag.save_network_configuration);
        model.setHashName("save_network_configuration"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToMakeTheChangesPersistentMsg());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSaveNetworkConfig", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    public void onSaveNetworkConfig() {
        ConfirmationModel model = (ConfirmationModel) getWindow();

        if (model.getProgress() != null) {
            return;
        }

        model.startProgress();
        new SaveNetworkConfigAction(this, model, getEntity()).execute();
    }

    public void cancel() {
        setConfirmWindow(null);
        setWindow(null);
    }

    public void cancelConfirm() {
        setConfirmWindow(null);
    }

    public void setupNetworks() {

        if (getWindow() != null) {
            return;
        }

        HostSetupNetworksModel setupNetworksWindowModel = new HostSetupNetworksModel(this, getEntity());
        setWindow(setupNetworksWindowModel);
    }

    private void updateActionAvailability() {
        VDS host = getEntity();

        getSaveNetworkConfigCommand().setIsExecutionAllowed(host != null
                && (host.getNetConfigDirty() == null ? false : host.getNetConfigDirty()));

        getSyncAllHostNetworksCommand().setIsExecutionAllowed(getOriginalItems() != null
                && getOriginalItems().stream()
                .map(hostInterface -> hostInterface.getNetworkImplementationDetails())
                .filter(Objects::nonNull)
                .anyMatch(implementationDetails -> implementationDetails.isManaged()
                        && !implementationDetails.isInSync()));
    }

    public void syncAllHostNetworks() {
        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().syncAllHostNetworkConfirmationDialogTitle());
        model.setHelpTag(HelpTag.sync_all_host_networks);
        model.setHashName("sync_all_host_networks"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().areYouSureYouWantToSyncAllHostNetworksMsg());

        UICommand tempVar = UICommand.createDefaultOkUiCommand("OnSyncAllHostNetworkConfirm", this); //$NON-NLS-1$
        model.getCommands().add(tempVar);
        UICommand tempVar2 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        model.getCommands().add(tempVar2);
    }

    private void onSyncAllHostNetworkConfirm(){
        ConfirmationModel model = (ConfirmationModel) getWindow();
        if (model.getProgress() != null) {
            return;
        }
        getWindow().startProgress();
        Frontend.getInstance().runAction(ActionType.SyncAllHostNetworks,
                new PersistentHostSetupNetworksParameters(getEntity().getId()),
                result -> {
                    getWindow().stopProgress();
                    cancel();
                },
                null);
    }

    private void onSyncAllHostNetworkCancelConfirm(){
        cancelConfirm();
    }

    public void onShowHideVirtualFunction(boolean show) {
        setShowVirtualFunctions(show);
        updateItems(getOriginalItems());
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if (command == getSetupNetworksCommand()) {
            setupNetworks();
        } else if (command == getSaveNetworkConfigCommand()) {
            saveNetworkConfig();
        } else if (command == getSyncAllHostNetworksCommand()) {
            syncAllHostNetworks();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        } else if ("OnSaveNetworkConfig".equals(command.getName())) { //$NON-NLS-1$
            onSaveNetworkConfig();
        } else if ("OnSyncAllHostNetworkConfirm".equals(command.getName())) { //$NON-NLS-1$
            onSyncAllHostNetworkConfirm();
        }
    }

    @Override
    protected String getListName() {
        return "HostInterfaceListModel"; //$NON-NLS-1$
    }

    @Override
    protected boolean isSingleSelectionOnly() {
        // Single selection model
        return true;
    }
}
