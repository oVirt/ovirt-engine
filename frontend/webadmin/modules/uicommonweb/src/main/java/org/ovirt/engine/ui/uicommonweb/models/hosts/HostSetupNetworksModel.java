package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.BaseCommandTarget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkCommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkItemModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperation;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperationFactory;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkOperationFactory.OperationMap;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.OperationCadidateEventArgs;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

/**
 * A Model for the Setup Networks Dialog<BR>
 * The Entity is the VDS being edited.<BR>
 * The Dialog holds two different Lists: NIC Models, and Network Models.<BR>
 * These two Lists are fetched from the backend, and cannot be changed by the User.<BR>
 * The user only changes the topology of their connections.
 */
public class HostSetupNetworksModel extends EntityModel {

    private EntityModel privateCheckConnectivity;

    public EntityModel getCheckConnectivity()
    {
        return privateCheckConnectivity;
    }

    private void setCheckConnectivity(EntityModel value)
    {
        privateCheckConnectivity = value;
    }

    private EntityModel connectivityTimeout;

    public EntityModel getConnectivityTimeout()
    {
        return connectivityTimeout;
    }

    private void setConnectivityTimeout(EntityModel value)
    {
        connectivityTimeout = value;
    }

    private EntityModel privateCommitChanges;

    public EntityModel getCommitChanges()
    {
        return privateCommitChanges;
    }

    public void setCommitChanges(EntityModel value)
    {
        privateCommitChanges = value;
    }

    private static final EventDefinition NICS_CHANGED_EVENT_DEFINITION = new EventDefinition("NicsChanged", //$NON-NLS-1$
            HostSetupNetworksModel.class);
    private static final EventDefinition NETWORKS_CHANGED_EVENT_DEFINITION = new EventDefinition("NetworksChanged", //$NON-NLS-1$
            HostSetupNetworksModel.class);

    private static final EventDefinition OPERATION_CANDIDATE_EVENT_DEFINITION =
            new EventDefinition("OperationCandidate", NetworkOperationFactory.class); //$NON-NLS-1$

    private Event privateOperationCandidateEvent;

    private Event privateNicsChangedEvent;

    private Event privateNetworksChangedEvent;

    private List<VdsNetworkInterface> allNics;

    private Map<String, NetworkInterfaceModel> nicMap;

    private Map<String, LogicalNetworkModel> networkMap;

    private final List<String> networksToSync = new ArrayList<String>();

    // The purpose of this map is to keep the network parameters while moving the network from one nic to another
    private final Map<String, NetworkParameters> networkToLastDetachParams;

    private NetworkOperationFactory operationFactory;
    private List<Network> allNetworks;
    private final Map<String, DcNetworkParams> netTodcParams;
    private final Map<String, NetworkParameters> netToBeforeSyncParams;
    private final SearchableListModel sourceListModel;
    private List<VdsNetworkInterface> allBonds;
    private NetworkOperation currentCandidate;
    private NetworkItemModel<?> currentOp1;
    private NetworkItemModel<?> currentOp2;

    private final UICommand okCommand;
    public static final String NIC = "nic"; //$NON-NLS-1$
    public static final String NETWORK = "network"; //$NON-NLS-1$

