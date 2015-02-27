package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.action.SimpleAction;
import org.ovirt.engine.ui.uicommonweb.action.UiAction;
import org.ovirt.engine.ui.uicommonweb.action.UiVdcAction;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel.AllNetworksSelector;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkCommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel.NewNetworkLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperationFactory;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.OperationCandidateEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.UIMessages;

/**
 * A Model for the Setup Networks Dialog<BR>
 * The Entity is the VDS being edited.<BR>
 * The Dialog holds two different Lists: NIC Models, and Network Models.<BR>
 * These two Lists are fetched from the backend, and cannot be changed by the User.<BR>
 * The user only changes the topology of their connections.
 */
public class HostSetupNetworksModel extends EntityModel<VDS> {

    private EntityModel<Boolean> checkConnectivity;

    private LogicalNetworkModel managementNetworkModel;

    public EntityModel<Boolean> getCheckConnectivity() {
        return checkConnectivity;
    }

    private void setCheckConnectivity(EntityModel<Boolean> value) {
        checkConnectivity = value;
    }

    private EntityModel<Integer> connectivityTimeout;

    public EntityModel<Integer> getConnectivityTimeout() {
        return connectivityTimeout;
    }

    private void setConnectivityTimeout(EntityModel<Integer> value) {
        connectivityTimeout = value;
    }

    private EntityModel<Boolean> commitChanges;

    public EntityModel<Boolean> getCommitChanges() {
        return commitChanges;
    }

    public void setCommitChanges(EntityModel<Boolean> value) {
        commitChanges = value;
    }

    private static final EventDefinition NICS_CHANGED_EVENT_DEFINITION = new EventDefinition("NicsChanged", //$NON-NLS-1$
            HostSetupNetworksModel.class);

    private static final EventDefinition OPERATION_CANDIDATE_EVENT_DEFINITION =
            new EventDefinition("OperationCandidate", NetworkOperationFactory.class); //$NON-NLS-1$

    private Event<OperationCandidateEventArgs> operationCandidateEvent;

    private Event<EventArgs> nicsChangedEvent;

    private List<VdsNetworkInterface> allNics;

    private Map<String, NetworkInterfaceModel> nicMap;

    private Map<String, LogicalNetworkModel> networkMap;

    private Map<String, NetworkLabelModel> networkLabelMap = new HashMap<>();

    private final NewNetworkLabelModel newLabelModel;

    private Map<String, String> labelToIface = new HashMap<>();

    private final List<String> networksToSync = new ArrayList<>();

    // The purpose of this map is to keep the network parameters while moving the network from one nic to another
    private final Map<String, NetworkParameters> networkToLastDetachParams;

    private Set<HostNicVfsConfig> originalVfsConfigs = new HashSet<>();
    private Map<Guid, HostNicVfsConfig> nicToVfsConfig = new HashMap<>();

    private NetworkOperationFactory operationFactory;
    private List<Network> allNetworks;
    private final Map<String, DcNetworkParams> netTodcParams;
    private final Map<String, NetworkParameters> netToBeforeSyncParams;
    private final SearchableListModel sourceListModel;
    private List<VdsNetworkInterface> allBonds;
    private SortedSet<String> dcLabels;
    private NetworkOperation currentCandidate;
    private NetworkItemModel<?> currentOp1;
    private NetworkItemModel<?> currentOp2;
    private String nextBondName;

    private final UICommand okCommand;
    public static final String NIC = "nic"; //$NON-NLS-1$
    public static final String NETWORK = "network"; //$NON-NLS-1$
    public static final String LABEL = "label"; //$NON-NLS-1$

    public HostSetupNetworksModel(SearchableListModel listModel, VDS host) {
        this.sourceListModel = listModel;
        setEntity(host);

        setTitle(ConstantsManager.getInstance().getMessages().setupHostNetworksTitle(host.getName()));
        setHelpTag(HelpTag.host_setup_networks);
        setHashName("host_setup_networks"); //$NON-NLS-1$

        networkToLastDetachParams = new HashMap<>();
        netTodcParams = new HashMap<>();
        netToBeforeSyncParams = new HashMap<>();
        setNicsChangedEvent(new Event<>(NICS_CHANGED_EVENT_DEFINITION));
        setOperationCandidateEvent(new Event<OperationCandidateEventArgs>(OPERATION_CANDIDATE_EVENT_DEFINITION));
        setCheckConnectivity(new EntityModel<Boolean>());
        getCheckConnectivity().setEntity(true);
        setConnectivityTimeout(new EntityModel<Integer>());
        setCommitChanges(new EntityModel<Boolean>());
        getCommitChanges().setEntity(true);

        // ok command
        okCommand = UICommand.createDefaultOkUiCommand("OnSetupNetworks", this); //$NON-NLS-1$
        getCommands().add(okCommand);

        // cancel command
        getCommands().add(UICommand.createCancelUiCommand("Cancel", this)); //$NON-NLS-1$

        newLabelModel = new NewNetworkLabelModel(this);
    }

