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

import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
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
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.DataFromHostSetupNetworksModel;
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

    private Map<Guid, Guid> vfMap;

    private Map<String, NetworkInterfaceModel> nicMap;

    private Map<String, LogicalNetworkModel> networkMap;

    private Map<String, NetworkLabelModel> networkLabelMap = new HashMap<>();

    private final NewNetworkLabelModel newLabelModel;

    public List<NetworkAttachment> getExistingNetworkAttachments() {
        return existingNetworkAttachments;
    }

    private List<NetworkAttachment> existingNetworkAttachments;


    private DataFromHostSetupNetworksModel hostSetupNetworksParametersData = new DataFromHostSetupNetworksModel();

    private Map<String, String> labelToIface = new HashMap<>();

    private final Set<String> networksToSync = new HashSet<>();

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

        hostSetupNetworksParametersData.networksToSync = networksToSync;
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
            onOperation(candidate, candidate.getCommand(op1, op2, hostSetupNetworksParametersData));
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
        return operationFactory.commandsFor(item, hostSetupNetworksParametersData);
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
            boolean doesBondHaveVmNetworkAttached = doesBondHaveVmNetworkAttached((NetworkInterfaceModel) item);
            final VdsNetworkInterface entity = ((NetworkInterfaceModel) item).getIface();
            editPopup = new SetupNetworksEditBondModel(entity, doesBondHaveVmNetworkAttached);
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

                    Bond bond = (Bond) entity;
                    onBondEditUpdateParams(bond);
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
            final LogicalNetworkModel logicalNetworkModel = (LogicalNetworkModel) item;
            final VdsNetworkInterface nic =
                    logicalNetworkModel.hasVlan() ? logicalNetworkModel.getVlanNicModel().getIface()
                            : logicalNetworkModel.getAttachedToNic().getIface();

            final HostInterfaceModel networkDialogModel;
            String version = getEntity().getVdsGroupCompatibilityVersion().getValue();
            final Network network = logicalNetworkModel.getNetwork();
            final String logicalNetworkModelName = network.getName();

            if (logicalNetworkModel.isManagement()) {
                networkDialogModel = new HostManagementNetworkModel(true);
                networkDialogModel.setTitle(ConstantsManager.getInstance().getConstants().editManagementNetworkTitle());
                networkDialogModel.setEntity(network);
                networkDialogModel.setNoneBootProtocolAvailable(false);
                networkDialogModel.getInterface().setIsAvailable(false);
            } else {
                networkDialogModel = new HostInterfaceModel(true);
                networkDialogModel.setTitle(ConstantsManager.getInstance()
                        .getMessages()
                        .editNetworkTitle(logicalNetworkModelName));
                networkDialogModel.getName().setIsAvailable(false);
                networkDialogModel.getNetwork().setIsChangeable(false);
                networkDialogModel.getGateway()
                        .setIsAvailable((Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.MultipleGatewaysSupported,
                                version));
            }

            networkDialogModel.getNetwork().setSelectedItem(network);
            networkDialogModel.setOriginalNetParams(netToBeforeSyncParams.get(logicalNetworkModelName));
            networkDialogModel.getAddress().setEntity(nic.getAddress());
            networkDialogModel.getSubnet().setEntity(nic.getSubnet());
            networkDialogModel.getGateway().setEntity(nic.getGateway());
            networkDialogModel.getBondingOptions().setIsAvailable(false);
            networkDialogModel.setBootProtocol(nic.getBootProtocol());

            NetworkAttachment networkAttachment =
                getNetworkAttachmentForNetwork(network.getId());

            if ((Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.HostNetworkQosSupported,
                    version)) {
                networkDialogModel.getQosOverridden().setIsAvailable(true);
                networkDialogModel.getQosModel().setIsAvailable(true);

                networkDialogModel.getQosOverridden().setEntity(networkAttachment != null && networkAttachment.isQosOverridden());

                networkDialogModel.getQosModel().init(nic.getQos());
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
                if (!network.isVmNetwork()) {
                    validProperties.remove("bridge_opts"); //$NON-NLS-1$
                }
                validProperties.putAll(KeyValueModel.convertProperties((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.UserDefinedNetworkCustomProperties,
                        version)));
                customPropertiesModel.setKeyValueMap(validProperties);
                customPropertiesModel.deserialize(KeyValueModel.convertProperties(networkAttachment.getProperties()));
            }

            networkDialogModel.getIsToSync().setIsChangeable(!logicalNetworkModel.isInSync());
            networkDialogModel.getIsToSync()
                    .setEntity(networksToSync.contains(logicalNetworkModelName));

            editPopup = networkDialogModel;

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void executeCommand(UICommand command) {
                    if (!networkDialogModel.validate()) {
                        return;
                    }
                    nic.setBootProtocol(networkDialogModel.getBootProtocol());
                    if (networkDialogModel.getIsStaticAddress()) {
                        nic.setAddress(networkDialogModel.getAddress().getEntity());
                        nic.setSubnet(networkDialogModel.getSubnet().getEntity());
                        nic.setGateway(networkDialogModel.getGateway().getEntity());
                    }

                    HostNetworkQos displayedQos = getDisplayedQos();

                    if (displayedQos != null) {
                        nic.setQos(displayedQos);
                    }

                    if (networkDialogModel.getIsToSync().getEntity()) {
                        networksToSync.add(logicalNetworkModelName);
                    } else {
                        networksToSync.remove(logicalNetworkModelName);
                    }

                    boolean customPropertiesAvailable = networkDialogModel.getCustomPropertiesModel().getIsAvailable();
                    Map<String, String> customProperties = customPropertiesAvailable
                        ? KeyValueModel.convertProperties(networkDialogModel.getCustomPropertiesModel().serialize())
                        : null;

                    removePreviousNetworkAttachmentInstanceFromRequestAndAddNewOne(logicalNetworkModel,
                        getOverridingHostNetworkQos(displayedQos),
                        customProperties);

                    sourceListModel.setConfirmWindow(null);
                }

                private HostNetworkQos getDisplayedQos() {
                    HostNetworkQos displayedQos = null;

                    if (networkDialogModel.getQosModel().getIsAvailable()) {
                        displayedQos = new HostNetworkQos();
                        networkDialogModel.getQosModel().flush(displayedQos);
                    }

                    return displayedQos;
                }

                private HostNetworkQos getOverridingHostNetworkQos(HostNetworkQos displayedQos) {
                    boolean qosAvailableAndSet = displayedQos != null
                            && networkDialogModel.getQosOverridden().getEntity();

                    if (qosAvailableAndSet) {
                        if (networkDialogModel.getQosModel().getIsChangable()) {
                            return displayedQos;
                        } else {
                            return getNetworkAttachmentForNetwork(network.getId()).getHostNetworkQos();
                        }
                    } else {
                        return null;
                    }
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

    public NetworkAttachment getNetworkAttachmentForNetwork(Guid networkId) {
        //unset networkId may mean a bug, but also it can be unmanaged network, which does not have ids.
        if (networkId == null) {
            return null;
        }

        NetworkAttachment updatedAttachment =
                new MapNetworkAttachments(hostSetupNetworksParametersData.newOrModifiedNetworkAttachments).byNetworkId()
                        .get(networkId);

        if (updatedAttachment != null) {
            return updatedAttachment;
        }

        NetworkAttachment existingAttachment = getExistingAttachmentByNetworkId(networkId);

        if (existingAttachment != null && !shouldBeRemoved(existingAttachment.getId())) {
            return existingAttachment;
        }

        return null;
    }

    private boolean shouldBeRemoved(Guid attachmentId) {
        Set<Guid> removedAttachmentIds =
                new HashSet<>(Entities.getIds(hostSetupNetworksParametersData.removedNetworkAttachments));
        return removedAttachmentIds.contains(attachmentId);
    }

    private void onBondEditUpdateParams(Bond bond) {
        for (Iterator<Bond> iter = hostSetupNetworksParametersData.newOrModifiedBonds.iterator(); iter.hasNext();) {
            Bond oldModifiedBond = iter.next();
            if (oldModifiedBond.getName().equals(bond.getName())) {
                iter.remove();
                break;
            }
        }

        hostSetupNetworksParametersData.newOrModifiedBonds.add(bond);
    }

    public void removePreviousNetworkAttachmentInstanceFromRequestAndAddNewOne(
        LogicalNetworkModel logicalNetwork,
        HostNetworkQos overridingQos,
        Map<String, String> customProperties) {

        Network updatedNetwork = logicalNetwork.getNetwork();
        Guid updatedNetworkId = updatedNetwork.getId();

        Map<Guid, NetworkAttachment> networkIdToPreexistingNetworkAttachment =
                new MapNetworkAttachments(existingNetworkAttachments).byNetworkId();

        Map<Guid, NetworkAttachment> networkIdToNewOrUpdatedNetworkAttachments =
                new MapNetworkAttachments(hostSetupNetworksParametersData.newOrModifiedNetworkAttachments).byNetworkId();

        NetworkAttachment preexistingNetworkAttachment =
                networkIdToPreexistingNetworkAttachment.get(updatedNetworkId);

        Guid networkAttachmentId =
                preexistingNetworkAttachment == null ? null : preexistingNetworkAttachment.getId();

        NetworkAttachment previousUpdate = networkIdToNewOrUpdatedNetworkAttachments.get(updatedNetworkId);
        hostSetupNetworksParametersData.newOrModifiedNetworkAttachments.remove(previousUpdate);

        NetworkAttachment updatedNetworkAttachment =
            NetworkOperation.newNetworkAttachment(updatedNetwork,
                logicalNetwork.getAttachedToNic().getIface(),
                logicalNetwork.getVlanNicModel() == null ? null : logicalNetwork.getVlanNicModel().getIface(),
                networkAttachmentId,
                hostSetupNetworksParametersData.networksToSync,
                overridingQos,
                customProperties);
        hostSetupNetworksParametersData.newOrModifiedNetworkAttachments.add(updatedNetworkAttachment);
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
            boolean doesBondHaveVmNetworkAttached = doesBondHaveVmNetworkAttached((NetworkInterfaceModel) networkCommand.getOp1(),
                    (NetworkInterfaceModel) networkCommand.getOp2());
            if (operation == NetworkOperation.BOND_WITH) {
                bondPopup =
                        new SetupNetworksAddBondModel(getFreeBonds(), nextBondName, doesBondHaveVmNetworkAttached);
            } else {
                bondPopup =
                        new SetupNetworksJoinBondsModel(getFreeBonds(),
                                (BondNetworkInterfaceModel) networkCommand.getOp1(),
                                (BondNetworkInterfaceModel) networkCommand.getOp2(),
                                doesBondHaveVmNetworkAttached);
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
                                    hostSetupNetworksParametersData)
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
        NetworkOperation.moveLabels(srcIfaces, dstIface, hostSetupNetworksParametersData);
    }

    private void commitNetworkChanges(VdsNetworkInterface iface, List<LogicalNetworkModel> networks) {
        NetworkInterfaceModel bondModel = nicMap.get(iface.getName());
        NetworkOperation.attachNetworks(bondModel,
            new ArrayList<LogicalNetworkModel>(networks),
            hostSetupNetworksParametersData);
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
        List<VdsNetworkInterface> physicalNics = new ArrayList<>();
        Map<String, List<VdsNetworkInterface>> bondToNic = new HashMap<>();
        Map<String, Set<LogicalNetworkModel>> nicToNetwork = new HashMap<>();
        List<LogicalNetworkModel> errorLabelNetworks = new ArrayList<>();
        labelToIface.clear();

        // map all nics
        final Map<Guid, VdsNetworkInterface> nicsById =  Entities.businessEntitiesById(allNics);
        final Map<String, VdsNetworkInterface> nicMap = Entities.entitiesByName(allNics);

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
                    networkModel.setVlanNicModel(new NetworkInterfaceModel(nic, nicNetworks, null, false, null, this));
                }
                nicToNetwork.get(ifName).add(networkModel);

                if (!networkModel.isInSync() && networkModel.isManaged()) {
                    NetworkAttachment existingNetworkAttachment = getExistingAttachmentByNetworkId(networkModel.getNetwork().getId());
                    netToBeforeSyncParams.put(networkName, createBeforeSyncNetParams(nic, existingNetworkAttachment));
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

            if (bondedNics != null) {
                List<NetworkInterfaceModel> bondedModels = new ArrayList<>();
                for (VdsNetworkInterface bonded : bondedNics) {
                    final VdsNetworkInterface physicalFunction = findPhysicalFunction(nicsById, bonded.getId());
                    NetworkInterfaceModel bondedModel = new NetworkInterfaceModel(bonded,
                            nicToVfsConfig.containsKey(bonded.getId()),
                            physicalFunction,
                            this);
                    bondedModel.setBonded(true);
                    bondedModels.add(bondedModel);
                }
                BondNetworkInterfaceModel bondNetworkInterfaceModel = new BondNetworkInterfaceModel((Bond)nic,
                        nicNetworks,
                        nicLabels,
                        bondedModels,
                        this);

                nicModels.put(nicName, bondNetworkInterfaceModel);
            } else {
                final VdsNetworkInterface physicalFunction = findPhysicalFunction(nicsById, nic.getId());
                NetworkInterfaceModel nicModel = new NetworkInterfaceModel(nic,
                        nicNetworks,
                        nicLabels,
                        nicToVfsConfig.containsKey(nic.getId()),
                        physicalFunction,
                        this);

                nicModels.put(nicName, nicModel);
            }
        }
        initLabeledNetworksErrorMessages(errorLabelNetworks, nicModels);
        setNics(nicModels);
    }

    private VdsNetworkInterface findPhysicalFunction(Map<Guid, VdsNetworkInterface> nicsById, Guid nicId) {
        final boolean vf = vfMap.containsKey(nicId);
        final VdsNetworkInterface physicalFunction;
        if (vf) {
            final Guid pfId = vfMap.get(nicId);
            physicalFunction = nicsById.get(pfId);
        } else {
            physicalFunction = null;
        }
        return physicalFunction;
    }

    private NetworkParameters createBeforeSyncNetParams(VdsNetworkInterface nic, NetworkAttachment attachment) {
        NetworkParameters params = new NetworkParameters();
        params.setBootProtocol(nic.getBootProtocol());
        params.setAddress(nic.getAddress());
        params.setSubnet(nic.getSubnet());
        params.setGateway(nic.getGateway());
        params.setQos(nic.getQos());

        params.setQosOverridden(attachment.isQosOverridden());
        params.setCustomProperties(attachment.getProperties());

        return params;
    }

    private NetworkAttachment getExistingAttachmentByNetworkId(Guid networkId) {
        NetworkAttachment existingNetworkAttachment =
                new MapNetworkAttachments(existingNetworkAttachments).byNetworkId()
                        .get(networkId);
        return existingNetworkAttachment;
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
                allBonds = ((VdcQueryReturnValue) returnValue).getReturnValue();

                initNetworkModels();
                initDcNetworkParams();
                initNicModels();

                hostSetupNetworksParametersData.allNics = allNics;
                hostSetupNetworksParametersData.existingNetworkAttachments = existingNetworkAttachments;

                stopProgress();
            }
        };

        VDS vds = getEntity();
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsFreeBondsByVdsId,
            new IdQueryParameters(vds.getId()),
            asyncQuery);
    }

    private void queryVfMap() {
        final AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValueObj) {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnValueObj;
                vfMap = returnValue.getReturnValue();
                if (vfMap == null) {
                    vfMap = Collections.emptyMap();
                }

                // chain the free bonds query
                queryFreeBonds();
            }
        };

        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        Frontend.getInstance().runQuery(VdcQueryType.GetVfToPfMapByHostId, params, asyncQuery);
    }

    private void queryInterfaces() {
        // query for interfaces
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValueObj) {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnValueObj;
                allNics = returnValue.getReturnValue();

                // chain the network attachments query
                queryNetworkAttachments();
            }
        };

        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        Frontend.getInstance().runQuery(VdcQueryType.GetVdsInterfacesByVdsId, params, asyncQuery);
    }

    private void queryNetworkAttachments() {
        // query for network attachments
        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValueObj) {
                VdcQueryReturnValue returnValue = (VdcQueryReturnValue) returnValueObj;
                Object returnValue2 = returnValue.getReturnValue();
                HostSetupNetworksModel.this.existingNetworkAttachments = (List<NetworkAttachment>) returnValue2;

                // chain the vfsConfig query
                queryVfsConfig();
            }
        };

        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        Frontend.getInstance().runQuery(VdcQueryType.GetNetworkAttachmentsByHostId, params, asyncQuery);
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

                queryVfMap();
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
                allNetworks = (List<Network>) returnValue;
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

    public Set<String> getNetworksToSync() {
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
        UiAction setupNetworksAction = createSetupNetworksAction();
        setupNetworksAction
                .then(getVfsConfigAction())
                .then(getCommitNetworkChangesAction())
                .onAllExecutionsFinish(getCloseAction());

        setupNetworksAction.runAction();
    }

    private UiAction createSetupNetworksAction() {
        final HostSetupNetworksParameters hostSetupNetworksParameters = createHostSetupNetworksParameters();
        return new UiVdcAction(VdcActionType.HostSetupNetworks, hostSetupNetworksParameters, this, true) {
            @Override
            protected boolean shouldExecute() {
                return !hostSetupNetworksParameters.isEmptyRequest();
            }
        };
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

    public HostSetupNetworksParameters createHostSetupNetworksParameters() {
        HostSetupNetworksParameters result = new HostSetupNetworksParameters(getEntity().getId());

        result.setNetworkAttachments(hostSetupNetworksParametersData.newOrModifiedNetworkAttachments);
        result.setRemovedNetworkAttachments(
            new HashSet<>(Entities.getIds(hostSetupNetworksParametersData.removedNetworkAttachments)));

        NetworkCommonUtils.fillBondSlaves(allNics);
        result.setBonds(hostSetupNetworksParametersData.newOrModifiedBonds);
        result.setRemovedBonds(new HashSet<>(Entities.getIds(hostSetupNetworksParametersData.removedBonds)));
        result.setRemovedUnmanagedNetworks(hostSetupNetworksParametersData.removedUnmanagedNetworks);
        result.setLabels(hostSetupNetworksParametersData.addedLabels);
        result.setRemovedLabels(Entities.objectNames(hostSetupNetworksParametersData.removedLabels));

        return result;
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

    private boolean doesBondHaveVmNetworkAttached(NetworkInterfaceModel... networkInterfaceModels){
        for (NetworkInterfaceModel networkInterfaceModel : networkInterfaceModels){
            for (LogicalNetworkModel logicalNetwork : networkInterfaceModel.getItems()) {
                if (logicalNetwork.getNetwork().isVmNetwork()) {
                    return true;
                }
            }
        }
        return false;
    }
}
