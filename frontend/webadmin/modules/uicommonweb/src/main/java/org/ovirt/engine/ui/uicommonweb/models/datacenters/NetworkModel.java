package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.OpenstackNetworkProviderProperties;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasValidatedTabs;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.ValidationCompleteEvent;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.qos.NewHostNetworkQosModel;
import org.ovirt.engine.ui.uicommonweb.models.dnsconfiguration.DnsConfigurationModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.PortSecuritySelectorValue;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NetworkProfilesModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.NewVnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ExternalSubnetModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.RegexValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public abstract class NetworkModel extends Model implements HasValidatedTabs {
    private static final String CMD_APPROVE = "OnApprove"; //$NON-NLS-1$
    private static final String CMD_ABORT = "OnAbort"; //$NON-NLS-1$

    public static final HostNetworkQos EMPTY_HOST_NETWORK_QOS = createEmptyHostNetworkQos();

    private static final EventDefinition NAME_WARNING_EVENT =
            new EventDefinition("NameWarningEvent", Boolean.class); //$NON-NLS-1$

    public static HostNetworkQos createEmptyHostNetworkQos() {
        HostNetworkQos qos = new HostNetworkQos();
        qos.setName(ConstantsManager.getInstance().getConstants().unlimitedQoSTitle());
        qos.setId(Guid.Empty);
        return qos;
    }

    private EntityModel<String> privateName;
    private EntityModel<String> privateDescription;
    private EntityModel<Boolean> external;
    private EntityModel<Boolean> connectedToPhysicalNetwork;
    private ListModel<Network> datacenterPhysicalNetwork;
    private EntityModel<Boolean> usePhysicalNetworkFromDatacenter;
    private EntityModel<Boolean> usePhysicalNetworkFromCustom;
    private ListModel<Provider<?>> externalProviders;
    private ListModel<String> networkLabel;
    private EntityModel<String> customPhysicalNetwork;
    private EntityModel<String> privateComment;
    private EntityModel<Integer> privateVLanTag;
    private EntityModel<Boolean> privateIsStpEnabled;
    private EntityModel<Boolean> privateHasVLanTag;
    private ListModel<MtuSelector> mtuSelector;
    private ListModel<PortSecuritySelectorValue> portSecuritySelector;
    private EntityModel<Integer> mtu;
    private EntityModel<Boolean> privateIsVmNetwork;
    private EntityModel<Boolean> portIsolation;
    private ListModel<HostNetworkQos> qos;
    private DnsConfigurationModel dnsConfigurationModel;
    private ListModel<StoragePool> privateDataCenters;
    private NetworkProfilesModel profiles;
    private EntityModel<Boolean> createSubnet;
    private ExternalSubnetModel subnetModel;
    private UICommand addQosCommand;
    private final Network network;
    private final SearchableListModel<?, ? extends Network> sourceListModel;
    private boolean isGeneralTabValid;
    private boolean isVnicProfileTabValid;
    private boolean isSubnetTabValid;
    private Event<EventArgs> nameWarningEvent;

    public NetworkModel(SearchableListModel<?, ? extends Network> sourceListModel) {
        this(new Network(), sourceListModel);
    }

    public NetworkModel(Network network, SearchableListModel<?, ? extends Network> sourceListModel) {
        addCommands();
        this.network = network;
        this.sourceListModel = sourceListModel;
        setName(new EntityModel<String>());
        setDescription(new EntityModel<String>());
        setComment(new EntityModel<String>());
        setDataCenters(new ListModel<StoragePool>());
        getDataCenters().getSelectedItemChangedEvent().addListener((ev, sender, args) -> syncWithBackend());
        setExternal(new EntityModel<>(false));
        getExternal().getEntityChangedEvent().addListener((ev, sender, args) -> onExportChanged());
        setCustomPhysicalNetwork(new EntityModel<String>());

        setDatacenterPhysicalNetwork(new ListModel<>());
        setConnectedToPhysicalNetwork(new EntityModel<>());
        setUsePhysicalNetworkFromDatacenter(new EntityModel<>());
        setUsePhysicalNetworkFromCustom(new EntityModel<>());

        getUsePhysicalNetworkFromCustom().getEntityChangedEvent()
                .addListener((ev, sender, args) -> onPhysicalNetworkSourceChange(sender));
        getUsePhysicalNetworkFromDatacenter().getEntityChangedEvent()
                .addListener((ev, sender, args) -> onPhysicalNetworkSourceChange(sender));
        getConnectedToPhysicalNetwork().getEntityChangedEvent()
                .addListener((ev, sender, args) -> onConnectedToPhysicalNetworkChange());


        setNetworkLabel(new ListModel<String>());
        setExternalProviders(new ListModel<Provider<?>>());
        initExternalProviderList();

        EntityModel<Boolean> stpEnabled = new EntityModel<>();
        stpEnabled.setEntity(false);
        setIsStpEnabled(stpEnabled);

        setVLanTag(new EntityModel<Integer>());
        EntityModel<Boolean> hasVlanTag = new EntityModel<>();
        hasVlanTag.setEntity(false);
        setHasVLanTag(hasVlanTag);
        getHasVLanTag().getEntityChangedEvent().addListener((ev, sender, args) -> updateVlanTagChangeability());

        getUsePhysicalNetworkFromDatacenter().setEntity(true);

        ListModel<MtuSelector> mtuSelector = new ListModel<>();
        mtuSelector.setItems(Arrays.asList(MtuSelector.values()));
        setMtuSelector(mtuSelector);
        mtuSelector.getSelectedItemChangedEvent().addListener((ev, sender, args) -> updateMtuSelectorsChangeability());

        setMtu(new EntityModel<Integer>());

        portSecuritySelector = new ListModel<>();
        portSecuritySelector.setItems(Arrays.asList(PortSecuritySelectorValue.values()));

        EntityModel<Boolean> isVmNetwork = new EntityModel<>();
        isVmNetwork.setEntity(true);
        setIsVmNetwork(isVmNetwork);
        isVmNetwork.getEntityChangedEvent().addListener((ev, sender, args) -> toggleProfilesAvailability());

        setPortIsolation(new EntityModel<>(false));

        setProfiles(new NetworkProfilesModel());
        List<VnicProfileModel> profiles = new LinkedList<>();
        profiles.add(createDefaultProfile());
        getProfiles().setItems(profiles);

        setQos(new ListModel<HostNetworkQos>());

        DnsResolverConfiguration dnsResolverConfiguration = getNetwork().getDnsResolverConfiguration();
        if (dnsResolverConfiguration == null) {
            dnsResolverConfiguration = new DnsResolverConfiguration();
            dnsResolverConfiguration.setNameServers(new ArrayList<NameServer>());
        }

        DnsConfigurationModel dnsConfigurationModel = new DnsConfigurationModel();
        dnsConfigurationModel.setEntity(dnsResolverConfiguration);
        setDnsConfigurationModel(dnsConfigurationModel);

        EntityModel<Boolean> createSubnet = new EntityModel<>(false);
        setCreateSubnet(createSubnet);
        createSubnet.getEntityChangedEvent().addListener((ev, sender, args) -> updateSubnetChangeability());

        setSubnetModel(new ExternalSubnetModel());

        // Update changeability according to initial values
        updateVlanTagChangeability();
        updateSubnetChangeability();

        setIsGeneralTabValid(true);
        setIsVnicProfileTabValid(true);
        updateAvailability();

        setNameWarningEvent(new Event<>(NAME_WARNING_EVENT));
        getName().getEntityChangedEvent().addListener((ev, sender, args) -> checkNameForWarningEvent());
    }

    private void initPhysicalNetworkList() {
        Frontend.getInstance()
                .runQuery(QueryType.GetAllNetworks,
                        new IdQueryParameters(getSelectedDc().getId()),
                        new AsyncQuery<QueryReturnValue>(result -> {
                            List<Network> networks = result.getReturnValue();
                            if (networks != null) {
                                getDatacenterPhysicalNetwork().setItems(networks.stream()
                                        .filter(network -> !network.isExternal())
                                        .collect(Collectors.toList()));
                                selectPhysicalDatacenterNetwork();
                            }
                        }));
    }

    private void updateAvailability() {
        if (!ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            getExternalProviders().setIsAvailable(false);
            getCustomPhysicalNetwork().setIsAvailable(false);
            getCreateSubnet().setIsAvailable(false);
            getVLanTag().setIsAvailable(false);
            getHasVLanTag().setIsAvailable(false);
            getExternal().setIsAvailable(false);
        }
    }

    private VnicProfileModel createDefaultProfile() {
        final VnicProfileModel defaultProfile = new NewVnicProfileModel();

        // make sure default profile's name is in sync with network's name
        defaultProfile.getName().setEntity(getName().getEntity());
        final IEventListener<EventArgs> networkNameListener =
                (ev, sender, args) -> defaultProfile.getName().setEntity(getName().getEntity());
        getName().getEntityChangedEvent().addListener(networkNameListener);

        // if user overrides default name, stop tracking network's name
        defaultProfile.getName().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                String defaultProfileName = defaultProfile.getName().getEntity();
                if ((defaultProfileName != null) && (!defaultProfileName.equals(getName().getEntity()))) {
                    getName().getEntityChangedEvent().removeListener(networkNameListener);
                    defaultProfile.getName().getEntityChangedEvent().removeListener(this);
                }
            }
        });

        return defaultProfile;
    }

    private void initExternalProviderList() {
        AsyncQuery<List<Provider<?>>> getProvidersQuery = new AsyncQuery<>(result -> {
            List<Provider<?>> providers = getNonReadOnlyExternalNetworkProviders(result);
            getExternalProviders().setItems(providers);
            selectExternalProvider();
        });
        AsyncDataProvider.getInstance().getAllNetworkProviders(getProvidersQuery);
    }

    private List<Provider<?>> getNonReadOnlyExternalNetworkProviders(List<Provider<?>> result) {
        List<Provider<?>> providers = new LinkedList<>();
        for (Provider<?> provider : result){
            if (isExternalNetworkProviderReadOnly(provider)){
                continue;
            }
            providers.add(provider);
        }
        return providers;
    }

    private boolean isExternalNetworkProviderReadOnly(Provider<?> provider) {
        OpenstackNetworkProviderProperties properties =
                (OpenstackNetworkProviderProperties) provider.getAdditionalProperties();
        if (properties.getReadOnly()){
            return true;
        }

        return false;
    }

    public EntityModel<String> getName() {
        return privateName;
    }

    private void setName(EntityModel<String> value) {
        privateName = value;
    }

    public EntityModel<String> getDescription() {
        return privateDescription;
    }

    private void setDescription(EntityModel<String> value) {
        privateDescription = value;
    }

    public EntityModel<Boolean> getExternal() {
        return external;
    }

    private void setExternal(EntityModel<Boolean> value) {
        external = value;
    }

    public ListModel<Provider<?>> getExternalProviders() {
        return externalProviders;
    }

    public void setExternalProviders(ListModel<Provider<?>> externalProviders) {
        this.externalProviders = externalProviders;
    }

    public ListModel<String> getNetworkLabel() {
        return networkLabel;
    }

    public void setNetworkLabel(ListModel<String> networkLabel) {
        this.networkLabel = networkLabel;
    }

    public EntityModel<String> getCustomPhysicalNetwork() {
        return customPhysicalNetwork;
    }

    private void setCustomPhysicalNetwork(EntityModel<String> customPhysicalNetwork) {
        this.customPhysicalNetwork = customPhysicalNetwork;
    }

    public EntityModel<String> getComment() {
        return privateComment;
    }

    private void setComment(EntityModel<String> value) {
        privateComment = value;
    }

    public EntityModel<Integer> getVLanTag() {
        return privateVLanTag;
    }

    private void setVLanTag(EntityModel<Integer> value) {
        privateVLanTag = value;
    }

    public EntityModel<Boolean> getIsStpEnabled() {
        return privateIsStpEnabled;
    }

    private void setIsStpEnabled(EntityModel<Boolean> value) {
        privateIsStpEnabled = value;
    }

    public EntityModel<Boolean> getHasVLanTag() {
        return privateHasVLanTag;
    }

    private void setHasVLanTag(EntityModel<Boolean> value) {
        privateHasVLanTag = value;
    }

    public ListModel<MtuSelector> getMtuSelector() {
        return mtuSelector;
    }

    public ListModel<PortSecuritySelectorValue> getPortSecuritySelector() {
        return portSecuritySelector;
    }

    private void setMtuSelector(ListModel<MtuSelector> value) {
        mtuSelector = value;
    }

    public EntityModel<Integer> getMtu() {
        return mtu;
    }

    private void setMtu(EntityModel<Integer> value) {
        mtu = value;
    }

    public EntityModel<Boolean> getIsVmNetwork() {
        return privateIsVmNetwork;
    }

    public void setIsVmNetwork(EntityModel<Boolean> value) {
        privateIsVmNetwork = value;
    }

    public EntityModel<Boolean> getPortIsolation() {
        return portIsolation;
    }

    public void setPortIsolation(EntityModel<Boolean> portIsolation) {
        this.portIsolation = portIsolation;
    }

    public ListModel<HostNetworkQos> getQos() {
        return qos;
    }

    private void setQos(ListModel<HostNetworkQos> qos) {
        this.qos = qos;
    }

    public DnsConfigurationModel getDnsConfigurationModel() {
        return dnsConfigurationModel;
    }

    private void setDnsConfigurationModel(DnsConfigurationModel dnsConfigurationModel) {
        this.dnsConfigurationModel = dnsConfigurationModel;
    }

    public ListModel<StoragePool> getDataCenters() {
        return privateDataCenters;
    }

    private void setDataCenters(ListModel<StoragePool> value) {
        privateDataCenters = value;
    }

    public NetworkProfilesModel getProfiles() {
        return profiles;
    }

    private void setProfiles(NetworkProfilesModel value) {
        profiles = value;
    }

    public Network getNetwork() {
        return network;
    }

    public SearchableListModel<?, ?> getSourceListModel() {
        return sourceListModel;
    }

    public EntityModel<Boolean> getCreateSubnet() {
        return createSubnet;
    }

    private void setCreateSubnet(EntityModel<Boolean> createSubnet) {
        this.createSubnet = createSubnet;
    }

    public ExternalSubnetModel getSubnetModel() {
        return subnetModel;
    }

    private void setSubnetModel(ExternalSubnetModel subnetModel) {
        this.subnetModel = subnetModel;
    }

    public UICommand getAddQosCommand() {
        return addQosCommand;
    }

    public boolean getIsGeneralTabValid() {
        return isGeneralTabValid;
    }

    public void setIsGeneralTabValid(boolean value) {
        if (isGeneralTabValid != value) {
            isGeneralTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsGeneralTabValid")); //$NON-NLS-1$
        }
    }

    public boolean getIsVnicProfileTabValid() {
        return isVnicProfileTabValid;
    }

    public void setIsVnicProfileTabValid(boolean value) {
        if (isVnicProfileTabValid != value) {
            isVnicProfileTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsVnicProfileTabValid")); //$NON-NLS-1$
        }
    }

    public boolean getIsSubnetTabValid() {
        return isSubnetTabValid;
    }

    public void setIsSubnetTabValid(boolean value) {
        if (isSubnetTabValid != value) {
            isSubnetTabValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSubnetTabValid")); //$NON-NLS-1$
        }
    }

    public void setConnectedToPhysicalNetwork(EntityModel<Boolean> connectedToPhysicalNetwork) {
        this.connectedToPhysicalNetwork = connectedToPhysicalNetwork;
    }

    public EntityModel<Boolean> getConnectedToPhysicalNetwork() {
        return connectedToPhysicalNetwork;
    }

    public ListModel<Network> getDatacenterPhysicalNetwork() {
        return datacenterPhysicalNetwork;
    }

    public void setDatacenterPhysicalNetwork(ListModel<Network> datacenterPhysicalNetwork) {
        this.datacenterPhysicalNetwork = datacenterPhysicalNetwork;
    }

    public EntityModel<Boolean> getUsePhysicalNetworkFromDatacenter() {
        return usePhysicalNetworkFromDatacenter;
    }

    public void setUsePhysicalNetworkFromDatacenter(EntityModel<Boolean> usePhysicalNetworkFromDatacenter) {
        this.usePhysicalNetworkFromDatacenter = usePhysicalNetworkFromDatacenter;
    }

    public EntityModel<Boolean> getUsePhysicalNetworkFromCustom() {
        return usePhysicalNetworkFromCustom;
    }

    public void setUsePhysicalNetworkFromCustom(EntityModel<Boolean> usePhysicalNetworkFromCustom) {
        this.usePhysicalNetworkFromCustom = usePhysicalNetworkFromCustom;
    }

    private void onPhysicalNetworkSourceChange(Object sender) {
        boolean datacenter = true;
        if (sender == getUsePhysicalNetworkFromDatacenter()) {
            getCustomPhysicalNetwork().setIsChangeable(false);
            getDatacenterPhysicalNetwork().setIsChangeable(true);
            datacenter = true;
        } else if (sender == getUsePhysicalNetworkFromCustom()) {
            getCustomPhysicalNetwork().setIsChangeable(true);
            getDatacenterPhysicalNetwork().setIsChangeable(false);
            datacenter = false;
        }
        getUsePhysicalNetworkFromCustom().setEntity(!datacenter, false);
        getUsePhysicalNetworkFromDatacenter().setEntity(datacenter, false);
        updateVlanChangeabilityAndValue();
    }

    private void updateVlanChangeabilityAndValue() {
        boolean changeable = !getExternal().getEntity() || (getConnectedToPhysicalNetwork().getEntity()
                && getUsePhysicalNetworkFromCustom().getEntity());

        getHasVLanTag().setEntity(changeable && getHasVLanTag().getEntity());
        getHasVLanTag().setIsChangeable(changeable);
        getVLanTag().setEntity(changeable ? getVLanTag().getEntity() : null);
    }

    private void onConnectedToPhysicalNetworkChange() {
        boolean visible = getConnectedToPhysicalNetwork().getEntity();
        getUsePhysicalNetworkFromDatacenter().setIsAvailable(visible);
        getUsePhysicalNetworkFromCustom().setIsAvailable(visible);
        getCustomPhysicalNetwork().setIsAvailable(visible);
        getDatacenterPhysicalNetwork().setIsAvailable(visible);
    }

    private boolean validate() {
        LengthValidation tempVar = new LengthValidation();
        tempVar.setMaxLength(BusinessEntitiesDefinitions.NETWORK_NAME_SIZE);
        RegexValidation tempVar2 = new RegexValidation();
        tempVar2.setIsNegate(true);
        tempVar2.setExpression("^(bond)"); //$NON-NLS-1$
        tempVar2.setMessage(ConstantsManager.getInstance().getConstants().networkNameStartMsg());
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar, tempVar2 });

        LengthValidation tempVar3 = new LengthValidation();
        tempVar3.setMaxLength(40);
        getDescription().validateEntity(new IValidation[] { tempVar3 });

        getComment().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        getVLanTag().setIsValid(true);
        if (getHasVLanTag().getEntity()) {
            IntegerValidation tempVar4 = new IntegerValidation();
            tempVar4.setMinimum(0);
            tempVar4.setMaximum(4094);
            getVLanTag().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4 });
        }

        IntegerValidation tempVar5 = new IntegerValidation();
        tempVar5.setMinimum(68);
        getMtu().validateEntity(new IValidation[] { new NotEmptyValidation(), tempVar5 });

        getDnsConfigurationModel().validate();

        getExternalProviders().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        boolean subnetValid = true;
        if (getExternal().getEntity() && getCreateSubnet().getEntity()) {
            subnetValid = getSubnetModel().validate();
        }

        boolean profilesValid = true;
        Iterable<VnicProfileModel> profiles = getProfiles().getItems();
        for (VnicProfileModel profileModel : profiles) {
            if (!profileModel.validate()) {
                profilesValid = false;
            }
        }

        getNetworkLabel().validateSelectedItem(new IValidation[] { new AsciiNameValidation() });

        setValidTab(TabName.GENERAL_TAB, isGeneralTabValid());
        setValidTab(TabName.SUBNET_TAB, subnetValid);
        setValidTab(TabName.PROFILES_TAB, profilesValid);

        ValidationCompleteEvent.fire(getEventBus(), this);
        return allTabsValid();
    }

    private boolean isGeneralTabValid() {
        return getName().getIsValid()
                && getVLanTag().getIsValid()
                && getDescription().getIsValid()
                && getMtu().getIsValid()
                && getExternalProviders().getIsValid()
                && getComment().getIsValid()
                && getNetworkLabel().getIsValid()
                && getDnsConfigurationModel().getIsValid();
    }

    protected boolean isCustomMtu() {
        return MtuSelector.customMtu == getMtuSelector().getSelectedItem();
    }

    public void syncWithBackend() {
        final StoragePool dc = getSelectedDc();
        if (dc == null) {
            return;
        }

        AsyncDataProvider.getInstance().getAllHostNetworkQos(dc.getId(), new AsyncQuery<>(qos -> {
            getQos().setItems(qos);
            getQos().setSelectedItem(qos.stream()
                            .filter(new Linq.IdPredicate<>(getNetwork().getQosId()))
                            .findFirst()
                            .orElse(EMPTY_HOST_NETWORK_QOS));
        }));

        updateDcLabels();

        onExportChanged();
        getProfiles().updateDcId(dc.getId());

        initPhysicalNetworkList();
    }

    private void addCommands() {
        UICommand tempVar2 = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(tempVar2);
        UICommand tempVar3 = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(tempVar3);
        addQosCommand = new UICommand("AddQos", this); //$NON-NLS-1$
        addQosCommand.setTitle(ConstantsManager.getInstance().getConstants().newNetworkQosButton());
    }

    public StoragePool getSelectedDc() {
        return getDataCenters().getSelectedItem();
    }

    public void flush() {
        network.setDataCenterId(getSelectedDc().getId());
        network.setName(getName().getEntity());
        network.setStp(getIsStpEnabled().getEntity());
        network.setDescription(getDescription().getEntity());
        network.setComment(getComment().getEntity());
        network.setVmNetwork(getIsVmNetwork().getEntity());

        String label = getNetworkLabel().getSelectedItem();
        network.setLabel(StringHelper.isNotNullOrEmpty(label) ? label : null);

        network.setDefaultMtu();
        if (getMtu().getIsChangable()) {
            network.setMtu(Integer.parseInt(getMtu().getEntity().toString()));
        }

        network.setDnsResolverConfiguration(getDnsConfigurationModel().flush());

        network.setVlanId(null);
        if (getHasVLanTag().getEntity() && !getExternal().getEntity()) {
            network.setVlanId(Integer.parseInt(getVLanTag().getEntity().toString()));
        }

        for (VnicProfileModel profileModel : getProfiles().getItems()) {
            profileModel.flush();
        }

        if (getQos().getIsChangable()) {
            HostNetworkQos qos = getQos().getSelectedItem();
            network.setQosId(qos == EMPTY_HOST_NETWORK_QOS ? null : qos.getId());
        }

        network.setPortIsolation(getPortIsolation().getEntity());
    }

    protected abstract void executeSave();

    protected void postSaveAction(Guid networkGuid, boolean succeeded) {
        if (succeeded) {
            performProfilesActions(networkGuid);
            stopProgress();
            cancel();
        }
    }

    private void performProfilesActions(Guid networkGuid) {
        List<VnicProfileModel> profileModels = (List<VnicProfileModel>) getProfiles().getItems();
        if (profileModels.isEmpty() || !getProfiles().getIsAvailable()) {
            return;
        }

        networkGuid = networkGuid == null ? getNetwork().getId() : networkGuid;
        ArrayList<ActionParametersBase> paramlist = new ArrayList<>();
        for (VnicProfileModel profileModel : profileModels) {
            if (!StringHelper.isNullOrEmpty(profileModel.getProfile().getName())) {
                VnicProfile vnicProfile = profileModel.getProfile();
                vnicProfile.setNetworkId(networkGuid);
                AddVnicProfileParameters parameters =
                        new AddVnicProfileParameters(vnicProfile, !getNetwork().isExternal());
                parameters.setPublicUse(profileModel.getPublicUse().getEntity());
                paramlist.add(parameters);
            }
        }
        Frontend.getInstance().runMultipleActions(ActionType.AddVnicProfile,
                paramlist,
                (IFrontendActionAsyncCallback) null); // cast is required to avoid overload ambiguity
    }

    protected void toggleProfilesAvailability() {
        getProfiles().setIsAvailable(getIsVmNetwork().getEntity());
    }

    void cancel() {
        sourceListModel.setWindow(null);
        sourceListModel.setConfirmWindow(null);
    }

    private void addQos() {
        NewHostNetworkQosModel qosModel = new NewHostNetworkQosModel(this, getSelectedDc()) {

            @Override
            protected void postSaveAction(boolean succeeded) {
                if (succeeded) {
                    List<HostNetworkQos> qosItems =
                            new ArrayList<>(NetworkModel.this.getQos().getItems());
                    qosItems.add(1, getQos());
                    NetworkModel.this.getQos().setItems(qosItems);
                    NetworkModel.this.getQos().setSelectedItem(getQos());
                }
                super.postSaveAction(succeeded);
            }

            @Override
            protected void cancel() {
                sourceListModel.setConfirmWindow(null);
            }
        };
        qosModel.getDataCenters().setIsChangeable(false);
        sourceListModel.setConfirmWindow(qosModel);
    }

    public void onSave() {
        if (!validate()) {
            return;
        }

        if (isManagement()) {
            ConfirmationModel confirmationModel = new ConfirmationModel();
            confirmationModel.setMessage(ConstantsManager.getInstance().getConstants().updateManagementNetworkWarning());
            UICommand cmdOk = UICommand.createDefaultOkUiCommand(CMD_APPROVE, this);
            confirmationModel.getCommands().add(cmdOk);
            UICommand cmdCancel = UICommand.createCancelUiCommand(CMD_ABORT, this); //$NON-NLS-1$
            confirmationModel.getCommands().add(cmdCancel);
            sourceListModel.setConfirmWindow(confirmationModel);
        } else {
            onApprove();
        }
    }

    private void onApprove() {
        // Save changes.
        flush();

        // Execute all the required commands (detach, attach, update) to save the updates
        executeSave();
    }

    private void onAbort() {
        sourceListModel.setConfirmWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        } else if (command == getAddQosCommand()) {
            addQos();
        } else if (CMD_APPROVE.equals(command.getName())) {
            onAbort();
            onApprove();
        } else if (CMD_ABORT.equals(command.getName())) {
            onAbort();
        }
    }

    protected abstract boolean isManagement();

    protected abstract void initMtu();

    protected abstract void initIsVm();

    protected abstract void initEnablePortSecurity();

    protected abstract void selectExternalProvider();

    protected abstract void selectPhysicalDatacenterNetwork();

    protected void onExportChanged() {
        boolean externalNetwork = getExternal().getEntity();

        getNetworkLabel().setIsChangeable(!externalNetwork);
        getExternalProviders().setIsChangeable(externalNetwork);
        getCustomPhysicalNetwork().setIsChangeable(
                externalNetwork && !getUsePhysicalNetworkFromDatacenter().getEntity());
        getDatacenterPhysicalNetwork().setIsChangeable(
                externalNetwork && getUsePhysicalNetworkFromDatacenter().getEntity());
        getQos().setIsChangeable(!externalNetwork);
        getAddQosCommand().setIsExecutionAllowed(!externalNetwork);

        getConnectedToPhysicalNetwork().setIsChangeable(externalNetwork);
        getUsePhysicalNetworkFromCustom().setIsChangeable(externalNetwork);
        getUsePhysicalNetworkFromDatacenter().setIsChangeable(externalNetwork);
        getPortSecuritySelector().setIsChangeable(externalNetwork);
        updateDnsChangeabilityAndValue();
        updateMtuSelectorsChangeability();
        updateVlanChangeabilityAndValue();
    }

    protected void updateDnsChangeabilityAndValue() {
        boolean externalNetwork = getExternal().getEntity();
        boolean previous = getDnsConfigurationModel().getShouldSetDnsConfiguration().getEntity();
        getDnsConfigurationModel().getShouldSetDnsConfiguration().setEntity(!externalNetwork && previous);
        getDnsConfigurationModel().getShouldSetDnsConfiguration().setIsChangeable(!externalNetwork);
    }

    private void updateDcLabels() {
        AsyncDataProvider.getInstance().getNetworkLabelsByDataCenterId(getSelectedDc().getId(),
                new AsyncQuery<>(returnValue -> {
                    String label = getNetworkLabel().getSelectedItem();
                    getNetworkLabel().setItems(returnValue);
                    getNetworkLabel().setSelectedItem(label);
                    onExportChanged();
                }));
    }

    private void updateVlanTagChangeability() {
        getVLanTag().setIsChangeable(getHasVLanTag().getEntity());
    }

    protected void setMtuSelectorsChangeability(boolean isChangeable, String prohibitionReason) {
        getMtuSelector().setIsChangeable(isChangeable, prohibitionReason);
        getMtu().setIsChangeable(isChangeable && isCustomMtu(), prohibitionReason);
    }

    protected abstract void updateMtuSelectorsChangeability();

    private void updateSubnetChangeability() {
        getSubnetModel().toggleChangeability(getCreateSubnet().getEntity());
    }

    public Event<EventArgs> getNameWarningEvent() {
        return nameWarningEvent;
    }

    public void setNameWarningEvent(Event<EventArgs> nameWarningEvent) {
        this.nameWarningEvent = nameWarningEvent;
    }

    private void checkNameForWarningEvent() {
        if (getName().getEntity().matches(ValidationUtils.HOST_NIC_NAME_PATTERN)) {
            getNameWarningEvent().raise(Boolean.FALSE, EventArgs.EMPTY);
        } else {
            getNameWarningEvent().raise(Boolean.TRUE, EventArgs.EMPTY);
        }
    }

    @Override
    public void cleanup() {
        cleanupEvents(getNameWarningEvent());
        super.cleanup();
    }

    public enum MtuSelector {
        defaultMtu(ConstantsManager.getInstance()
                .getMessages()
                .defaultMtu((Integer) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.DefaultMTU))),
        customMtu(ConstantsManager.getInstance().getConstants().customMtu());

        private String description;

        private MtuSelector(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
