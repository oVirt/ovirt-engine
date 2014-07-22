package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkCommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperationFactory;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperationFactory.OperationMap;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.OperationCadidateEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIMessages;

/**
 * A Model for the Setup Networks Dialog<BR>
 * The Entity is the VDS being edited.<BR>
 * The Dialog holds two different Lists: NIC Models, and Network Models.<BR>
 * These two Lists are fetched from the backend, and cannot be changed by the User.<BR>
 * The user only changes the topology of their connections.
 */
public class HostSetupNetworksModel extends EntityModel {

    @Override
    public VDS getEntity() {
        return (VDS) super.getEntity();
    }

    private EntityModel<Boolean> checkConnectivity;

    public EntityModel<Boolean> getCheckConnectivity()
    {
        return checkConnectivity;
    }

    private void setCheckConnectivity(EntityModel<Boolean> value)
    {
        checkConnectivity = value;
    }

    private EntityModel<Integer> connectivityTimeout;

    public EntityModel<Integer> getConnectivityTimeout()
    {
        return connectivityTimeout;
    }

    private void setConnectivityTimeout(EntityModel<Integer> value)
    {
        connectivityTimeout = value;
    }

    private EntityModel<Boolean> commitChanges;

    public EntityModel<Boolean> getCommitChanges()
    {
        return commitChanges;
    }

    public void setCommitChanges(EntityModel<Boolean> value)
    {
        commitChanges = value;
    }

    private static final EventDefinition NICS_CHANGED_EVENT_DEFINITION = new EventDefinition("NicsChanged", //$NON-NLS-1$
            HostSetupNetworksModel.class);
    private static final EventDefinition NETWORKS_CHANGED_EVENT_DEFINITION = new EventDefinition("NetworksChanged", //$NON-NLS-1$
            HostSetupNetworksModel.class);

    private static final EventDefinition OPERATION_CANDIDATE_EVENT_DEFINITION =
            new EventDefinition("OperationCandidate", NetworkOperationFactory.class); //$NON-NLS-1$

    private Event operationCandidateEvent;

    private Event nicsChangedEvent;

    private Event networksChangedEvent;

    private List<VdsNetworkInterface> allNics;

    private Map<String, NetworkInterfaceModel> nicMap;

    private Map<String, LogicalNetworkModel> networkMap;

    private Map<String, NetworkLabelModel> networkLabelMap;

    private Map<String, String> labelToIface;

    private final List<String> networksToSync = new ArrayList<String>();

    // The purpose of this map is to keep the network parameters while moving the network from one nic to another
    private final Map<String, NetworkParameters> networkToLastDetachParams;

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

        networkToLastDetachParams = new HashMap<String, NetworkParameters>();
        netTodcParams = new HashMap<String, DcNetworkParams>();
        netToBeforeSyncParams = new HashMap<String, NetworkParameters>();
        setNicsChangedEvent(new Event(NICS_CHANGED_EVENT_DEFINITION));
        setNetworksChangedEvent(new Event(NETWORKS_CHANGED_EVENT_DEFINITION));
        setOperationCandidateEvent(new Event(OPERATION_CANDIDATE_EVENT_DEFINITION));
        setCheckConnectivity(new EntityModel<Boolean>());
        getCheckConnectivity().setEntity(true);
        setConnectivityTimeout(new EntityModel<Integer>());
        setCommitChanges(new EntityModel<Boolean>());
        getCommitChanges().setEntity(true);