    public HostSetupNetworksModel(SearchableListModel listModel) {
        this.sourceListModel = listModel;

        setTitle(ConstantsManager.getInstance().getConstants().setupHostNetworksTitle());
        setHashName("host_setup_networks"); //$NON-NLS-1$

        networkToLastDetachParams = new HashMap<String, NetworkParameters>();
        netTodcParams = new HashMap<String, DcNetworkParams>();
        netToBeforeSyncParams = new HashMap<String, NetworkParameters>();
        setNicsChangedEvent(new Event(NICS_CHANGED_EVENT_DEFINITION));
        setNetworksChangedEvent(new Event(NETWORKS_CHANGED_EVENT_DEFINITION));
        setOperationCandidateEvent(new Event(OPERATION_CANDIDATE_EVENT_DEFINITION));
        setCheckConnectivity(new EntityModel());
        getCheckConnectivity().setEntity(true);
        setConnectivityTimeout(new EntityModel());
        setCommitChanges(new EntityModel());
        getCommitChanges().setEntity(false);

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

        if (op1Type != null && op1Type.equals(NIC)) {
            nic1 = nicMap.get(op1Key);
        } else if (op1Type != null && op1Type.equals(NETWORK)) {
            network1 = networkMap.get(op1Key);
        }

        if (op2Type != null && op2Type.equals(NIC)) {
            nic2 = nicMap.get(op2Key);
        } else if (op2Type != null && op2Type.equals(NETWORK)) {
            network2 = networkMap.get(op2Key);
        }

        NetworkItemModel<?> op1 = nic1 == null ? network1 : nic1;
        NetworkItemModel<?> op2 = nic2 == null ? network2 : nic2;

        if (op1 == null || (op1 == null && op2 == null)) {
            throw new IllegalArgumentException("null Operands"); //$NON-NLS-1$
        }

        NetworkOperation candidate = NetworkOperationFactory.operationFor(op1, op2, true);

        if (drop) {
            onOperation(candidate, candidate.getCommand(op1, op2, allNics));
        } else {
            // raise the candidate event only if it was changed
            if (!candidate.equals(currentCandidate) || !equals(op1, currentOp1) || !equals(op2, currentOp2)) {
                currentCandidate = candidate;
                currentOp1 = op1;
                currentOp2 = op2;
                getOperationCandidateEvent().raise(this, new OperationCadidateEventArgs(candidate, op1, op2));
            }
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
        return privateNetworksChangedEvent;
    }

    public List<NetworkInterfaceModel> getNics() {
        return new ArrayList<NetworkInterfaceModel>(nicMap.values());
    }

    public Event getNicsChangedEvent() {
        return privateNicsChangedEvent;
    }

    public Event getOperationCandidateEvent() {
        return privateOperationCandidateEvent;
    }

    public void onEdit(NetworkItemModel<?> item) {
        Model editPopup = null;
        BaseCommandTarget okTarget = null;
        if (item instanceof BondNetworkInterfaceModel) {
            /*****************
             * Bond Dialog
             *****************/
            final VdsNetworkInterface entity = ((NetworkInterfaceModel) item).getEntity();
            editPopup = new HostBondInterfaceModel(true);
            final HostBondInterfaceModel bondDialogModel = (HostBondInterfaceModel) editPopup;
            bondDialogModel.setTitle(ConstantsManager.getInstance()
                    .getMessages()
                    .editBondInterfaceTitle(entity.getName()));
            bondDialogModel.getNetwork().setIsAvailable(false);
            bondDialogModel.getCheckConnectivity().setIsAvailable(false);
            bondDialogModel.getAddress().setIsAvailable(false);
            bondDialogModel.getSubnet().setIsAvailable(false);
            bondDialogModel.getGateway().setIsAvailable(false);
            bondDialogModel.setBootProtocolAvailable(false);

            // bond name
            bondDialogModel.getBond().setIsChangable(false);
            List<VdsNetworkInterface> bondName = Arrays.asList(entity);
            bondDialogModel.getBond().setItems(bondName);
            bondDialogModel.getBond().setSelectedItem(entity);

            // bond options
            String bondOptions = entity.getBondOptions();
            List<KeyValuePairCompat<String, EntityModel>> items =
                    (List<KeyValuePairCompat<String, EntityModel>>) bondDialogModel.getBondingOptions().getItems();
            boolean found = false;
            KeyValuePairCompat<String, EntityModel> customKey = null;
            for (KeyValuePairCompat<String, EntityModel> pair : items) {
                String key = pair.getKey();
                if (key.equals(bondOptions)) {
                    bondDialogModel.getBondingOptions().setSelectedItem(pair);
                    found = true;
                    break;
                } else {
                    if ("custom".equals(key)) { //$NON-NLS-1$
                        customKey = pair;
                    }
                }
            }
            if (!found) {
                EntityModel value = new EntityModel();
                value.setEntity(bondOptions);
                customKey.setValue(value);
                bondDialogModel.getBondingOptions().setSelectedItem(customKey);
            }

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void ExecuteCommand(UICommand command) {
                    setBondOptions(entity, bondDialogModel);
                    sourceListModel.setConfirmWindow(null);
                }
            };
        } else if (item instanceof LogicalNetworkModel) {
            final LogicalNetworkModel logicalNetwork = (LogicalNetworkModel) item;
            final VdsNetworkInterface entity =
                    logicalNetwork.hasVlan() ? logicalNetwork.getVlanNic().getEntity()
                            : logicalNetwork.getAttachedToNic().getEntity();

            if (logicalNetwork.isManagement()) {
                /*****************
                 * Management Network Dialog
                 *****************/
                editPopup = new HostManagementNetworkModel(true);
                final HostManagementNetworkModel mgmntDialogModel = (HostManagementNetworkModel) editPopup;
                mgmntDialogModel.setTitle(ConstantsManager.getInstance().getConstants().editManagementNetworkTitle());
                mgmntDialogModel.setOriginalNetParams(netToBeforeSyncParams.get(logicalNetwork.getName()));
                mgmntDialogModel.setEntity(logicalNetwork.getEntity());
                mgmntDialogModel.getAddress().setEntity(entity.getAddress());
                mgmntDialogModel.getSubnet().setEntity(entity.getSubnet());
                mgmntDialogModel.getGateway().setEntity(entity.getGateway());
                mgmntDialogModel.setNoneBootProtocolAvailable(false);
                mgmntDialogModel.getBondingOptions().setIsAvailable(false);
                mgmntDialogModel.getInterface().setIsAvailable(false);
                mgmntDialogModel.setBootProtocol(entity.getBootProtocol());

                mgmntDialogModel.getIsToSync().setIsChangable(!logicalNetwork.isInSync());
                mgmntDialogModel.getIsToSync()
                        .setEntity(HostSetupNetworksModel.this.networksToSync.contains(logicalNetwork.getName()));

                // OK Target
                okTarget = new BaseCommandTarget() {
                    @Override
                    public void ExecuteCommand(UICommand command) {
                        if (!mgmntDialogModel.Validate()) {
                            return;
                        }
                        entity.setBootProtocol(mgmntDialogModel.getBootProtocol());
                        entity.setAddress((String) mgmntDialogModel.getAddress().getEntity());
                        entity.setSubnet((String) mgmntDialogModel.getSubnet().getEntity());
                        entity.setGateway((String) mgmntDialogModel.getGateway().getEntity());

                        if ((Boolean) mgmntDialogModel.getIsToSync().getEntity()) {
                            HostSetupNetworksModel.this.networksToSync.add(logicalNetwork.getName());
                        } else {
                            HostSetupNetworksModel.this.networksToSync.remove(logicalNetwork.getName());
                        }

                        sourceListModel.setConfirmWindow(null);
                    }
                };
            } else {
                /*****************
                 * Network Dialog
                 *****************/
                editPopup = new HostInterfaceModel(true);
                final HostInterfaceModel networkDialogModel = (HostInterfaceModel) editPopup;
                networkDialogModel.setTitle(ConstantsManager.getInstance()
                        .getMessages()
                        .editNetworkTitle(logicalNetwork.getName()));
                networkDialogModel.setOriginalNetParams(netToBeforeSyncParams.get(logicalNetwork.getName()));
                networkDialogModel.getAddress().setEntity(entity.getAddress());
                networkDialogModel.getSubnet().setEntity(entity.getSubnet());
                networkDialogModel.getName().setIsAvailable(false);
                networkDialogModel.getBondingOptions().setIsAvailable(false);

                networkDialogModel.getNetwork().setIsChangable(false);
                networkDialogModel.getNetwork().setSelectedItem(logicalNetwork.getEntity());

                networkDialogModel.setBootProtocol(entity.getBootProtocol());

                networkDialogModel.getIsToSync().setIsChangable(!logicalNetwork.isInSync());
                networkDialogModel.getIsToSync()
                        .setEntity(HostSetupNetworksModel.this.networksToSync.contains(logicalNetwork.getName()));

                // OK Target
                okTarget = new BaseCommandTarget() {
                    @Override
                    public void ExecuteCommand(UICommand command) {
                        if (!networkDialogModel.Validate()) {
                            return;
                        }
                        entity.setBootProtocol(networkDialogModel.getBootProtocol());
                        entity.setAddress((String) networkDialogModel.getAddress().getEntity());
                        entity.setSubnet((String) networkDialogModel.getSubnet().getEntity());

                        if ((Boolean) networkDialogModel.getIsToSync().getEntity()) {
                            HostSetupNetworksModel.this.networksToSync.add(logicalNetwork.getName());
                        } else {
                            HostSetupNetworksModel.this.networksToSync.remove(logicalNetwork.getName());
                        }

                        sourceListModel.setConfirmWindow(null);
                    }
                };
            }
        }

        // ok command
        UICommand okCommand = new UICommand("OK", okTarget); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);

