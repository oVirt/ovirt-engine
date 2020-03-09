package org.ovirt.engine.ui.uicommonweb.models.hosts;

import static org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos.fromHostNetworkQos;

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

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateOrUpdateBond;
import org.ovirt.engine.core.common.action.HostSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.comparators.LexoNumericComparator;
import org.ovirt.engine.core.common.businessentities.network.AnonymousHostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.LldpInfo;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NicLabel;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.MapNetworkAttachments;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
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
import org.ovirt.engine.ui.uicommonweb.models.hosts.InterfacePropertiesAccessor.FromNetworkAttachmentModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel.AllNetworksSelector;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.DataFromHostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModelParametersHelper;
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

    private EntityModel<Boolean> showVf;

    public EntityModel<Boolean> getShowVf() {
        return showVf;
    }

    public void setShowVf(EntityModel<Boolean> value) {
        showVf = value;
    }

    private static final EventDefinition NICS_CHANGED_EVENT_DEFINITION = new EventDefinition("NicsChanged", //$NON-NLS-1$
            HostSetupNetworksModel.class);

    private static final EventDefinition OPERATION_CANDIDATE_EVENT_DEFINITION =
            new EventDefinition("OperationCandidate", NetworkOperationFactory.class); //$NON-NLS-1$

    private static final EventDefinition LLDP_CHANGED_EVENT_DEFINITION =
            new EventDefinition("LldpChanged", HostSetupNetworksModel.class); //$NON-NLS-1$

    private Event<EventArgs> lldpChangedEvent;

    private Event<OperationCandidateEventArgs> operationCandidateEvent;

    private Event<EventArgs> nicsChangedEvent;

    private List<VdsNetworkInterface> allExistingNics;

    private Map<Guid, Guid> vfMap;

    private Map<String, NetworkInterfaceModel> nicModelByName;

    private Map<String, LogicalNetworkModel> networkModelByName;

    private Map<String, NetworkLabelModel> networkLabelModelByLabel = new HashMap<>();

    private Map<String, LldpInfo> networkLldpInfoByName;

    private final NewNetworkLabelModel newLabelModel;

    private DataFromHostSetupNetworksModel hostSetupNetworksParametersData = new DataFromHostSetupNetworksModel();

    // The purpose of this map is to keep the network parameters while moving the network from one nic to another
    private final Map<String, NetworkParameters> networkToLastDetachParams = new HashMap<>();

    private Set<HostNicVfsConfig> originalVfsConfigs = new HashSet<>();
    private Map<Guid, HostNicVfsConfig> nicToVfsConfig = new HashMap<>();

    private NetworkOperationFactory operationFactory;
    private List<Network> allNetworks;
    private Map<Guid, HostNetworkQos> qosById;
    private final SearchableListModel<VDS, HostInterfaceLineModel> sourceListModel;
    private List<VdsNetworkInterface> allBonds;
    private SortedSet<String> dcLabels;
    private NetworkOperation currentCandidate;
    private NetworkItemModel<?> currentOp1;
    private NetworkItemModel<?> currentOp2;
    private String nextBondName;
    private boolean customBondNameSupported = false;

    private Map<Integer, VdsNetworkInterface> existingVlanDevicesByVlanId;

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

        setNicsChangedEvent(new Event<>(NICS_CHANGED_EVENT_DEFINITION));
        setOperationCandidateEvent(new Event<OperationCandidateEventArgs>(OPERATION_CANDIDATE_EVENT_DEFINITION));
        setLldpChangedEvent(new Event<>(LLDP_CHANGED_EVENT_DEFINITION));
        setCheckConnectivity(new EntityModel<Boolean>());
        getCheckConnectivity().setEntity(true);
        setConnectivityTimeout(new EntityModel<Integer>());
        setCommitChanges(new EntityModel<Boolean>());
        getCommitChanges().setEntity(true);
        setShowVf(new EntityModel<>());
        getShowVf().setEntity(false);
        getShowVf().getPropertyChangedEvent().addListener((ev, sender, args) -> redraw());

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
                return nicModelByName.get(key);
            case NETWORK:
                return networkModelByName.get(key);
            case LABEL:
                NetworkLabelModel labelModel = networkLabelModelByLabel.get(key);
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

    public List<LogicalNetworkModel> getNetworkModels() {
        return new ArrayList<>(networkModelByName.values());
    }

    public List<NetworkInterfaceModel> getNicModels() {
        return new ArrayList<>(nicModelByName.values());
    }

    public List<NetworkLabelModel> getLabelModels() {
        return new ArrayList<>(networkLabelModelByLabel.values());
    }

    public LldpInfo getNetworkLldpByName(String name) {
        if (networkLldpInfoByName != null) {
            return networkLldpInfoByName.get(name);
        }
        return null;
    }

    public boolean isNetworkLldpInfoPresent() {
        return networkLldpInfoByName != null;
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
            hostNicVfsConfig.setNetworkLabels(vfsConfigModel.getLabelsModel().computeSelectedLabels());
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
            BondNetworkInterfaceModel bondModel = (BondNetworkInterfaceModel)item;
            final CreateOrUpdateBond createOrUpdateBondParameter = bondModel.getCreateOrUpdateBond();
            editPopup = new SetupNetworksEditBondModel(createOrUpdateBondParameter, doesBondHaveVmNetworkAttached);
            final SetupNetworksBondModel bondDialogModel = (SetupNetworksBondModel) editPopup;

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void executeCommand(UICommand command) {
                    if (!bondDialogModel.validate(customBondNameSupported)) {
                        return;
                    }
                    sourceListModel.setConfirmWindow(null);
                    setBondOptions(createOrUpdateBondParameter, bondDialogModel);
                    redraw();
                }
            };
        } else if (item instanceof NetworkInterfaceModel) {
            /*******************
             * VFs Config Dialog
             *******************/
            final VdsNetworkInterface entity = ((NetworkInterfaceModel) item).getOriginalIface();
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
                        redraw();
                    }
                };
            }
        } else if (item instanceof LogicalNetworkModel) {
            /*****************
             * Network Dialog
             *****************/
            final LogicalNetworkModel logicalNetworkModel = (LogicalNetworkModel) item;
            final VdsNetworkInterface nic;

            if (logicalNetworkModel.isInSync()) {
                nic = logicalNetworkModel.hasVlan() ? logicalNetworkModel.getVlanDevice()
                        : logicalNetworkModel.getAttachedToNic().getOriginalIface();
            } else {
                nic = logicalNetworkModel.getVlanDevice() != null ? logicalNetworkModel.getVlanDevice()
                        : logicalNetworkModel.getAttachedToNic().getOriginalIface();
            }

            final NetworkAttachmentModel networkAttachmentModel;
            String version = getEntity().getClusterCompatibilityVersion().getValue();
            final Network network = logicalNetworkModel.getNetwork();
            final String logicalNetworkModelName = network.getName();

            final NetworkAttachment networkAttachment =
                    logicalNetworkModel.getNetworkAttachment();

            HostNetworkQos networkQos = qosById.get(network.getQosId());
            DnsResolverConfiguration reportedDnsResolverConfiguration = getEntity().getReportedDnsResolverConfiguration();
            if (logicalNetworkModel.isManagement()) {
                networkAttachmentModel = new ManagementNetworkAttachmentModel(network,
                        nic,
                        networkAttachment,
                        networkQos,
                        reportedDnsResolverConfiguration);
            } else {
                networkAttachmentModel = new NetworkAttachmentModel(network,
                        nic,
                        networkAttachment,
                        networkQos,
                        reportedDnsResolverConfiguration);
            }

            networkAttachmentModel.getQosOverridden().setIsAvailable(true);
            networkAttachmentModel.getQosModel().setIsAvailable(true);
            networkAttachmentModel.setIpv6AutoconfAvailable(!NetworkCommonUtils.isEl8(getEntity().getKernelVersion()));

            KeyValueModel customPropertiesModel = networkAttachmentModel.getCustomPropertiesModel();
            customPropertiesModel.setIsAvailable(true);
            Map<String, String> validProperties =
                    KeyValueModel.convertProperties((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(
                            ConfigValues.PreDefinedNetworkCustomProperties,
                            version));
            // TODO: extract this (and as much surrounding code as possible) into a custom properties utility common
            // to backend and frontend (lvernia)
            if (!network.isVmNetwork()) {
                validProperties.remove("bridge_opts"); //$NON-NLS-1$
            }
            validProperties.putAll(KeyValueModel.convertProperties((String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.UserDefinedNetworkCustomProperties,
                    version)));
            customPropertiesModel.setKeyValueMap(validProperties);
            customPropertiesModel.deserialize(KeyValueModel.convertProperties(networkAttachment.getProperties()));

            networkAttachmentModel.getIsToSync().setIsChangeable(!logicalNetworkModel.isInSync());
            networkAttachmentModel.getIsToSync()
                    .setEntity(shouldSyncNetwork(logicalNetworkModelName));

            networkAttachmentModel.getQosOverridden().setEntity(networkAttachment.isQosOverridden());

            editPopup = networkAttachmentModel;

            // OK Target
            okTarget = new BaseCommandTarget() {
                @Override
                public void executeCommand(UICommand command) {
                    if (!networkAttachmentModel.validate()) {
                        return;
                    }
                    final FromNetworkAttachmentModel interfacePropertiesAccessor = new FromNetworkAttachmentModel(networkAttachmentModel);
                    LogicalNetworkModelParametersHelper.populateIpv4Details(
                            interfacePropertiesAccessor,
                            networkAttachment.getIpConfiguration().getIpv4PrimaryAddress());
                    LogicalNetworkModelParametersHelper.populateIpv6Details(
                            interfacePropertiesAccessor,
                            networkAttachment.getIpConfiguration().getIpv6PrimaryAddress());

                    if (networkAttachmentModel.getQosModel().getIsAvailable()) {
                        if (networkAttachmentModel.getQosOverridden().getEntity()) {
                            HostNetworkQos overriddenQos = new HostNetworkQos();
                            networkAttachmentModel.getQosModel().flush(overriddenQos);
                            AnonymousHostNetworkQos hostNetworkQos = fromHostNetworkQos(overriddenQos);
                            networkAttachment.setHostNetworkQos(hostNetworkQos);
                        } else {
                            networkAttachment.setHostNetworkQos(null);
                        }
                    }

                    if (networkAttachmentModel.getIsToSync().getEntity()) {
                        hostSetupNetworksParametersData.getNetworksToSync().add(logicalNetworkModelName);
                    } else {
                        hostSetupNetworksParametersData.getNetworksToSync().remove(logicalNetworkModelName);
                    }

                    boolean customPropertiesAvailable = networkAttachmentModel.getCustomPropertiesModel().getIsAvailable();
                    Map<String, String> customProperties = customPropertiesAvailable
                            ? KeyValueModel.convertProperties(networkAttachmentModel.getCustomPropertiesModel().serialize())
                            : null;
                    networkAttachment.setProperties(customProperties);

                    networkAttachment.setDnsResolverConfiguration(networkAttachmentModel.getDnsConfigurationModel().flush());

                    sourceListModel.setConfirmWindow(null);
                    redraw();
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

    private boolean shouldSyncNetwork(final String networkName) {
        return hostSetupNetworksParametersData.getNetworksToSync().contains(networkName);
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
                    if (!bondPopup.validate(customBondNameSupported)) {
                        return;
                    }

                    sourceListModel.setConfirmWindow(null);

                    CreateOrUpdateBond bond = new CreateOrUpdateBond();
                    bond.setName(bondPopup.getBond().getSelectedItem());
                    setBondOptions(bond, bondPopup);

                    NetworkInterfaceModel nic1 = (NetworkInterfaceModel) networkCommand.getOp1();
                    NetworkInterfaceModel nic2 = (NetworkInterfaceModel) networkCommand.getOp2();

                    // Store networks
                    List<LogicalNetworkModel> networks = new ArrayList<>();
                    networks.addAll(nic1.getItems());
                    networks.addAll(nic2.getItems());

                    // Store labels
                    List<NetworkLabelModel> labels = new ArrayList<>();
                    labels.addAll(nic1.getLabels());
                    labels.addAll(nic2.getLabels());

                    networkCommand.execute(bond);

                    /*
                     * We are calling the <code>redraw()</code> to create the BondModel which is needed by the following
                     * operations (attaching the networks and the labels to the bond).
                     *
                     * For more details @see #redraw. After executing the <code>networkCommand</code> which creates the
                     * bond, the bondModel still not exist (only the <code>hostSetupNetworksParametersData.bonds</code>
                     * are updated). <code>redraw()</code> has to be called to create it.
                     */
                    redraw();

                    // Attach the previous networks
                    attachNetworks(bond.getName(), networks);

                    // Attach previous labels
                    attachLabels(bond.getName(), labels);

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

    private void attachLabels(String bondName, List<NetworkLabelModel> labels) {
        NetworkInterfaceModel bondModel = nicModelByName.get(bondName);
        NetworkOperation.attachLabels(bondModel, new ArrayList<>(labels), hostSetupNetworksParametersData);
    }

    private void attachNetworks(String bondName, List<LogicalNetworkModel> networks) {
        NetworkInterfaceModel bondModel = nicModelByName.get(bondName);
        NetworkOperation.attachNetworks(bondModel, new ArrayList<>(networks), hostSetupNetworksParametersData);
    }

    /**
     ** Executing <code>NetworkOperation</code> affects the <code>hostSetupNetworksParametersData</code> only. The
     * <code>redraw()</code> method should be called to recreate all the models (networkModels, nicModels, labelModels,
     * etc.) according to the data in the <code>hostSetupNetworksParametersData</code>.
     **/
    public void redraw() {
        initAllModels(false);
    }

    @Override
    protected void onEntityChanged() {
        super.onEntityChanged();
        initAllModels(true);
    }

    private void onNicsChanged() {
        operationFactory = new NetworkOperationFactory(getNetworkModels(), getNicModels());
        validate();
    }

    private LogicalNetworkModel createUnmanagedNetworkModel(String networkName, VdsNetworkInterface nic) {
        Network unmanagedNetwork = new Network();
        unmanagedNetwork.setName(networkName);
        unmanagedNetwork.setVlanId(nic.getVlanId());
        unmanagedNetwork.setMtu(nic.getMtu());
        unmanagedNetwork.setVmNetwork(nic.isBridged());
        LogicalNetworkModel networkModel = new LogicalNetworkModel(unmanagedNetwork, null, this);
        return networkModel;
    }

    private boolean equals(NetworkItemModel<?> item1, NetworkItemModel<?> item2) {
        return Objects.equals(item1, item2);
    }

    private List<String> getFreeBonds() {
        List<String> freeBonds = new ArrayList<>();
        for (VdsNetworkInterface bond : allBonds) {
            if (!nicModelByName.containsKey(bond.getName())) {
                freeBonds.add(bond.getName());
            }
        }
        return freeBonds;
    }

    private void initAllModels(boolean fetchFromBackend) {
        if (fetchFromBackend) {
            // run query for networks, this chains the query for nics, and also stops progress when done
            startProgress();
            queryLabels();
        } else {
            initLabelModels();
            initNetworkModels();
            initNicModels();
        }
    }

    private void initLabelModels() {
        networkLabelModelByLabel.clear();
        for (String label : dcLabels) {
            networkLabelModelByLabel.put(label, new NetworkLabelModel(label, this));
        }
    }

    private void initNetworkModels() {
        Map<String, LogicalNetworkModel> networkModels = new HashMap<>();
        Map<Guid, NetworkAttachment> networkAttachmentByNetworkId = mapNetworkAttachmentsByNetworkId();
        for (Network network : allNetworks) {
            NetworkAttachment attachment = networkAttachmentByNetworkId.get(network.getId());
            LogicalNetworkModel networkModel = new LogicalNetworkModel(network, attachment, this);
            networkModels.put(network.getName(), networkModel);

            if (networkModel.isManagement()) {
                managementNetworkModel = networkModel;
            }

            if (!network.isExternal()) {
                String label = network.getLabel();
                if (label != null) {
                    // The network model is candidate to be drawn as part of the label.
                    // This doesn't yet consider whether it actually exists on the interface.
                    networkLabelModelByLabel.get(label).getNetworks().add(networkModel);
                }
            }
        }
        setNetworks(networkModels);
    }

    private Map<Guid, NetworkAttachment> mapNetworkAttachmentsByNetworkId() {
        return new MapNetworkAttachments(hostSetupNetworksParametersData.getNetworkAttachments()).byNetworkId();
    }

    private void initNicModels() {
        Map<String, Set<LogicalNetworkModel>> nicNameToNetworkModels = createNicNameToNetworkModels();

        Map<String, String> labelToDesiredNicName = new HashMap<>();
        List<LogicalNetworkModel> networkModelsWithLabelError = new ArrayList<>();
        Map<String, List<NetworkLabelModel>> nicNameToLabelModels =
                createNicToLabelModels(nicNameToNetworkModels, networkModelsWithLabelError, labelToDesiredNicName);

        Map<String, List<VdsNetworkInterface>> bondNameToSlaves = getBondNameToSlaves();
        initNextBondName(new ArrayList<>(bondNameToSlaves.keySet()));

        Map<String, NetworkInterfaceModel> nicModels =
                createAllNicModelsByName(nicNameToNetworkModels, nicNameToLabelModels, bondNameToSlaves);

        setLabelErrorsOnNicModels(nicModels, networkModelsWithLabelError, labelToDesiredNicName);
        setNics(nicModels);
    }

    private Map<String, NetworkInterfaceModel> createAllNicModelsByName(
            Map<String, Set<LogicalNetworkModel>> nicNameToNetworkModels,
            Map<String, List<NetworkLabelModel>> nicNameToLabelModels,
            Map<String, List<VdsNetworkInterface>> bondNameToSlaves) {
        Map<Guid, VdsNetworkInterface> nicsById = Entities.businessEntitiesById(allExistingNics);
        Map<String, NetworkInterfaceModel> nicModels = new HashMap<>();
        nicModels.putAll(createBondModels(nicNameToNetworkModels, bondNameToSlaves, nicNameToLabelModels, nicsById));
        nicModels.putAll(
                createRegularNicModels(nicNameToNetworkModels, bondNameToSlaves, nicNameToLabelModels, nicsById));
        return nicModels;
    }

    private Map<String, NetworkInterfaceModel> createRegularNicModels(
            Map<String, Set<LogicalNetworkModel>> nicNameToNetworkModels,
            Map<String, List<VdsNetworkInterface>> bondNameToSlaves,
            Map<String, List<NetworkLabelModel>> nicNameToLabelModels,
            final Map<Guid, VdsNetworkInterface> nicsById) {
        Map<String, NetworkInterfaceModel> regularNicModels = new HashMap<>();
        for (VdsNetworkInterface nic : allExistingNics) {
            if (!isPhysicalNic(nic, bondNameToSlaves.keySet(), getAllSlaveNames())) {
                continue;
            }

            final VdsNetworkInterface physicalFunction = findPhysicalFunction(nicsById, nic.getId());

            String nicName = nic.getName();
            Collection<LogicalNetworkModel> nicNetworks = nicNameToNetworkModels.get(nicName);
            NetworkInterfaceModel nicModel = new NetworkInterfaceModel(nic,
                    nicNetworks,
                    nicNameToLabelModels.get(nicName),
                    nicToVfsConfig.containsKey(nic.getId()),
                    physicalFunction == null ? null : physicalFunction.getName(),
                    this);

            if (physicalFunction != null && !getShowVf().getEntity()) {
                continue;
            }
            regularNicModels.put(nicName, nicModel);
        }
        return regularNicModels;
    }

    private Set<String> getAllSlaveNames() {
        Set<String> slaveNames = new HashSet<>();
        for (CreateOrUpdateBond bond : hostSetupNetworksParametersData.getBonds()) {
            slaveNames.addAll(bond.getSlaves());
        }
        return slaveNames;
    }

    private boolean isPhysicalNic(VdsNetworkInterface nic,
            Set<String> bondNames,
            Set<String> slaveNames) {
        boolean isBond = bondNames.contains(nic.getName());
        boolean isRemovedBond = hostSetupNetworksParametersData.getRemovedBonds().contains(nic.getId());
        boolean isSlave = slaveNames.contains(nic.getName());
        boolean isVlanDevice = NetworkCommonUtils.isVlan(nic);

        return !(isBond || isRemovedBond || isSlave || isVlanDevice);
    }

    private Map<String, NetworkInterfaceModel> createBondModels(
            Map<String, Set<LogicalNetworkModel>> nicNameToNetworkModels,
            Map<String, List<VdsNetworkInterface>> bondNameToSlaves,
            Map<String, List<NetworkLabelModel>> nicNameToLabelModels,
            final Map<Guid, VdsNetworkInterface> nicsById) {
        Map<String, NetworkInterfaceModel> bondModels = new HashMap<>();
        for (CreateOrUpdateBond createOrUpdateBond : hostSetupNetworksParametersData.getBonds()) {
            String bondName = createOrUpdateBond.getName();
            List<NetworkInterfaceModel> slavesModels = createSlaveModels(bondNameToSlaves, nicsById, bondName);
            Collection<LogicalNetworkModel> nicNetworks = nicNameToNetworkModels.get(bondName);
            Bond originalBond = getOriginalBond(nicsById, createOrUpdateBond);

            BondNetworkInterfaceModel bondNetworkInterfaceModel =
                    new BondNetworkInterfaceModel(originalBond,
                            createOrUpdateBond,
                            nicNetworks,
                            nicNameToLabelModels.get(createOrUpdateBond.getName()),
                            slavesModels,
                            this);

            bondModels.put(bondName, bondNetworkInterfaceModel);
        }
        return bondModels;
    }

    private Bond getOriginalBond(final Map<Guid, VdsNetworkInterface> nicsById, CreateOrUpdateBond createOrUpdateBond) {
        if (createOrUpdateBond.getId() != null) {
            assert nicsById.containsKey(
                    createOrUpdateBond.getId()) : "createOrUpdateBond's id is not part of the nicsById map"; //$NON-NLS-1$
        }
        Bond originalBond = createOrUpdateBond.getId() != null
                ? (Bond) nicsById.get(createOrUpdateBond.getId()) : createOrUpdateBond.toBond();
        return originalBond;
    }

    private List<NetworkInterfaceModel> createSlaveModels(Map<String, List<VdsNetworkInterface>> bondNameToSlaves,
            final Map<Guid, VdsNetworkInterface> nicsById,
            String bondName) {
        List<NetworkInterfaceModel> slavesModels = new ArrayList<>();
        for (VdsNetworkInterface slave : bondNameToSlaves.get(bondName)) {
            NetworkInterfaceModel slaveModel = createSlaveModel(nicsById, slave);
            slavesModels.add(slaveModel);
        }
        return slavesModels;
    }

    private NetworkInterfaceModel createSlaveModel(final Map<Guid, VdsNetworkInterface> nicsById,
            VdsNetworkInterface slave) {
        final VdsNetworkInterface physicalFunction = findPhysicalFunction(nicsById, slave.getId());
        NetworkInterfaceModel slaveModel = new NetworkInterfaceModel(slave,
                nicToVfsConfig.containsKey(slave.getId()),
                physicalFunction == null ? null : physicalFunction.getName(),
                this);
        slaveModel.setBonded(true);
        return slaveModel;
    }

    private Map<String, List<NetworkLabelModel>> createNicToLabelModels(
            Map<String, Set<LogicalNetworkModel>> nicNameToNetworkModels,
            List<LogicalNetworkModel> errorLabelNetworks,
            Map<String, String> labelToDesiredNicName) {
        Map<String, List<NetworkLabelModel>> nicToLabelModels = new HashMap<>();

        for (NicLabel nicLabel : hostSetupNetworksParametersData.getLabels()) {
            String label = nicLabel.getLabel();
            String nicName = nicLabel.getNicName();
            labelToDesiredNicName.put(label, nicName);

            Collection<LogicalNetworkModel> nicNetworks = nicNameToNetworkModels.get(nicName);

            NetworkLabelModel labelModel = networkLabelModelByLabel.get(label);
            assert labelModel != null : "NicLabel should have a NetworkLabelModel"; //$NON-NLS-1$

            markNetworkModelsAsAttachedViaLabel(errorLabelNetworks, nicNetworks, labelModel);

            if (nicToLabelModels.get(nicName) == null) {
                nicToLabelModels.put(nicName, new ArrayList<NetworkLabelModel>());
            }
            nicToLabelModels.get(nicName).add(labelModel);
        }
        return nicToLabelModels;
    }

    private void markNetworkModelsAsAttachedViaLabel(List<LogicalNetworkModel> errorLabelNetworks,
            Collection<LogicalNetworkModel> nicNetworks,
            NetworkLabelModel labelModel) {
        for (Iterator<LogicalNetworkModel> iter = labelModel.getNetworks().iterator(); iter.hasNext();) {
            LogicalNetworkModel networkModel = iter.next();

            if (nicNetworks != null && nicNetworks.contains(networkModel)) {
                networkModel.attachViaLabel();
            } else {
                // The network has the same label as the nic but not attached to the nic.
                iter.remove();
                errorLabelNetworks.add(networkModel);
            }
        }
    }

    private void initNextBondName(List<String> bondNames) {
        Collections.sort(bondNames, new LexoNumericComparator());
        nextBondName = BusinessEntitiesDefinitions.BOND_NAME_PREFIX + 0;
        for (int i = 0; i < bondNames.size(); ++i) {
            if (nextBondName.equals(bondNames.get(i))) {
                nextBondName = BusinessEntitiesDefinitions.BOND_NAME_PREFIX + (i + 1);
            } else {
                break;
            }
        }
    }

    private Map<String, Set<LogicalNetworkModel>> createNicNameToNetworkModels() {
        Map<String, Set<LogicalNetworkModel>> nicToNetworks = new HashMap<>();

        for (NetworkAttachment networkAttachment : hostSetupNetworksParametersData.getNetworkAttachments()) {
            String nicName = networkAttachment.getNicName();

            if (!nicToNetworks.containsKey(nicName)) {
                nicToNetworks.put(nicName, new HashSet<LogicalNetworkModel>());
            }

            LogicalNetworkModel networkModel = networkModelByName.get(networkAttachment.getNetworkName());

            assert networkModel != null : "network on an attachment should have a logical model"; //$NON-NLS-1$

            setVlanDeviceOnNetworkModelIfNeeded(nicName, networkModel);

            nicToNetworks.get(nicName).add(networkModel);
        }

        createModelsForUnamangedNetworks(nicToNetworks);

        return nicToNetworks;
    }

    public void createModelsForUnamangedNetworks(Map<String, Set<LogicalNetworkModel>> nicToNetworks) {
        for (VdsNetworkInterface nic : allExistingNics) {
            if (shouldCreateUnmanagedNetworkModel(nic)) {
                LogicalNetworkModel networkModel = createUnmanagedNetworkModel(nic.getNetworkName(), nic);

                networkModelByName.put(networkModel.getName(), networkModel);

                String baseNicName = NetworkCommonUtils.stripVlan(nic);
                if (!nicToNetworks.containsKey(baseNicName)) {
                    nicToNetworks.put(baseNicName, new HashSet<LogicalNetworkModel>());
                }
                nicToNetworks.get(baseNicName).add(networkModel);
            }
        }
    }

    private boolean shouldCreateUnmanagedNetworkModel(VdsNetworkInterface nic) {
        return nic.getNetworkImplementationDetails() != null && !nic.getNetworkImplementationDetails().isManaged()
                && !hostSetupNetworksParametersData.getRemovedUnmanagedNetworks().contains(nic.getNetworkName());
    }

    private Map<String, List<VdsNetworkInterface>> getBondNameToSlaves() {
        final Map<String, VdsNetworkInterface> nicMap = Entities.entitiesByName(allExistingNics);
        Map<String, List<VdsNetworkInterface>> bondToSlaves = new HashMap<>();

        for (CreateOrUpdateBond createOrUpdateBond : hostSetupNetworksParametersData.getBonds()) {
            String bondName = createOrUpdateBond.getName();
            assert !bondToSlaves.containsKey(bondName) : "the same bond shouldn't exist twice in the parameters"; //$NON-NLS-1$

            bondToSlaves.put(bondName, new ArrayList<VdsNetworkInterface>());
            Set<String> slavesNames = createOrUpdateBond.getSlaves();
            for (String slaveName : slavesNames) {
                bondToSlaves.get(bondName).add(nicMap.get(slaveName));
            }
        }
        return bondToSlaves;
    }

    private void setVlanDeviceOnNetworkModelIfNeeded(String baseNicName,
            LogicalNetworkModel networkModel) {
        if (networkModel.hasVlan()) {
            VdsNetworkInterface existingVlanDevice = networkModel.getVlanDevice();
            assert existingVlanDevice == null : "should have only one vlan device, but found " + existingVlanDevice; //$NON-NLS-1$
            VdsNetworkInterface vlanDeviceWithTheSameVlanIdAsTheNetwork = getExistingVlanDeviceByVlanId(networkModel.getVlanId());

            if (vlanDeviceWithTheSameVlanIdAsTheNetwork != null
                    && baseNicName.equals(vlanDeviceWithTheSameVlanIdAsTheNetwork.getBaseInterface())) {
                networkModel.setVlanDevice(vlanDeviceWithTheSameVlanIdAsTheNetwork);
            }
        }
    }

    private Map<Integer, VdsNetworkInterface> mapVlanDevicesByVlanId() {
        Map<Integer, VdsNetworkInterface> vlanDevicesByVlanId = new HashMap<>();

        for (VdsNetworkInterface nic : allExistingNics) {
            if (nic.getVlanId() != null) {
                vlanDevicesByVlanId.put(nic.getVlanId(), nic);
            }
        }

        return vlanDevicesByVlanId;
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

    private void setLabelErrorsOnNicModels(Map<String, NetworkInterfaceModel> nicModels,
            List<LogicalNetworkModel> networkModelsWithLabelError,
            Map<String, String> labelToDesiredNicName) {
        for (LogicalNetworkModel networkModel : networkModelsWithLabelError) {
            NetworkInterfaceModel desiredNicModel =
                    nicModels.get(labelToDesiredNicName.get(networkModel.getNetwork().getLabel()));
            NetworkOperation operation = NetworkOperationFactory.operationFor(networkModel, desiredNicModel);
            UIMessages messages = ConstantsManager.getInstance().getMessages();
            // Should be attached but can't due to conflict
            if (desiredNicModel != null) {
                if (operation.isNullOperation()) {
                    networkModel.setErrorMessage(messages.networkLabelConflict(desiredNicModel.getName(),
                            networkModel.getNetwork().getLabel())
                            + " " + operation.getMessage(networkModel, desiredNicModel)); //$NON-NLS-1$
                } else {
                    networkModel.setErrorMessage(messages.labeledNetworkNotAttached(desiredNicModel.getName(),
                            networkModel.getNetwork().getLabel()));
                }
            }
        }
    }

    private void queryLabels() {
        AsyncDataProvider.getInstance().getNetworkLabelsByDataCenterId(getEntity().getStoragePoolId(),
            new AsyncQuery<>(returnValue -> {
                dcLabels = returnValue;
                initLabelModels();

                // chain the networks query
                queryNetworks();
            }));
    }

    private void queryFreeBonds() {
        // query for all unused, existing bonds on the host

        VDS vds = getEntity();
        Frontend.getInstance().runQuery(QueryType.GetVdsFreeBondsByVdsId,
            new IdQueryParameters(vds.getId()),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    allBonds = returnValue.getReturnValue();

                    queryCustomBondNameSupport();
                    queryTLVInformation();
                    queryReportedDnsResolverConfiguration();

                    initNetworkModels();
                    initNicModels();

                    stopProgress();
                }));
    }

    private void queryCustomBondNameSupport() {
        AsyncDataProvider.getInstance().getCustomBondNameSupported(new AsyncQuery<>(returnValue -> {
            customBondNameSupported = returnValue;
        }), getEntity().getClusterCompatibilityVersion());
    }

    private void queryVfMap() {
        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        Frontend.getInstance().runQuery(QueryType.GetVfToPfMapByHostId, params, new AsyncQuery<QueryReturnValue>(returnValue -> {
            vfMap = returnValue.getReturnValue();
            if (vfMap == null) {
                vfMap = Collections.emptyMap();
            }

            // chain the free bonds query
            queryFreeBonds();
        }));
    }

    private void queryInterfaces() {
        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        // query for interfaces
        Frontend.getInstance().runQuery(QueryType.GetVdsInterfacesByVdsId, params, new AsyncQuery<>((QueryReturnValue returnValue) -> {
            allExistingNics = returnValue.getReturnValue();

            existingVlanDevicesByVlanId = mapVlanDevicesByVlanId();
            initCreateOrUpdateBondParameters();
            initNicLabelsParameters();

            // chain the network attachments query
            queryNetworkAttachments();
        }));
    }

    private void queryTLVInformation() {
        Frontend.getInstance()
                .runQuery(QueryType.GetMultipleTlvsByHostId,
                        new IdQueryParameters(getEntity().getId()),
                        new AsyncQuery<QueryReturnValue>(result -> {
                            Map<String, LldpInfo> lldpInfoMap = result.getReturnValue();
                            networkLldpInfoByName = lldpInfoMap == null ? new HashMap<>() : lldpInfoMap;
                            getLldpChangedEvent().raise(this, EventArgs.EMPTY);
                        }, true));
    }

    private void queryReportedDnsResolverConfiguration() {
        Frontend.getInstance()
                .runQuery(QueryType.GetDnsResolverConfigurationById,
                        new IdQueryParameters(getEntity().getId()),
                        new AsyncQuery<QueryReturnValue>(result -> {
                            getEntity().setReportedDnsResolverConfiguration(result.getReturnValue());
                        }, true));
    }

    private void queryNetworkAttachments() {
        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        params.setRefresh(false);
        // query for network attachments
        Frontend.getInstance().runQuery(QueryType.GetNetworkAttachmentsByHostId, params, new AsyncQuery<>((QueryReturnValue returnValue) -> {
            hostSetupNetworksParametersData.getNetworkAttachments().addAll((List<NetworkAttachment>) returnValue.getReturnValue());

            initNetworkIdToExistingAttachmentMap();

            // chain the vfsConfig query
            queryVfsConfig();
        }));
    }

    private void queryVfsConfig() {
        // query for vfsConfigs
        VDS vds = getEntity();
        IdQueryParameters params = new IdQueryParameters(vds.getId());
        Frontend.getInstance().runQuery(QueryType.GetAllVfsConfigByHostId, params, new AsyncQuery<QueryReturnValue>(returnValueObj -> {
            Object returnValue = returnValueObj.getReturnValue();
            List<HostNicVfsConfig> allHostVfs = (List<HostNicVfsConfig>) returnValue;

            for (HostNicVfsConfig vfsConfig : allHostVfs) {
                originalVfsConfigs.add(vfsConfig);
                nicToVfsConfig.put(vfsConfig.getNicId(), new HostNicVfsConfig(vfsConfig));
            }

            queryVfMap();
        }));
    }

    private void queryNetworks() {
        VDS vds = getEntity();

        // query for networks
        AsyncDataProvider.getInstance().getClusterNetworkList(new AsyncQuery<>(returnValue -> {
            allNetworks = returnValue;
            // chain the qoss query
            queryQoss();
        }), vds.getClusterId());
    }

    private void queryQoss() {
        // query for qoss
        AsyncDataProvider.getInstance().getAllHostNetworkQos(getEntity().getStoragePoolId(), new AsyncQuery<>(qoss -> {
            qosById = Entities.businessEntitiesById(qoss);
            // chain the nic query
            queryInterfaces();
        }));
    }

    private void setBondOptions(CreateOrUpdateBond bond, SetupNetworksBondModel bondDialogModel) {
        Map.Entry<String, EntityModel<String>> BondPair = bondDialogModel.getBondingOptions().getSelectedItem();
        String key = BondPair.getKey();
        bond.setBondOptions("custom".equals(key) ? BondPair.getValue().getEntity() : key); //$NON-NLS-1$
    }

    private void setNetworks(Map<String, LogicalNetworkModel> networks) {
        networkModelByName = networks;
    }

    private void setNics(Map<String, NetworkInterfaceModel> nics) {
        nicModelByName = nics;
        onNicsChanged();
        getNicsChangedEvent().raise(this, EventArgs.EMPTY);
    }

    private void setNicsChangedEvent(Event<EventArgs> value) {
        nicsChangedEvent = value;
    }

    private void setOperationCandidateEvent(Event<OperationCandidateEventArgs> event) {
        operationCandidateEvent = event;
    }

    public Event<EventArgs> getLldpChangedEvent() {
        return lldpChangedEvent;
    }

    public void setLldpChangedEvent(Event<EventArgs> lldpChangedEvent) {
        this.lldpChangedEvent = lldpChangedEvent;
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

    public void onSetupNetworks() {
        // Determines the connectivity timeout in seconds
        AsyncDataProvider.getInstance().getNetworkConnectivityCheckTimeoutInSeconds(new AsyncQuery<>(
                returnValue -> {
                    getConnectivityTimeout().setEntity(returnValue);
                    postOnSetupNetworks();
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
        return new UiVdcAction(ActionType.HostSetupNetworks, hostSetupNetworksParameters, this, true) {
            @Override
            protected boolean shouldExecute() {
                return !hostSetupNetworksParameters.isEmptyRequest();
            }
        };
    }

    public UiAction getCommitNetworkChangesAction() {
        return new UiVdcAction(ActionType.CommitNetworkChanges,
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
        return () -> {
            sourceListModel.setWindow(null);
            sourceListModel.search();
        };

    }

    public HostSetupNetworksParameters createHostSetupNetworksParameters() {
        HostSetupNetworksParameters result = new HostSetupNetworksParameters(getEntity().getId());

        for (NetworkAttachment attachment : hostSetupNetworksParametersData.getNetworkAttachments()) {
            attachment.setOverrideConfiguration(shouldSyncNetwork(attachment.getNetworkName()));
        }

        result.setNetworkAttachments(
                new ArrayList<NetworkAttachment>(hostSetupNetworksParametersData.getNetworkAttachments()));
        result.setRemovedNetworkAttachments(hostSetupNetworksParametersData.getRemovedNetworkAttachments());

        result.setCreateOrUpdateBonds(new ArrayList<CreateOrUpdateBond>(hostSetupNetworksParametersData.getBonds()));
        result.setRemovedBonds(new HashSet<>(hostSetupNetworksParametersData.getRemovedBonds()));

        result.setRemovedUnmanagedNetworks(hostSetupNetworksParametersData.getRemovedUnmanagedNetworks());

        result.setLabels(hostSetupNetworksParametersData.getLabels());
        result.setRemovedLabels(hostSetupNetworksParametersData.getRemovedLabels());
        result.setCommitOnSuccess(commitChanges.getEntity());

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

    public DataFromHostSetupNetworksModel getHostSetupNetworksParametersData() {
        return hostSetupNetworksParametersData;
    }

    private void initCreateOrUpdateBondParameters() {
        NetworkCommonUtils.fillBondSlaves(allExistingNics);
        for (VdsNetworkInterface nic : allExistingNics) {
            if (nic.isBond()) {
                getHostSetupNetworksParametersData().getBonds()
                        .add(CreateOrUpdateBond.fromBond((Bond) nic));
            }
        }

        hostSetupNetworksParametersData.setOriginalBondsByName(Entities.entitiesByName(hostSetupNetworksParametersData.getBonds()));
    }

    private void initNicLabelsParameters() {
        for (VdsNetworkInterface nic : allExistingNics) {
            if (nic.getLabels() != null) {
                for (String label : nic.getLabels()) {
                    getHostSetupNetworksParametersData().getLabels()
                            .add(new NicLabel(nic.getId(), nic.getName(), label));
                }
                getHostSetupNetworksParametersData().getOriginalLabels().addAll(nic.getLabels());
            }
        }
    }

    public VdsNetworkInterface getExistingVlanDeviceByVlanId(int vlanId) {
        return existingVlanDevicesByVlanId.get(vlanId);
    }

    private void initNetworkIdToExistingAttachmentMap() {
        Map<Guid, Guid> networkIdToExistingAttachmentId = new HashMap<>();
        for (NetworkAttachment attachment : hostSetupNetworksParametersData.getNetworkAttachments()) {
            networkIdToExistingAttachmentId.put(attachment.getNetworkId(), attachment.getId());
        }

        hostSetupNetworksParametersData.setNetworkIdToExistingAttachmentId(networkIdToExistingAttachmentId);
    }

    public boolean isVfsMapEmpty() {
        return vfMap.isEmpty();
    }

    public HostNicVfsConfig getVfConfig(Guid nicId) {
        return nicToVfsConfig.get(nicId);
    }

    @Override
    public void cleanup() {
        cleanupEvents(getNicsChangedEvent(),
                getOperationCandidateEvent(),
                getLldpChangedEvent());
        super.cleanup();
    }
}