        // ok command
        okCommand = new UICommand("OnSetupNetworks", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getCommands().add(okCommand);

        // cancel command
        UICommand cancelCommand;
        cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);
    }

    public boolean candidateOperation(String op1Key, String op1Type, String op2Key, String op2Type, boolean drop) {

        NetworkInterfaceModel nic1 = null;
        LogicalNetworkModel network1 = null;
        NetworkInterfaceModel nic2 = null;
        LogicalNetworkModel network2 = null;

        if (NIC.equals(op1Type)) {
            nic1 = nicMap.get(op1Key);
        } else if (NETWORK.equals(op1Type)) {
            network1 = networkMap.get(op1Key);
        }

        if (NIC.equals(op2Type)) {
            nic2 = nicMap.get(op2Key);
        } else if (NETWORK.equals(op2Type)) {
            network2 = networkMap.get(op2Key);
        }

        NetworkItemModel<?> op1 = nic1 == null ? network1 : nic1;
        NetworkItemModel<?> op2 = nic2 == null ? network2 : nic2;

        return candidateOperation(op1, op2, drop);
    }

    private boolean candidateOperation(NetworkItemModel<?> op1, NetworkItemModel<?> op2, boolean drop) {
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
            getOperationCandidateEvent().raise(this, new OperationCadidateEventArgs(candidate, op1, op2));
        }
        return !candidate.isNullOperation();
    }

    public OperationMap commandsFor(NetworkItemModel<?> item) {
        return operationFactory.commandsFor(item, allNics);
    }

    public List<VdsNetworkInterface> getAllNics() {
        return allNics;
    }

    public List<LogicalNetworkModel> getNetworks() {
        return new ArrayList<LogicalNetworkModel>(networkMap.values());
    }

    public Event getNetworksChangedEvent() {
        return networksChangedEvent;
    }

    public List<NetworkInterfaceModel> getNics() {
        return new ArrayList<NetworkInterfaceModel>(nicMap.values());
    }

    public Event getNicsChangedEvent() {
        return nicsChangedEvent;
    }

    public Event getOperationCandidateEvent() {
        return operationCandidateEvent;
    }

    private Set<LogicalNetworkModel> computeLabelChanges(NicLabelModel labelsModel,
            Collection<LogicalNetworkModel> originalNetworks) {

        Collection<String> removedLabels = labelsModel.getRemovedLabels();
        Collection<String> addedLabels = labelsModel.getAddedLabels();
        Set<LogicalNetworkModel> removedNetworks = new HashSet<LogicalNetworkModel>();
        Set<LogicalNetworkModel> addedNetworks = new HashSet<LogicalNetworkModel>();
        for (String label : removedLabels) {
             NetworkLabelModel networkLabelModel = networkLabelMap.get(label);
             Collection<LogicalNetworkModel> labelNetworks = networkLabelModel != null ? networkLabelModel.getNetworks() : null;
            if (labelNetworks != null) {
                removedNetworks.addAll(labelNetworks);
            }
        }
        for (String label : addedLabels) {
            NetworkLabelModel labelModel = networkLabelMap.get(label);
            if (labelModel != null) {
                addedNetworks.addAll(labelModel.getNetworks());
            }
        }

        Set<LogicalNetworkModel> potentialNetworks = new HashSet<LogicalNetworkModel>(originalNetworks);
        potentialNetworks.removeAll(removedNetworks);
        potentialNetworks.addAll(addedNetworks);

        return potentialNetworks;
    }

    // generate a mock "bonding" operation to check if the networks can be configured together
    private boolean validateLabelChanges(Collection<LogicalNetworkModel> potentialNetworks) {
        NetworkInterfaceModel mockSrc = new NetworkInterfaceModel(this);
        NetworkInterfaceModel mockDst = new NetworkInterfaceModel(this);
        mockSrc.setEntity(new VdsNetworkInterface());
        mockDst.setEntity(new VdsNetworkInterface());
        mockDst.setItems(new ArrayList<LogicalNetworkModel>(potentialNetworks));

        boolean res = candidateOperation(mockSrc, mockDst, false);
        if (!res) {
            candidateOperation(mockSrc, mockDst, true); // trick to get a red-highlighted error status
        }
        return res;
    }

    private void commitLabelChanges(NicLabelModel labelModel,
            VdsNetworkInterface iface,
            Collection<LogicalNetworkModel> potentialNetworks) {

        labelModel.commit(iface);
        NetworkInterfaceModel ifaceModel = nicMap.get(iface.getName());
        NetworkOperation.clearNetworks(ifaceModel, allNics);
        NetworkOperation.attachNetworks(ifaceModel, new ArrayList<LogicalNetworkModel>(potentialNetworks), allNics);
    }

    public void onEdit(NetworkItemModel<?> item) {
        Model editPopup = null;
        BaseCommandTarget okTarget = null;
        if (item instanceof BondNetworkInterfaceModel) {
            /*****************
             * Bond Dialog
             *****************/
            final VdsNetworkInterface entity = ((NetworkInterfaceModel) item).getEntity();
            editPopup = new SetupNetworksEditBondModel(entity, getFreeLabels(), labelToIface);
            final SetupNetworksBondModel bondDialogModel = (SetupNetworksBondModel) editPopup;

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void executeCommand(UICommand command) {
                    if (!bondDialogModel.validate()) {
                        return;
                    }
                    sourceListModel.setConfirmWindow(null);
                    Collection<LogicalNetworkModel> potentialNetworks =
                            computeLabelChanges(bondDialogModel.getLabelsModel(), nicMap.get(entity.getName())
                                    .getItems());
                    if (validateLabelChanges(potentialNetworks)) {
                        setBondOptions(entity, bondDialogModel);
                        commitLabelChanges(bondDialogModel.getLabelsModel(), entity, potentialNetworks);
                        redraw();
                    }
                }
            };
        } else if (item instanceof NetworkInterfaceModel) {
            /*******************
             * Interface Dialog
             *******************/
            final VdsNetworkInterface entity = ((NetworkInterfaceModel) item).getEntity();
            final HostNicModel interfacePopupModel = new HostNicModel(entity, getFreeLabels(), labelToIface);
            editPopup = interfacePopupModel;

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void executeCommand(UICommand uiCommand) {
                    if (!interfacePopupModel.validate()) {
                        return;
                    }
                    sourceListModel.setConfirmWindow(null);
                    Collection<LogicalNetworkModel> potentialNetworks =
                            computeLabelChanges(interfacePopupModel.getLabelsModel(), nicMap.get(entity.getName())
                                    .getItems());
                    if (validateLabelChanges(potentialNetworks)) {
                        commitLabelChanges(interfacePopupModel.getLabelsModel(), entity, potentialNetworks);
                        redraw();
                    }
                }
            };
        } else if (item instanceof LogicalNetworkModel) {
            /*****************
             * Network Dialog
             *****************/
            final LogicalNetworkModel logicalNetwork = (LogicalNetworkModel) item;
            final VdsNetworkInterface entity =
                    logicalNetwork.hasVlan() ? logicalNetwork.getVlanNicModel().getEntity()
                            : logicalNetwork.getAttachedToNic().getEntity();

            final HostInterfaceModel networkDialogModel;
            String version = getEntity().getVdsGroupCompatibilityVersion().getValue();
            if (logicalNetwork.isManagement()) {
                networkDialogModel = new HostManagementNetworkModel(true);
                networkDialogModel.setTitle(ConstantsManager.getInstance().getConstants().editManagementNetworkTitle());
                networkDialogModel.setEntity(logicalNetwork.getEntity());
                networkDialogModel.setNoneBootProtocolAvailable(false);
                networkDialogModel.getInterface().setIsAvailable(false);
            } else {
                networkDialogModel = new HostInterfaceModel(true);
                networkDialogModel.setTitle(ConstantsManager.getInstance()
                        .getMessages()
                        .editNetworkTitle(logicalNetwork.getName()));
                networkDialogModel.getName().setIsAvailable(false);
                networkDialogModel.getNetwork().setIsChangable(false);
                networkDialogModel.getGateway()
                        .setIsAvailable((Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.MultipleGatewaysSupported,
                                version));
            }

            networkDialogModel.getNetwork().setSelectedItem(logicalNetwork.getEntity());
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
                networkDialogModel.getQosModel().init(entity.getQos());
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
                if (!logicalNetwork.getEntity().isVmNetwork()) {
                    validProperties.remove("bridge_opts"); //$NON-NLS-1$
                }
                validProperties.putAll(KeyValueModel.convertProperties((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.UserDefinedNetworkCustomProperties,
                        version)));
                customPropertiesModel.setKeyValueMap(validProperties);
                customPropertiesModel.deserialize(KeyValueModel.convertProperties(entity.getCustomProperties()));
            }
            networkDialogModel.getIsToSync().setIsChangable(!logicalNetwork.isInSync());
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
                        entity.setQosOverridden(networkDialogModel.getQosOverridden().getEntity());
                        entity.setQos(networkDialogModel.getQosModel().flush());
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
        UICommand okCommand = new UICommand("OK", okTarget); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);

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
            VdsNetworkInterface iface1 = ((NetworkInterfaceModel) networkCommand.getOp1()).getEntity();
            VdsNetworkInterface iface2 = ((NetworkInterfaceModel) networkCommand.getOp2()).getEntity();
            if (operation == NetworkOperation.BOND_WITH) {
                bondPopup =
                        new SetupNetworksAddBondModel(getFreeBonds(),
                                nextBondName,
                                Arrays.asList(iface1, iface2),
                                getFreeLabels(),
                                labelToIface);
            } else {
                bondPopup =
                        new SetupNetworksJoinBondsModel(getFreeBonds(),
                                (BondNetworkInterfaceModel) networkCommand.getOp1(),
                                (BondNetworkInterfaceModel) networkCommand.getOp2(),
                                getFreeLabels(),
                                labelToIface);
            }
            bondPopup.getCommands().add(new UICommand("OK", new BaseCommandTarget() { //$NON-NLS-1$

                        @Override
                        public void executeCommand(UICommand command) {
                            if (!bondPopup.validate()) {
                                return;
                            }
                            sourceListModel.setConfirmWindow(null);

                            NetworkInterfaceModel nic1 = (NetworkInterfaceModel) networkCommand.getOp1();
                            NetworkInterfaceModel nic2 = (NetworkInterfaceModel) networkCommand.getOp2();
                            List<LogicalNetworkModel> networks = new ArrayList<LogicalNetworkModel>();
                            networks.addAll(nic1.getItems());
                            networks.addAll(nic2.getItems());
                            Collection<LogicalNetworkModel> potentialNetworks =
                                    computeLabelChanges(bondPopup.getLabelsModel(), networks);
                            if (!validateLabelChanges(potentialNetworks)) {
                                return;
                            }
                            VdsNetworkInterface bond = new Bond(bondPopup.getBond().getSelectedItem());
                            setBondOptions(bond, bondPopup);

                            networkCommand.execute(bond);
                            redraw();

                            // Attach the previous networks
                            commitLabelChanges(bondPopup.getLabelsModel(), bond, potentialNetworks);
                            redraw();
                        }
                    }));

            popupWindow = bondPopup;
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

    public void redraw() {
        initAllModels(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        initAllModels(true);
    }

    protected void onNicsChanged() {
        operationFactory = new NetworkOperationFactory(getNetworks(), getNics());
        validate();
        getNetworksChangedEvent().raise(this, EventArgs.EMPTY);
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
        if (item1 == null && item2 == null) {
            return true;
        }
        return (item1 != null) ? item1.equals(item2) : item2.equals(item1);

    }

    private Collection<String> getFreeLabels() {
        SortedSet<String> freeLabels = new TreeSet<String>(dcLabels);
        freeLabels.removeAll(labelToIface.keySet());
        return freeLabels;
    }

    private List<String> getFreeBonds() {
        List<String> freeBonds = new ArrayList<String>();
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
            queryNetworks();
        } else {
            initNetworkModels();
            initNicModels();
        }
    }

    private void initNetworkModels() {
        Map<String, LogicalNetworkModel> networkModels = new HashMap<String, LogicalNetworkModel>();
        networkLabelMap = new HashMap<String, NetworkLabelModel>();
        for (Network network : allNetworks) {
            LogicalNetworkModel networkModel = new LogicalNetworkModel(network, this);
            networkModels.put(network.getName(), networkModel);

            if (!network.isExternal()) {
                NetworkLabelModel labelModel = networkLabelMap.get(network.getLabel());
                if (labelModel == null) {
                    labelModel = new NetworkLabelModel(network.getLabel(), this);
                    networkLabelMap.put(network.getLabel(), labelModel);
                }
                // The network model is candidate to be drawn as part of the label.
                // This doesn't yet consider whether it actually exists on the interface.
                labelModel.getNetworks().add(networkModel);
            }
        }
        setNetworks(networkModels);
    }

    private void initNicModels() {
        Map<String, NetworkInterfaceModel> nicModels = new HashMap<String, NetworkInterfaceModel>();
        Map<String, VdsNetworkInterface> nicMap = new HashMap<String, VdsNetworkInterface>();
        List<VdsNetworkInterface> physicalNics = new ArrayList<VdsNetworkInterface>();
        Map<String, List<VdsNetworkInterface>> bondToNic = new HashMap<String, List<VdsNetworkInterface>>();
        Map<String, Set<LogicalNetworkModel>> nicToNetwork = new HashMap<String, Set<LogicalNetworkModel>>();
        List<LogicalNetworkModel> errorLabelNetworks = new ArrayList<LogicalNetworkModel>();
        labelToIface = new HashMap<String, String>();

        // map all nics
        for (VdsNetworkInterface nic : allNics) {
            nicMap.put(nic.getName(), nic);
        }

        // pass over all nics
        for (VdsNetworkInterface nic : allNics) {
            // is this a management nic? (comes from backend)
            final boolean isNicManagement = nic.getIsManagement();
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
                    List<VdsNetworkInterface> bondedNics = new ArrayList<VdsNetworkInterface>();
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
                    networkModel.getEntity().setVlanId(nic.getVlanId());
                    networkModel.getEntity().setMtu(nic.getMtu());
                    networkModel.getEntity().setVmNetwork(nic.isBridged());
                }

                // is this a management network (from backend)?
                if (isNicManagement) {
                    networkModel.setManagement(true);
                }

                Collection<LogicalNetworkModel> nicNetworks = new ArrayList<LogicalNetworkModel>();
                nicNetworks.add(networkModel);
                // set vlan device on the network
                if (networkModel.hasVlan()) {
                    NetworkInterfaceModel existingEridge = networkModel.getVlanNicModel();
                    assert existingEridge == null : "should have only one bridge, but found " + existingEridge; //$NON-NLS-1$
                    networkModel.setVlanNicModel(new NetworkInterfaceModel(nic, nicNetworks, null, this));
                }
                nicToNetwork.get(ifName).add(networkModel);

                if (!networkModel.isInSync() && networkModel.isManaged()) {
                    netToBeforeSyncParams.put(networkName, new NetworkParameters(nic));
                }
            }
        }

        // calculate the next available bond name
        List<String> bondNames = new ArrayList<String>(bondToNic.keySet());
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
            List<NetworkLabelModel> nicLabels = new ArrayList<NetworkLabelModel>();

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
                        if (!labelModel.getNetworks().isEmpty()) {
                            nicLabels.add(labelModel);
                        }
                    }
                }
            }

            List<VdsNetworkInterface> bondedNics = bondToNic.get(nicName);
            NetworkInterfaceModel nicModel;

            if (bondedNics != null) {
                List<NetworkInterfaceModel> bondedModels = new ArrayList<NetworkInterfaceModel>();
                for (VdsNetworkInterface bonded : bondedNics) {
                    NetworkInterfaceModel bondedModel = new NetworkInterfaceModel(bonded, this);
                    bondedModel.setBonded(true);
                    bondedModels.add(bondedModel);
                }
                nicModel = new BondNetworkInterfaceModel(nic, nicNetworks, nicLabels, bondedModels, this);
            } else {
                nicModel = new NetworkInterfaceModel(nic, nicNetworks, nicLabels, this);
            }

            nicModels.put(nicName, nicModel);
        }
        initLabeledNetworksErrorMessages(errorLabelNetworks, nicModels);
        setNics(nicModels);
    }

    private void initLabeledNetworksErrorMessages(List<LogicalNetworkModel> errorLabelNetworks, Map<String, NetworkInterfaceModel> nicModels){
        for (LogicalNetworkModel networkModel : errorLabelNetworks){
            NetworkInterfaceModel desiredNic = nicModels.get(labelToIface.get(networkModel.getEntity().getLabel()));
            NetworkOperation operation = NetworkOperationFactory.operationFor(networkModel, desiredNic);
            UIMessages messages = ConstantsManager.getInstance().getMessages();
            // Should be attached but can't due to conflict
            if (operation.isNullOperation()) {
                networkModel.setErrorMessage(messages.networkLabelConflict(desiredNic.getName(),
                        networkModel.getEntity().getLabel())
                        + " " + operation.getMessage(networkModel, desiredNic)); //$NON-NLS-1$
            } else {
                networkModel.setErrorMessage(messages.labeledNetworkNotAttached(desiredNic.getName(),
                        networkModel.getEntity().getLabel()));
            }
        }
    }

    private void queryLabels() {
        AsyncDataProvider.getInstance().getNetworkLabelsByDataCenterId(getEntity().getStoragePoolId(), new AsyncQuery(new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                dcLabels = (SortedSet<String>) returnValue;

                initNicModels();
                stopProgress();
            }
        }));
    }

    private void queryFreeBonds() {
        // query for all unused, existing bonds on the host
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue)
            {
                List<VdsNetworkInterface> bonds =
                        ((VdcQueryReturnValue) returnValue).getReturnValue();
                allBonds = bonds;

                // chain the DC labels query
                queryLabels();
            }
        };

        VDS vds = getEntity();
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsFreeBondsByVdsId, new IdQueryParameters(vds.getId()), asyncQuery);
    }

    private void queryInterfaces() {
        // query for interfaces
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValueObj)
            {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnValueObj;
                Object returnValue2 = returnValue.getReturnValue();
                List<VdsNetworkInterface> allNics = (List<VdsNetworkInterface>) returnValue2;
                HostSetupNetworksModel.this.allNics = allNics;

                // chain the free bonds query
                queryFreeBonds();
            }
        };

        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsInterfacesByVdsId, params, asyncQuery);
    }

    private void queryNetworks() {
        // query for networks
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue)
            {
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
        getNetworksChangedEvent().raise(this, EventArgs.EMPTY);
    }

    private void setNetworksChangedEvent(Event value) {
        networksChangedEvent = value;
    }

    private void setNics(Map<String, NetworkInterfaceModel> nics) {
        nicMap = nics;
        onNicsChanged();
        getNicsChangedEvent().raise(this, EventArgs.EMPTY);
    }

    private void setNicsChangedEvent(Event value) {
        nicsChangedEvent = value;
    }

    private void setOperationCandidateEvent(Event event) {
        operationCandidateEvent = event;
    }

    private void validate() {
        // check if management network is attached
        LogicalNetworkModel mgmtNetwork = networkMap.get(HostInterfaceListModel.ENGINE_NETWORK_NAME);
        if (!mgmtNetwork.isAttached()) {
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
        final HostSetupNetworksModel model = (HostSetupNetworksModel) sourceListModel.getWindow();

        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(model.getAllNics());
        params.setCheckConnectivity(model.getCheckConnectivity().getEntity());
        params.setConectivityTimeout(model.getConnectivityTimeout().getEntity());
        params.setVdsId(getEntity().getId());
        params.setNetworksToSync(model.getNetworksToSync());

        model.startProgress(null);
        Frontend.getInstance().runAction(VdcActionType.SetupNetworks, params, new IFrontendActionAsyncCallback() {

            @Override
            public void executed(FrontendActionAsyncResult result) {
                VdcReturnValueBase returnValueBase = result.getReturnValue();
                if (returnValueBase != null && returnValueBase.getSucceeded())
                {
                    EntityModel<Boolean> commitChanges = model.getCommitChanges();
                    if (commitChanges.getEntity())
                    {
                        new SaveNetworkConfigAction(sourceListModel, model, getEntity()).execute();
                    }
                    else
                    {
                        model.stopProgress();
                        sourceListModel.setWindow(null);
                        sourceListModel.search();
                    }
                }
                else
                {
                    model.stopProgress();
                }
            }
        });
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if ("OnSetupNetworks".equals(command.getName())) //$NON-NLS-1$
        {
            onSetupNetworks();
        } else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }

    }

    private void cancel() {
        sourceListModel.setWindow(null);

    }

}