        // cancel command
        UICommand cancelCommand = new UICommand("Cancel", new BaseCommandTarget() { //$NON-NLS-1$
                    @Override
                    public void ExecuteCommand(UICommand command) {
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
                    public void ExecuteCommand(UICommand command) {
                        sourceListModel.setConfirmWindow(null);
                    }
                });
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);

        if (operation.isNullOperation()) {
            return;
        } else if (operation == NetworkOperation.BOND_WITH) {
            final HostBondInterfaceModel bondPopup = new HostBondInterfaceModel(true);
            bondPopup.setTitle(ConstantsManager.getInstance().getConstants().createNewBondTitle());
            bondPopup.getNetwork().setIsAvailable(false);
            bondPopup.getCheckConnectivity().setIsAvailable(false);
            bondPopup.setBootProtocol(NetworkBootProtocol.NONE);
            bondPopup.getAddress().setIsAvailable(false);
            bondPopup.getSubnet().setIsAvailable(false);
            bondPopup.getGateway().setIsAvailable(false);
            bondPopup.setBootProtocolAvailable(false);
            List<VdsNetworkInterface> freeBonds = getFreeBonds();
            if (freeBonds.isEmpty()) {
                popupWindow = new ConfirmationModel();
                popupWindow.setTitle(ConstantsManager.getInstance().getConstants().errorTitle());
                popupWindow.setMessage(ConstantsManager.getInstance().getConstants().thereAreNoAvailableBondsMsg());
                popupWindow.getCommands().add(cancelCommand);
                sourceListModel.setConfirmWindow(popupWindow);
                return;
            }
            bondPopup.getBond().setItems(freeBonds);
            bondPopup.getCommands().add(new UICommand("OK", new BaseCommandTarget() { //$NON-NLS-1$

                        @Override
                        public void ExecuteCommand(UICommand command) {
                            sourceListModel.setConfirmWindow(null);
                            VdsNetworkInterface bond = (VdsNetworkInterface) bondPopup.getBond().getSelectedItem();
                            setBondOptions(bond, bondPopup);
                            NetworkInterfaceModel nic1 = (NetworkInterfaceModel) networkCommand.getOp1();
                            NetworkInterfaceModel nic2 = (NetworkInterfaceModel) networkCommand.getOp2();
                            List<LogicalNetworkModel> networks =
                                    nic1.getItems().size() != 0 ? new ArrayList<LogicalNetworkModel>(nic1.getItems())
                                            : new ArrayList<LogicalNetworkModel>(nic2.getItems());
                            networkCommand.Execute(bond);
                            redraw();

                            // Attach the previous networks
                            for (NetworkInterfaceModel nic : getNics()) {
                                if (nic.getName().equals(bond.getName())) {
                                    NetworkOperation.attachNetworks(nic, networks, allNics);
                                    redraw();
                                    return;
                                }
                            }

                        }
                    }));

            popupWindow = bondPopup;
        } else {
            // just execute the command
            networkCommand.Execute();
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
    protected void OnEntityChanged() {
        super.OnEntityChanged();
        initAllModels(true);
    }

    protected void onNicsChanged() {
        operationFactory = new NetworkOperationFactory(getNetworks(), getNics());
        queryFreeBonds();
        validate();
        getNetworksChangedEvent().raise(this, EventArgs.Empty);
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

    private List<VdsNetworkInterface> getFreeBonds() {
        List<VdsNetworkInterface> freeBonds = new ArrayList<VdsNetworkInterface>();
        for (VdsNetworkInterface bond : allBonds) {
            if (!nicMap.containsKey(bond.getName())) {
                freeBonds.add(bond);
            }
        }
        return freeBonds;
    }

    private void initAllModels(boolean fetchFromBackend) {
        if (fetchFromBackend) {
            // run query for networks, this chains the query for nics, and also stops progress when done
            StartProgress(null);
            queryNetworks();
        } else {
            initNetworkModels();
            initNicModels();
        }
    }

    private void initNetworkModels() {
        Map<String, LogicalNetworkModel> networkModels = new HashMap<String, LogicalNetworkModel>();
        for (Network network : allNetworks) {
            networkModels.put(network.getName(), new LogicalNetworkModel(network, this));
        }
        setNetworks(networkModels);
    }

    private void initNicModels() {
        Map<String, NetworkInterfaceModel> nicModels = new HashMap<String, NetworkInterfaceModel>();
        Map<String, VdsNetworkInterface> nicMap = new HashMap<String, VdsNetworkInterface>();
        Map<String, VdsNetworkInterface> physicalNics = new HashMap<String, VdsNetworkInterface>();
        Map<String, List<String>> bondToNic = new HashMap<String, List<String>>();
        Map<String, List<String>> nicToNetwork = new HashMap<String, List<String>>();

        // map all nics
        for (VdsNetworkInterface nic : allNics) {
            nicMap.put(nic.getName(), nic);
        }

        // pass over all nics
        for (VdsNetworkInterface nic : allNics) {
            // is this a management nic? (comes from backend)
            boolean isNicManagement = nic.getIsManagement();
            final String nicName = nic.getName();
            final String networkName = nic.getNetworkName();
            final String bondName = nic.getBondName();
            final Integer vlanId = nic.getVlanId();
            final int dotpos = nicName.indexOf('.');

            // is this a physical nic?
            boolean isPhysicalInterface = vlanId == null;

            if (isPhysicalInterface) {
                physicalNics.put(nicName, nic);
            }

            // is the nic bonded?
            if (bondName != null) {
                if (bondToNic.containsKey(bondName)) {
                    bondToNic.get(bondName).add(nicName);
                } else {
                    List<String> bondedNics = new ArrayList<String>();
                    bondedNics.add(nicName);
                    bondToNic.put(bondName, bondedNics);
                }
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

                // bridge name is either <nic>, <nic.vlanid> or <bond.vlanid>
                String ifName;
                if (dotpos > 0) {
                    ifName = nicName.substring(0, dotpos);
                } else {
                    ifName = nicName;
                }
                Collection<LogicalNetworkModel> nicNetworks = new ArrayList<LogicalNetworkModel>();
                nicNetworks.add(networkModel);
                // set iface bridge to network
                NetworkInterfaceModel existingEridge = networkModel.getVlanNic();
                assert existingEridge == null : "should have only one bridge, but found " + existingEridge; //$NON-NLS-1$
                networkModel.setBridge(new NetworkInterfaceModel(nic, nicNetworks, this));

                if (nicToNetwork.containsKey(ifName)) {
                    nicToNetwork.get(ifName).add(networkName);
                } else {
                    List<String> bridgedNetworks = new ArrayList<String>();
                    bridgedNetworks.add(networkName);
                    nicToNetwork.put(ifName, bridgedNetworks);
                }

                if (!networkModel.isInSync() && networkModel.isManaged()) {
                    netToBeforeSyncParams.put(networkName, new NetworkParameters(nic));
                }
            }
        }

        // build models
        for (VdsNetworkInterface nic : physicalNics.values()) {
            String nicName = nic.getName();
            // dont show bonded nics
            if (nic.getBondName() != null) {
                continue;
            }
            List<LogicalNetworkModel> nicNetworks = new ArrayList<LogicalNetworkModel>();
            List<String> networkNames = nicToNetwork.get(nicName);
            if (networkNames != null) {
                for (String networkName : networkNames) {
                    LogicalNetworkModel networkModel;
                    networkModel = networkMap.get(networkName);
                    nicNetworks.add(networkModel);
                }
            }
            List<String> bondedNicNames = bondToNic.get(nicName);
            NetworkInterfaceModel nicModel;
            if (bondedNicNames != null) {
                List<NetworkInterfaceModel> bondedNics = new ArrayList<NetworkInterfaceModel>();
                for (String bondedNicName : bondedNicNames) {
                    VdsNetworkInterface bonded = nicMap.get(bondedNicName);
                    NetworkInterfaceModel bondedModel = new NetworkInterfaceModel(bonded, this);
                    bondedModel.setBonded(true);
                    bondedNics.add(bondedModel);
                }
                nicModel = new BondNetworkInterfaceModel(nic, nicNetworks, bondedNics, this);
            } else {
                nicModel = new NetworkInterfaceModel(nic, nicNetworks, this);
            }

            nicModels.put(nicName, nicModel);
        }
        setNics(nicModels);
    }

    private void queryFreeBonds() {
        // query for all bonds on the host
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue)
            {
                List<VdsNetworkInterface> bonds =
                        (List<VdsNetworkInterface>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                allBonds = bonds;
            }
        };

        VDS vds = (VDS) getEntity();
        Frontend.RunQuery(VdcQueryType.GetVdsFreeBondsByVdsId, new IdQueryParameters(vds.getId()), asyncQuery);
    }

    private void queryInterfaces() {
        // query for interfaces
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValueObj)
            {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnValueObj;
                Object returnValue2 = returnValue.getReturnValue();
                List<VdsNetworkInterface> allNics = (List<VdsNetworkInterface>) returnValue2;
                HostSetupNetworksModel.this.allNics = allNics;
                initNicModels();
                StopProgress();
            }
        };