    public NetworkLabelModel getNewNetworkLabelModel() {
        return newLabelModel;
    }

    private NetworkItemModel<?> getItemModel(String key, String type) {
        if (type != null) {
            switch(type) {
            case NIC:
                return nicMap.get(key);
            case NETWORK:
                return networkMap.get(key);
            case LABEL:
                NetworkLabelModel labelModel = networkLabelMap.get(key);
                return labelModel == null ? newLabelModel : labelModel;
            }
        }

        return null;
    }

    public boolean candidateOperation(String op1Key, String op1Type, String op2Key, String op2Type, boolean drop) {
        NetworkItemModel<?> op1 = getItemModel(op1Key, op1Type);
        NetworkItemModel<?> op2 = getItemModel(op2Key, op2Type);

        if (op1 == null) {
            throw new IllegalArgumentException("null Operands"); //$NON-NLS-1$
        }

        NetworkOperation candidate = NetworkOperationFactory.operationFor(op1, op2, true);

        if (drop) {
            onOperation(candidate, candidate.getCommand(op1, op2, allNics));
        }

        // raise the candidate event only if it was changed
        if (!candidate.equals(currentCandidate) || !equals(op1, currentOp1) || !equals(op2, currentOp2)) {
            currentCandidate = candidate;
            currentOp1 = op1;
            currentOp2 = op2;
            getOperationCandidateEvent().raise(this, new OperationCandidateEventArgs(candidate, op1, op2));
        }
        return !candidate.isNullOperation();
    }

    public Map<NetworkOperation, List<NetworkCommand>> commandsFor(NetworkItemModel<?> item) {
        return operationFactory.commandsFor(item, allNics);
    }

    public List<VdsNetworkInterface> getAllNics() {
        return allNics;
    }

    public List<LogicalNetworkModel> getNetworks() {
        return new ArrayList<>(networkMap.values());
    }

    public List<NetworkInterfaceModel> getNics() {
        return new ArrayList<>(nicMap.values());
    }

    public List<NetworkLabelModel> getLabels() {
        return new ArrayList<>(networkLabelMap.values());
    }

    public Event<EventArgs> getNicsChangedEvent() {
        return nicsChangedEvent;
    }

    public Event<OperationCandidateEventArgs> getOperationCandidateEvent() {
        return operationCandidateEvent;
    }

    private void commitVfsConfigChanges(final HostNicVfsConfig hostNicVfsConfig,
            final VfsConfigModel vfsConfigModel) {
        if (hostNicVfsConfig != null) {
            // Num of vfs
            hostNicVfsConfig.setNumOfVfs(vfsConfigModel.getNumOfVfs().getEntity());

            // Networks
            hostNicVfsConfig.setAllNetworksAllowed(vfsConfigModel
                    .getAllNetworksAllowed().getSelectedItem() == AllNetworksSelector.allNetworkAllowed);
            Set<Guid> networks = new HashSet<>();
            for (VfsConfigNetwork vfsConfigNetwork : vfsConfigModel.getNetworks().getItems()) {
                if (vfsConfigNetwork.isAttached() && vfsConfigNetwork.getLabelViaAttached() == null) {
                    networks.add(vfsConfigNetwork.getEntity().getId());
                }
            }
            hostNicVfsConfig.setNetworks(networks);

            // Labels
            hostNicVfsConfig.setNetworkLabels(vfsConfigModel.getLabelsModel().computeSelecetedLabels());
        }
    }