        VDS vds = (VDS) getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        Frontend.RunQuery(VdcQueryType.GetVdsInterfacesByVdsId, params, asyncQuery);
    }

    private void queryNetworks() {
        // query for networks
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue)
            {
                List<Network> networks = (List<Network>) returnValue;
                allNetworks = networks;
                initNetworkModels();
                initDcNetworkParams();

                // chain the nic query
                queryInterfaces();
            }
        };

        VDS vds = (VDS) getEntity();
        AsyncDataProvider.GetClusterNetworkList(asyncQuery, vds.getVdsGroupId());
    }

    private void initDcNetworkParams() {
        for (Network network : allNetworks) {
            netTodcParams.put(network.getName(), new DcNetworkParams(network));
        }
    }

    private void setBondOptions(VdsNetworkInterface entity, HostBondInterfaceModel bondDialogModel) {
        KeyValuePairCompat<String, EntityModel> BondPair =
                (KeyValuePairCompat<String, EntityModel>) bondDialogModel.getBondingOptions()
                        .getSelectedItem();
        String key = BondPair.getKey();
        entity.setBondOptions((String) ("custom".equals(key) ? BondPair.getValue().getEntity() : key)); //$NON-NLS-1$
    }

    private void setNetworks(Map<String, LogicalNetworkModel> networks) {
        networkMap = networks;
        getNetworksChangedEvent().raise(this, EventArgs.Empty);
    }

    private void setNetworksChangedEvent(Event value) {
        privateNetworksChangedEvent = value;
    }

    private void setNics(Map<String, NetworkInterfaceModel> nics) {
        nicMap = nics;
        onNicsChanged();
        getNicsChangedEvent().raise(this, EventArgs.Empty);
    }

    private void setNicsChangedEvent(Event value) {
        privateNicsChangedEvent = value;
    }

    private void setOperationCandidateEvent(Event event) {
        privateOperationCandidateEvent = event;
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
        AsyncDataProvider.GetNetworkConnectivityCheckTimeoutInSeconds(new AsyncQuery(sourceListModel,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        getConnectivityTimeout().setEntity(returnValue);
                        postOnSetupNetworks();
                    }
                }));
    }

    public void postOnSetupNetworks() {
        final HostSetupNetworksModel model = (HostSetupNetworksModel) sourceListModel.getWindow();

        SetupNetworksParameters params = new SetupNetworksParameters();
        params.setInterfaces(model.getAllNics());
        params.setCheckConnectivity((Boolean) model.getCheckConnectivity().getEntity());
        params.setConectivityTimeout((Integer) model.getConnectivityTimeout().getEntity());
        params.setVdsId(((VDS) getEntity()).getId());
        params.setNetworksToSync(model.getNetworksToSync());

        model.StartProgress(null);
        Frontend.RunAction(VdcActionType.SetupNetworks, params, new IFrontendActionAsyncCallback() {

            @Override
            public void Executed(FrontendActionAsyncResult result) {
                VdcReturnValueBase returnValueBase = result.getReturnValue();
                if (returnValueBase != null && returnValueBase.getSucceeded())
                {
                    EntityModel commitChanges = model.getCommitChanges();
                    if ((Boolean) commitChanges.getEntity())
                    {
                        new SaveNetworkConfigAction(sourceListModel, model, (VDS) getEntity()).execute();
                    }
                    else
                    {
                        model.StopProgress();
                        sourceListModel.setWindow(null);
                        sourceListModel.Search();
                    }
                }
                else
                {
                    model.StopProgress();
                }
            }
        });
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSetupNetworks")) //$NON-NLS-1$
        {
            onSetupNetworks();
        } else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            cancel();
        }

    }

    private void cancel() {
        sourceListModel.setWindow(null);

    }

}