    public void onEdit(NetworkItemModel<?> item) {
        Model editPopup = null;
        BaseCommandTarget okTarget = null;
        if (item instanceof BondNetworkInterfaceModel) {
            /*****************
             * Bond Dialog
             *****************/
            final VdsNetworkInterface entity = ((NetworkInterfaceModel) item).getIface();
            editPopup = new SetupNetworksEditBondModel(entity);
            final SetupNetworksBondModel bondDialogModel = (SetupNetworksBondModel) editPopup;

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void executeCommand(UICommand command) {
                    if (!bondDialogModel.validate()) {
                        return;
                    }
                    sourceListModel.setConfirmWindow(null);
                    setBondOptions(entity, bondDialogModel);
                }
            };
        } else if (item instanceof NetworkInterfaceModel) {
            /*******************
             * VFs Config Dialog
             *******************/
            final VdsNetworkInterface entity = ((NetworkInterfaceModel) item).getIface();
            final HostNicVfsConfig hostNicVfsConfig = nicToVfsConfig.get(entity.getId());
            if (hostNicVfsConfig != null) {
                final VfsConfigModel vfsConfigPopupModel = new VfsConfigModel(hostNicVfsConfig, allNetworks, dcLabels);
                vfsConfigPopupModel.setTitle(ConstantsManager.getInstance().getMessages().editHostNicVfsConfigTitle(entity.getName()));
                editPopup = vfsConfigPopupModel;

                // OK Target
                okTarget = new BaseCommandTarget() {
                    @Override
                    public void executeCommand(UICommand uiCommand) {
                        if (!vfsConfigPopupModel.validate()) {
                            return;
                        }
                        sourceListModel.setConfirmWindow(null);
                        commitVfsConfigChanges(hostNicVfsConfig, vfsConfigPopupModel);
                    }
                };
            }
        } else if (item instanceof LogicalNetworkModel) {
            /*****************
             * Network Dialog
             *****************/
            final LogicalNetworkModel logicalNetwork = (LogicalNetworkModel) item;
            final VdsNetworkInterface entity =
                    logicalNetwork.hasVlan() ? logicalNetwork.getVlanNicModel().getIface()
                            : logicalNetwork.getAttachedToNic().getIface();

            final HostInterfaceModel networkDialogModel;
            String version = getEntity().getVdsGroupCompatibilityVersion().getValue();
            if (logicalNetwork.isManagement()) {
                networkDialogModel = new HostManagementNetworkModel(true);
                networkDialogModel.setTitle(ConstantsManager.getInstance().getConstants().editManagementNetworkTitle());
                networkDialogModel.setEntity(logicalNetwork.getNetwork());
                networkDialogModel.setNoneBootProtocolAvailable(false);
                networkDialogModel.getInterface().setIsAvailable(false);
            } else {
                networkDialogModel = new HostInterfaceModel(true);
                networkDialogModel.setTitle(ConstantsManager.getInstance()
                        .getMessages()
                        .editNetworkTitle(logicalNetwork.getName()));
                networkDialogModel.getName().setIsAvailable(false);
                networkDialogModel.getNetwork().setIsChangeable(false);
                networkDialogModel.getGateway()
                        .setIsAvailable((Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.MultipleGatewaysSupported,
                                version));
            }

            networkDialogModel.getNetwork().setSelectedItem(logicalNetwork.getNetwork());
            networkDialogModel.setOriginalNetParams(netToBeforeSyncParams.get(logicalNetwork.getName()));
            networkDialogModel.getAddress().setEntity(entity.getAddress());
            networkDialogModel.getSubnet().setEntity(entity.getSubnet());
            networkDialogModel.getGateway().setEntity(entity.getGateway());
            networkDialogModel.setStaticIpChangeAllowed(!getEntity().getHostName().equals(entity.getAddress()));
            networkDialogModel.getBondingOptions().setIsAvailable(false);
            networkDialogModel.setBootProtocol(entity.getBootProtocol());

            if ((Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.HostNetworkQosSupported,
                    version)) {
                networkDialogModel.getQosOverridden().setIsAvailable(true);
                networkDialogModel.getQosModel().setIsAvailable(true);
                networkDialogModel.getQosOverridden().setEntity(entity.isQosOverridden());
                if (entity.isQosOverridden()) {
                    networkDialogModel.getQosModel().init(entity.getQos());
                } else {
                    Guid qosId = logicalNetwork.getNetwork().getQosId();
                    if (qosId != null) {
                        networkDialogModel.startProgress(null);
                        Frontend.getInstance().runQuery(VdcQueryType.GetQosById,
                                new IdQueryParameters(qosId),
                                new AsyncQuery(new INewAsyncCallback() {

                                    @Override
                                    public void onSuccess(Object model, Object returnValue) {
                                        networkDialogModel.getQosModel()
                                                .init((HostNetworkQos) ((VdcQueryReturnValue) returnValue).getReturnValue());
                                        networkDialogModel.stopProgress();
                                    }
                                }));
                    }
                }
            }

            if ((Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.NetworkCustomPropertiesSupported,
                    version)) {
                KeyValueModel customPropertiesModel = networkDialogModel.getCustomPropertiesModel();
                customPropertiesModel.setIsAvailable(true);
                Map<String, String> validProperties =
                        KeyValueModel.convertProperties((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.PreDefinedNetworkCustomProperties,
                                version));
                // TODO: extract this (and as much surrounding code as possible) into a custom properties utility common
                // to backend and frontend (lvernia)
                if (!logicalNetwork.getNetwork().isVmNetwork()) {
                    validProperties.remove("bridge_opts"); //$NON-NLS-1$
                }
                validProperties.putAll(KeyValueModel.convertProperties((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.UserDefinedNetworkCustomProperties,
                        version)));
                customPropertiesModel.setKeyValueMap(validProperties);
                customPropertiesModel.deserialize(KeyValueModel.convertProperties(entity.getCustomProperties()));
            }

            networkDialogModel.getIsToSync().setIsChangeable(!logicalNetwork.isInSync());
            networkDialogModel.getIsToSync()
                    .setEntity(networksToSync.contains(logicalNetwork.getName()));

            editPopup = networkDialogModel;

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void executeCommand(UICommand command) {
                    if (!networkDialogModel.validate()) {
                        return;
                    }
                    entity.setBootProtocol(networkDialogModel.getBootProtocol());
                    if (networkDialogModel.getIsStaticAddress()) {
                        entity.setAddress(networkDialogModel.getAddress().getEntity());
                        entity.setSubnet(networkDialogModel.getSubnet().getEntity());
                        entity.setGateway(networkDialogModel.getGateway().getEntity());
                    }

                    if (networkDialogModel.getQosModel().getIsAvailable()) {
                        boolean qosOverridden = networkDialogModel.getQosOverridden().getEntity();
                        HostNetworkQos qos = null;
                        entity.setQosOverridden(qosOverridden);
                        if (qosOverridden) {
                            qos = new HostNetworkQos();
                            networkDialogModel.getQosModel().flush(qos);
                        }
                        entity.setQos(qos);
                    }

                    if (networkDialogModel.getCustomPropertiesModel().getIsAvailable()) {
                        entity.setCustomProperties(KeyValueModel.convertProperties(networkDialogModel.getCustomPropertiesModel()
                                .serialize()));
                    }

                    if (networkDialogModel.getIsToSync().getEntity()) {
                        networksToSync.add(logicalNetwork.getName());
                    } else {
                        networksToSync.remove(logicalNetwork.getName());
                    }

                    sourceListModel.setConfirmWindow(null);
                }
            };
        }

        // ok command
        UICommand okCommand = UICommand.createDefaultOkUiCommand("OK", okTarget); //$NON-NLS-1$

        // cancel command
        UICommand cancelCommand = new UICommand("Cancel", new BaseCommandTarget() { //$NON-NLS-1$
                    @Override
                    public void executeCommand(UICommand command) {
                        sourceListModel.setConfirmWindow(null);
                    }
                });
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);

        if (editPopup != null) {
            editPopup.getCommands().add(okCommand);
            editPopup.getCommands().add(cancelCommand);
        }
        sourceListModel.setConfirmWindow(editPopup);
    }

    public void onOperation(NetworkOperation operation, final NetworkCommand networkCommand) {
        Model popupWindow;

        UICommand cancelCommand = new UICommand("Cancel", new BaseCommandTarget() { //$NON-NLS-1$
                    @Override
                    public void executeCommand(UICommand command) {
                        sourceListModel.setConfirmWindow(null);
                    }
                });
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);

        if (operation.isNullOperation()) {
            return;
        } else if (operation == NetworkOperation.BOND_WITH || operation == NetworkOperation.JOIN_BONDS) {
            final SetupNetworksBondModel bondPopup;
            final List<VdsNetworkInterface> srcIfaces = new ArrayList<>();
            srcIfaces.add(((NetworkInterfaceModel) networkCommand.getOp1()).getIface());
            srcIfaces.add(((NetworkInterfaceModel) networkCommand.getOp2()).getIface());
            if (operation == NetworkOperation.BOND_WITH) {
                bondPopup =
                        new SetupNetworksAddBondModel(getFreeBonds(), nextBondName);
            } else {
                bondPopup =
                        new SetupNetworksJoinBondsModel(getFreeBonds(),
                                (BondNetworkInterfaceModel) networkCommand.getOp1(),
                                (BondNetworkInterfaceModel) networkCommand.getOp2());
            }
            bondPopup.getCommands().add(new UICommand("OK", new BaseCommandTarget() { //$NON-NLS-1$

                        @Override
                        public void executeCommand(UICommand command) {
                            if (!bondPopup.validate()) {
                                return;
                            }
                            sourceListModel.setConfirmWindow(null);
                            VdsNetworkInterface bond = new Bond(bondPopup.getBond().getSelectedItem());
                            setBondOptions(bond, bondPopup);
                            NetworkInterfaceModel nic1 = (NetworkInterfaceModel) networkCommand.getOp1();
                            NetworkInterfaceModel nic2 = (NetworkInterfaceModel) networkCommand.getOp2();
                            List<LogicalNetworkModel> networks = new ArrayList<>();
                            networks.addAll(nic1.getItems());
                            networks.addAll(nic2.getItems());

                            networkCommand.execute(bond);
                            redraw();

                            // Attach the previous networks
                            commitNetworkChanges(bond, networks);

                            // Attach previous labels
                            commitLabelChanges(srcIfaces, bond);
                            redraw();
                        }
                    }));

            popupWindow = bondPopup;
        } else if (networkCommand.getOp1() == getNewNetworkLabelModel()) {
            final SetupNetworksLabelModel labelPopup = new SetupNetworksLabelModel(dcLabels);
            labelPopup.getCommands().add(new UICommand("OK", new BaseCommandTarget() { //$NON-NLS-1$

                        @Override
                        public void executeCommand(UICommand uiCommand) {
                            if (!labelPopup.validate()) {
                                return;
                            }

                            sourceListModel.setConfirmWindow(null);
                            String label = labelPopup.getLabel().getEntity();
                            dcLabels.add(label);
                            NetworkOperation.LABEL.getCommand(new NetworkLabelModel(label, HostSetupNetworksModel.this),
                                    networkCommand.getOp2(),
                                    allNics)
                                    .execute();
                            redraw();
                        }
                    }));
            popupWindow = labelPopup;
        } else {
            // just execute the command
            networkCommand.execute();
            redraw();
            return;
        }

        // add cancel
        popupWindow.getCommands().add(cancelCommand);

        // set window
        sourceListModel.setConfirmWindow(popupWindow);
    }

    private void commitLabelChanges(List<VdsNetworkInterface> srcIfaces,
            VdsNetworkInterface dstIface) {
        NetworkOperation.moveLabels(srcIfaces, dstIface);
    }

    private void commitNetworkChanges(VdsNetworkInterface iface, List<LogicalNetworkModel> networks) {
        NetworkInterfaceModel bondModel = nicMap.get(iface.getName());
        NetworkOperation.attachNetworks(bondModel,
            new ArrayList<LogicalNetworkModel>(networks),
            allNics);
    }

    public void redraw() {
        initAllModels(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        initAllModels(true);
    }

    private void onNicsChanged() {
        operationFactory = new NetworkOperationFactory(getNetworks(), getNics());
        validate();
    }

    private LogicalNetworkModel createUnmanagedNetworkModel(String networkName, VdsNetworkInterface nic) {
        Network unmanagedNetwork = new Network();
        unmanagedNetwork.setName(networkName);
        unmanagedNetwork.setVlanId(nic.getVlanId());
        unmanagedNetwork.setMtu(nic.getMtu());
        unmanagedNetwork.setVmNetwork(nic.isBridged());
        LogicalNetworkModel networkModel = new LogicalNetworkModel(unmanagedNetwork, this);
        networkMap.put(networkName, networkModel);
        return networkModel;
    }

    private boolean equals(NetworkItemModel<?> item1, NetworkItemModel<?> item2) {
        return Objects.equals(item1, item2);
    }

    private List<String> getFreeBonds() {
        List<String> freeBonds = new ArrayList<>();
        for (VdsNetworkInterface bond : allBonds) {
            if (!nicMap.containsKey(bond.getName())) {
                freeBonds.add(bond.getName());
            }
        }
        return freeBonds;
    }

    private void initAllModels(boolean fetchFromBackend) {
        if (fetchFromBackend) {
            // run query for networks, this chains the query for nics, and also stops progress when done
            startProgress(null);
            queryLabels();
        } else {
            initLabelModels();
            initNetworkModels();
            initNicModels();
        }
    }

    private void initLabelModels() {
        networkLabelMap.clear();
        for (String label : dcLabels) {
            networkLabelMap.put(label, new NetworkLabelModel(label, this));
        }
    }

    private void initNetworkModels() {
        Map<String, LogicalNetworkModel> networkModels = new HashMap<>();
        for (Network network : allNetworks) {
            LogicalNetworkModel networkModel = new LogicalNetworkModel(network, this);
            networkModels.put(network.getName(), networkModel);

            if (networkModel.isManagement()) {
                managementNetworkModel = networkModel;
            }

            if (!network.isExternal()) {
                String label = network.getLabel();
                if (label != null) {
                    // The network model is candidate to be drawn as part of the label.
                    // This doesn't yet consider whether it actually exists on the interface.
                    networkLabelMap.get(label).getNetworks().add(networkModel);
                }
            }
        }
        setNetworks(networkModels);
    }

    private void initNicModels() {
        Map<String, NetworkInterfaceModel> nicModels = new HashMap<>();
        Map<String, VdsNetworkInterface> nicMap = new HashMap<>();
        List<VdsNetworkInterface> physicalNics = new ArrayList<>();
        Map<String, List<VdsNetworkInterface>> bondToNic = new HashMap<>();
        Map<String, Set<LogicalNetworkModel>> nicToNetwork = new HashMap<>();
        List<LogicalNetworkModel> errorLabelNetworks = new ArrayList<>();
        labelToIface.clear();

        // map all nics
        for (VdsNetworkInterface nic : allNics) {
            nicMap.put(nic.getName(), nic);
        }

        // pass over all nics
        for (VdsNetworkInterface nic : allNics) {
            final String nicName = nic.getName();
            final String networkName = nic.getNetworkName();
            final String bondName = nic.getBondName();
            final boolean isVlan = nic.getVlanId() != null;

            if (!isVlan) { // physical interface (rather than virtual VLAN interface)
                physicalNics.add(nic);
            }

            // is the nic bonded?
            if (bondName != null) {
                if (bondToNic.containsKey(bondName)) {
                    bondToNic.get(bondName).add(nicMap.get(nicName));
                } else {
                    List<VdsNetworkInterface> bondedNics = new ArrayList<>();
                    bondedNics.add(nicMap.get(nicName));
                    bondToNic.put(bondName, bondedNics);
                }
            }

            // bridge name is either <nic>, <nic.vlanid> or <bond.vlanid>
            String ifName;
            if (isVlan) {
                ifName = nic.getBaseInterface();
            } else {
                ifName = nicName;
            }

            // initialize this nic's network list if it hadn't been initialized
            if (!nicToNetwork.containsKey(ifName)) {
                nicToNetwork.put(ifName, new HashSet<LogicalNetworkModel>());
            }

            // does this nic have a network?
            if (networkName != null) {
                LogicalNetworkModel networkModel = networkMap.get(networkName);

                if (networkModel == null) {
                    networkModel = createUnmanagedNetworkModel(networkName, nic);
                } else {
                    // The real vlanId, isBridged and mtu configured on the host can be not synced with the values
                    // configured in the networks table (dc networks).
                    // The real values configured on the host should be displayed.
                    networkModel.getNetwork().setVlanId(nic.getVlanId());
                    networkModel.getNetwork().setMtu(nic.getMtu());
                    networkModel.getNetwork().setVmNetwork(nic.isBridged());
                }

                Collection<LogicalNetworkModel> nicNetworks = new ArrayList<>();
                nicNetworks.add(networkModel);
                // set vlan device on the network
                if (networkModel.hasVlan()) {
                    NetworkInterfaceModel existingEridge = networkModel.getVlanNicModel();
                    assert existingEridge == null : "should have only one bridge, but found " + existingEridge; //$NON-NLS-1$
                    networkModel.setVlanNicModel(new NetworkInterfaceModel(nic, nicNetworks, null, false, this));
                }
                nicToNetwork.get(ifName).add(networkModel);

                if (!networkModel.isInSync() && networkModel.isManaged()) {
                    netToBeforeSyncParams.put(networkName, new NetworkParameters(nic));
                }
            }
        }

        // calculate the next available bond name
        List<String> bondNames = new ArrayList<>(bondToNic.keySet());
        Collections.sort(bondNames, new LexoNumericComparator());
        nextBondName = BusinessEntitiesDefinitions.BOND_NAME_PREFIX + 0;
        for (int i=0; i<bondNames.size(); ++i) {
            if (nextBondName.equals(bondNames.get(i))) {
                nextBondName = BusinessEntitiesDefinitions.BOND_NAME_PREFIX + (i + 1);
            } else {
                break;
            }
        }

        // build models
        for (VdsNetworkInterface nic : physicalNics) {
            // dont show bonded nics
            if (nic.getBondName() != null) {
                continue;
            }

            String nicName = nic.getName();
            Collection<LogicalNetworkModel> nicNetworks = nicToNetwork.get(nicName);
            List<NetworkLabelModel> nicLabels = new ArrayList<>();

            // does this nic have any labels?
            Set<String> labels = nic.getLabels();
            if (labels != null) {
                for (String label : labels) {
                    labelToIface.put(label, nicName);
                    NetworkLabelModel labelModel = networkLabelMap.get(label);
                    if (labelModel != null) {
                        // attach label networks to nic
                        for (Iterator<LogicalNetworkModel> iter = labelModel.getNetworks().iterator(); iter.hasNext();) {
                            LogicalNetworkModel networkModel = iter.next();

                            if (nicNetworks.contains(networkModel)) {
                                networkModel.attachViaLabel();
                            } else {
                                // The network has the same label as the nic but not attached to the nic.
                                iter.remove();
                                errorLabelNetworks.add(networkModel);
                            }
                        }

                        // attach label itself to nic
                        nicLabels.add(labelModel);
                    }
                }
            }

            List<VdsNetworkInterface> bondedNics = bondToNic.get(nicName);
            NetworkInterfaceModel nicModel;

            if (bondedNics != null) {
                List<NetworkInterfaceModel> bondedModels = new ArrayList<>();
                for (VdsNetworkInterface bonded : bondedNics) {
                    NetworkInterfaceModel bondedModel = new NetworkInterfaceModel(bonded, nicToVfsConfig.containsKey(bonded.getId()), this);
                    bondedModel.setBonded(true);
                    bondedModels.add(bondedModel);
                }
                nicModel = new BondNetworkInterfaceModel(nic, nicNetworks, nicLabels, bondedModels, this);
            } else {
                nicModel = new NetworkInterfaceModel(nic, nicNetworks, nicLabels, nicToVfsConfig.containsKey(nic.getId()), this);
            }

            nicModels.put(nicName, nicModel);
        }
        initLabeledNetworksErrorMessages(errorLabelNetworks, nicModels);
        setNics(nicModels);
    }

    private void initLabeledNetworksErrorMessages(List<LogicalNetworkModel> errorLabelNetworks, Map<String, NetworkInterfaceModel> nicModels){
        for (LogicalNetworkModel networkModel : errorLabelNetworks){
            NetworkInterfaceModel desiredNic = nicModels.get(labelToIface.get(networkModel.getNetwork().getLabel()));
            NetworkOperation operation = NetworkOperationFactory.operationFor(networkModel, desiredNic);
            UIMessages messages = ConstantsManager.getInstance().getMessages();
            // Should be attached but can't due to conflict
            if (operation.isNullOperation()) {
                networkModel.setErrorMessage(messages.networkLabelConflict(desiredNic.getName(),
                        networkModel.getNetwork().getLabel())
                        + " " + operation.getMessage(networkModel, desiredNic)); //$NON-NLS-1$
            } else {
                networkModel.setErrorMessage(messages.labeledNetworkNotAttached(desiredNic.getName(),
                        networkModel.getNetwork().getLabel()));
            }
        }
    }

    private void queryLabels() {
        AsyncDataProvider.getInstance().getNetworkLabelsByDataCenterId(getEntity().getStoragePoolId(),
            new AsyncQuery(new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {
                    dcLabels = (SortedSet<String>) returnValue;
                    initLabelModels();

                    // chain the networks query
                    queryNetworks();
                }
            }));
    }

    private void queryFreeBonds() {
        // query for all unused, existing bonds on the host
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<VdsNetworkInterface> bonds =
                        ((VdcQueryReturnValue) returnValue).getReturnValue();
                allBonds = bonds;

                initNicModels();
                stopProgress();
            }
        };

        VDS vds = getEntity();
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsFreeBondsByVdsId,
            new IdQueryParameters(vds.getId()),
            asyncQuery);
    }

    private void queryInterfaces() {
        // query for interfaces
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValueObj) {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnValueObj;
                Object returnValue2 = returnValue.getReturnValue();
                List<VdsNetworkInterface> allNics = (List<VdsNetworkInterface>) returnValue2;
                HostSetupNetworksModel.this.allNics = allNics;

                // chain the vfsConfig query
                queryVfsConfig();
            }
        };

        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsInterfacesByVdsId, params, asyncQuery);
    }

    private void queryVfsConfig() {
        // query for vfsConfigs
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValueObj) {
                Object returnValue = ((VdcQueryReturnValue) returnValueObj).getReturnValue();
                List<HostNicVfsConfig> allHostVfs = (List<HostNicVfsConfig>) returnValue;

                for (HostNicVfsConfig vfsConfig : allHostVfs) {
                    originalVfsConfigs.add(vfsConfig);
                    nicToVfsConfig.put(vfsConfig.getNicId(), new HostNicVfsConfig(vfsConfig));
                }

                // chain the free bonds query
                queryFreeBonds();
            }
        };

        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        Frontend.getInstance().runQuery(VdcQueryType.GetAllVfsConfigByHostId, params, asyncQuery);
    }

    private void queryNetworks() {
        // query for networks
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                List<Network> networks = (List<Network>) returnValue;
                allNetworks = networks;
                initNetworkModels();
                initDcNetworkParams();

                // chain the nic query
                queryInterfaces();
            }
        };

        VDS vds = getEntity();
        AsyncDataProvider.getInstance().getClusterNetworkList(asyncQuery, vds.getVdsGroupId());
    }

    private void initDcNetworkParams() {
        for (Network network : allNetworks) {
            netTodcParams.put(network.getName(), new DcNetworkParams(network));
        }
    }

    private void setBondOptions(VdsNetworkInterface entity, SetupNetworksBondModel bondDialogModel) {
        Map.Entry<String, EntityModel<String>> BondPair = bondDialogModel.getBondingOptions().getSelectedItem();
        String key = BondPair.getKey();
        entity.setBondOptions("custom".equals(key) ? BondPair.getValue().getEntity() : key); //$NON-NLS-1$
    }

    private void setNetworks(Map<String, LogicalNetworkModel> networks) {
        networkMap = networks;
    }

    private void setNics(Map<String, NetworkInterfaceModel> nics) {
        nicMap = nics;
        onNicsChanged();
        getNicsChangedEvent().raise(this, EventArgs.EMPTY);
    }

    private void setNicsChangedEvent(Event<EventArgs> value) {
        nicsChangedEvent = value;
    }

    private void setOperationCandidateEvent(Event<OperationCandidateEventArgs> event) {
        operationCandidateEvent = event;
    }

    private void validate() {
        // check if management network is attached
        if (!managementNetworkModel.isAttached()) {
            okCommand.getExecuteProhibitionReasons().add(ConstantsManager.getInstance()
                    .getConstants()
                    .mgmtNotAttachedToolTip());
            okCommand.setIsExecutionAllowed(false);
        } else {
            okCommand.setIsExecutionAllowed(true);
        }
    }

    public Map<String, NetworkParameters> getNetworkToLastDetachParams() {
        return networkToLastDetachParams;
    }

    public List<String> getNetworksToSync() {
        return networksToSync;
    }

    public DcNetworkParams getNetDcParams(String networkName) {
        return netTodcParams.get(networkName);
    }

    public void onSetupNetworks() {
        // Determines the connectivity timeout in seconds
        AsyncDataProvider.getInstance().getNetworkConnectivityCheckTimeoutInSeconds(new AsyncQuery(sourceListModel,
                                                                                                   new INewAsyncCallback() {
                                                                                                       @Override
                                                                                                       public void onSuccess(Object target, Object returnValue) {
                                                                                                           getConnectivityTimeout().setEntity((Integer) returnValue);
                                                                                                           postOnSetupNetworks();
                                                                                                       }
                                                                                                   }));
    }

    public void postOnSetupNetworks() {
        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(getAllNics());
        params.setCheckConnectivity(getCheckConnectivity().getEntity());
        params.setConectivityTimeout(getConnectivityTimeout().getEntity());
        params.setVdsId(getEntity().getId());
        params.setNetworksToSync(getNetworksToSync());

        SimpleAction closeAction = getCloseAction();
        UiAction setupNetworks = new UiVdcAction(VdcActionType.SetupNetworks, params, this, true);

        setupNetworks.then(getVfsConfigAction()).then(getCommitNetworkChangesAction()).onAllExecutionsFinish(closeAction);

        setupNetworks.runAction();
    }

    public UiAction getCommitNetworkChangesAction() {
        return new UiVdcAction(VdcActionType.CommitNetworkChanges,
                new VdsActionParameters(getEntity().getId()),
                HostSetupNetworksModel.this, true) {
            @Override
            protected boolean shouldExecute() {
                EntityModel<Boolean> commitChanges = HostSetupNetworksModel.this.getCommitChanges();
                return commitChanges.getEntity();
            }
        };
    }

    public UiAction getVfsConfigAction() {
        return new VfsConfigAction(this, originalVfsConfigs, nicToVfsConfig);
    }

    public SimpleAction getCloseAction() {
        return new SimpleAction() {
            @Override
            public void execute() {
                sourceListModel.setWindow(null);
                sourceListModel.search();
            }
        };
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSetupNetworks".equals(command.getName())) { //$NON-NLS-1$
            onSetupNetworks();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }

    }

    private void cancel() {
        sourceListModel.setWindow(null);

    }
}
