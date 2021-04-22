package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VersionQueryParameters;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetDeviceCustomPropertiesParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkQoSModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class VnicProfileModel extends Model {


    private EntityModel<String> name;
    private EntityModel<Boolean> portMirroring;
    private EntityModel<Boolean> passthrough;
    private EntityModel<Boolean> migratable;
    private KeyValueModel customPropertySheet;
    private EntityModel<Boolean> publicUse;
    private EntityModel<String> description;
    private final SearchableListModel<?, ?> sourceModel;
    private ListModel<Network> network;
    private ListModel<StoragePool> dataCenters;
    private ListModel<NetworkQoS> networkQoS;
    private ListModel<NetworkFilter> networkFilter;
    private ListModel<VnicProfile> failoverVnicProfile;
    private VnicProfile vnicProfile = null;
    private final boolean customPropertiesVisible;
    private final Guid defaultQosId;
    private NetworkQoS defaultQos;
    private AtomicInteger asyncProgressCounter;
    protected UIConstants constants = ConstantsManager.getInstance().getConstants();

    private static final NetworkFilter EMPTY_FILTER = new NetworkFilter();
    protected static final VnicProfile EMPTY_FAILOVER_VNIC_PROFILE = new VnicProfile();

    public EntityModel<String> getName() {
        return name;
    }

    private void setName(EntityModel<String> value) {
        name = value;
    }

    public EntityModel<Boolean> getPortMirroring() {
        return portMirroring;
    }

    public void setPortMirroring(EntityModel<Boolean> value) {
        portMirroring = value;
    }

    public EntityModel<Boolean> getPassthrough() {
        return passthrough;
    }

    public void setPassthrough(EntityModel<Boolean> value) {
        passthrough = value;
    }

    public EntityModel<Boolean> getMigratable() {
        return migratable;
    }

    public void setMigratable(EntityModel<Boolean> migratable) {
        this.migratable = migratable;
    }

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(KeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    public EntityModel<Boolean> getPublicUse() {
        return publicUse;
    }

    public void setPublicUse(EntityModel<Boolean> publicUse) {
        this.publicUse = publicUse;
    }

    public EntityModel<String> getDescription() {
        return description;
    }

    public void setDescription(EntityModel<String> description) {
        this.description = description;
    }

    public ListModel<Network> getNetwork() {
        return network;
    }

    public void setNetwork(ListModel<Network> network) {
        this.network = network;
    }

    public void setProfile(VnicProfile vnicProfile) {
        this.vnicProfile = vnicProfile;
    }

    public VnicProfile getProfile() {
        return vnicProfile;
    }

    public ListModel<StoragePool> getDataCenters() {
        return dataCenters;
    }

    public void setDataCenters(ListModel<StoragePool> dataCenters) {
        this.dataCenters = dataCenters;
    }

    public ListModel<NetworkQoS> getNetworkQoS() {
        return networkQoS;
    }

    public void setNetworkQoS(ListModel<NetworkQoS> networkQoS) {
        this.networkQoS = networkQoS;
    }

    public ListModel<NetworkFilter> getNetworkFilter() {
        return networkFilter;
    }

    public void setNetworkFilter(ListModel<NetworkFilter> networkFilter) {
        this.networkFilter = networkFilter;
    }

    public VnicProfileModel(SearchableListModel<?, ?> sourceModel,
            boolean customPropertiesVisible,
            Guid dcId,
            Guid defaultQosId) {
        this.sourceModel = sourceModel;
        this.customPropertiesVisible = customPropertiesVisible;
        this.defaultQosId = defaultQosId;
        this.asyncProgressCounter = new AtomicInteger(0);

        setName(new EntityModel<String>());
        setNetwork(new ListModel<Network>());
        setNetworkQoS(new ListModel<NetworkQoS>());
        setDataCenters(new ListModel<StoragePool>());
        setNetworkFilter(new ListModel<NetworkFilter>());
        setPortMirroring(new EntityModel<Boolean>());
        setPassthrough(new EntityModel<Boolean>());
        setMigratable(new EntityModel<Boolean>());
        setCustomPropertySheet(new KeyValueModel());
        setFailoverVnicProfile(new ListModel<VnicProfile>());
        EntityModel<Boolean> publicUse = new EntityModel<>();
        publicUse.setEntity(true);
        setPublicUse(publicUse);
        setDescription(new EntityModel<String>());

        getNetwork().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            Network network = getNetwork().getSelectedItem();
            boolean passthroughAndPortMirroringAllowed = network == null || !network.isExternal();

            getPortMirroring().setIsChangeable(passthroughAndPortMirroringAllowed,
                    constants.portMirroringNotSupportedExternalNetworks());
            getPassthrough().setIsChangeable(passthroughAndPortMirroringAllowed,
                    constants.passthroughNotSupportedExternalNetworks());

            if(passthroughAndPortMirroringAllowed) {
                updateChangeabilityIfVmsUsingTheProfile();
            }
        });


        initPassthroughChangeListener();
        initMigratableChangeListener();

        getPassthrough().setEntity(false);
        getMigratable().setEntity(false);

        populateDataCenters(dcId);
        getDataCenters().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            if (getDataCenters().getSelectedItem() != null) {
                Version dcCompatibilityVersion = getDataCenters().getSelectedItem().getCompatibilityVersion();
                Guid currentDcId = getDataCenters().getSelectedItem().getId();
                initCustomPropertySheet(dcCompatibilityVersion);
                initNetworkQoSList(currentDcId);
                initNetworkFilterList(dcCompatibilityVersion);
                initNetworkList(currentDcId);
            }
        });
        initCommands();
    }

    public ListModel<VnicProfile> getFailoverVnicProfile() {
        return failoverVnicProfile;
    }

    public void setFailoverVnicProfile(ListModel<VnicProfile> failoverVnicProfile) {
        this.failoverVnicProfile = failoverVnicProfile;
    }

    private void populateDataCenters(Guid dcId) {
        addAsyncOperationProgress();
        if (dcId == null) {
            SearchParameters tempVar = new SearchParameters("DataCenter:", SearchType.StoragePool); // $NON-NLS-1$
            Frontend.getInstance().runQuery(QueryType.Search, tempVar, new AsyncQuery<QueryReturnValue>(
                    returnValue -> {
                        getDataCenters().setItems(returnValue.getReturnValue());
                        removeAsyncOperationProgress();
                    }));
        } else {
            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery<StoragePool>(
                    returnValue -> {
                        getDataCenters().setItems(Arrays.asList(returnValue));
                        removeAsyncOperationProgress();
                    }), dcId);
            getDataCenters().setIsChangeable(false);
        }
    }

    private void initNetworkList(Guid dataCenterId) {
        addAsyncOperationProgress();
        AsyncDataProvider.getInstance().getNetworkList(new AsyncQuery<>(returnValue -> {
            Collection<Network> networks =
                    returnValue.stream().filter(Network::isVmNetwork).collect(Collectors.toList());

            getNetwork().setItems(networks);

            updateNetworks(networks);

            removeAsyncOperationProgress();
            initFailoverVnicProfiles(dataCenterId);
        }), dataCenterId);
    }

    protected void updateNetworks(Collection<Network> networks) {
        VnicProfile profile = getProfile();
        if (profile != null && profile.getNetworkId() != null) {
            Network selected = networks.stream()
                    .filter(net -> Objects.equals(profile.getNetworkId(), net.getId()))
                    .findFirst().get();
            getNetwork().setSelectedItem(selected);
            getNetwork().setIsChangeable(false);
        }
    }

    private void addAsyncOperationProgress() {
        if (this.asyncProgressCounter.getAndIncrement() == 0) {
            startProgress();
        }
    }

    private void removeAsyncOperationProgress() {
        if (this.asyncProgressCounter.decrementAndGet() == 0) {
            stopProgress();
        }
    }

    protected void initCommands() {
        UICommand okCommand = UICommand.createDefaultOkUiCommand("OnSave", this); //$NON-NLS-1$
        getCommands().add(okCommand);
        UICommand cancelCommand = UICommand.createCancelUiCommand("Cancel", this); //$NON-NLS-1$
        getCommands().add(cancelCommand);
    }

    private void onSave() {
        if (getProgress() != null) {
            return;
        }

        if (!validate()) {
            return;
        }

        // Save changes.
        flush();

        startProgress();

        Frontend.getInstance().runAction(getActionType(),
                getActionParameters(),
                result -> {
                    ActionReturnValue returnValue = result.getReturnValue();
                    stopProgress();

                    if (returnValue != null && returnValue.getSucceeded()) {
                        cancel();
                    }
                },
                this);
    }

    public void flush() {
        if (vnicProfile == null) {
            vnicProfile = new VnicProfile();
        }
        vnicProfile.setName(getName().getEntity());
        Network network = getNetwork().getSelectedItem();
        vnicProfile.setNetworkId(network != null ? network.getId() : null);
        NetworkQoS networkQoS = getNetworkQoS().getSelectedItem();
        vnicProfile.setNetworkQosId(networkQoS != null
                && networkQoS.getId() != null
                && !networkQoS.getId().equals(Guid.Empty)
                ? networkQoS.getId() : null);
        NetworkFilter networkFilter = getNetworkFilter().getSelectedItem();
        vnicProfile.setNetworkFilterId(networkFilter != null
                ? networkFilter.getId() : null);
        VnicProfile failoverVnicProfile = getFailoverVnicProfile().getSelectedItem();
        vnicProfile.setFailoverVnicProfileId(failoverVnicProfile != null ? failoverVnicProfile.getId() : null);
        vnicProfile.setPortMirroring(getPortMirroring().getEntity());
        vnicProfile.setPassthrough(getPassthrough().getEntity());
        if (vnicProfile.isPassthrough()) {
            vnicProfile.setMigratable(getMigratable().getEntity());
        }

        if (customPropertiesVisible) {
            vnicProfile.setCustomProperties(KeyValueModel.convertProperties(getCustomPropertySheet().serialize()));
        } else {
            vnicProfile.setCustomProperties(null);
        }

        vnicProfile.setDescription(getDescription().getEntity());
    }

    private void cancel() {
        sourceModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        } else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
            cancel();
        }
    }

    private void initCustomPropertySheet(Version dcCompatibilityVersion) {
        if (!customPropertiesVisible) {
            return;
        }

        GetDeviceCustomPropertiesParameters params = new GetDeviceCustomPropertiesParameters();
        params.setVersion(dcCompatibilityVersion);
        params.setDeviceType(VmDeviceGeneralType.INTERFACE);
        addAsyncOperationProgress();
        Frontend.getInstance().runQuery(QueryType.GetDeviceCustomProperties,
                params,
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                            if (returnValue != null) {
                                Map<String, String> customPropertiesList = returnValue.getReturnValue();

                                getCustomPropertySheet().setKeyValueMap(customPropertiesList);
                                getCustomPropertySheet().setIsChangeable(!customPropertiesList.isEmpty());

                                initCustomProperties();
                            }
                            removeAsyncOperationProgress();
                        }));
    }

    public void initNetworkQoSList(Guid dcId) {
        if (dcId == null) {
            return;
        }
        addAsyncOperationProgress();
        AsyncDataProvider.getInstance().getAllNetworkQos(dcId, new AsyncQuery<>(networkQoSes -> {
            getNetworkQoS().setItems(networkQoSes);
            defaultQos =
                    networkQoSes.stream()
                            .filter(new Linq.IdPredicate<>(defaultQosId))
                            .findFirst()
                            .orElse(NetworkQoSModel.EMPTY_QOS);
            getNetworkQoS().setSelectedItem(defaultQos);
            removeAsyncOperationProgress();
        }));
    }

    private void initNetworkFilterList(Version dcCompatibilityVersion) {
        addAsyncOperationProgress();
        Frontend.getInstance().runQuery(QueryType.GetAllSupportedNetworkFiltersByVersion,
                new VersionQueryParameters(dcCompatibilityVersion),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<NetworkFilter> networkFilters =
                            new ArrayList((Collection<NetworkFilter>) returnValue.getReturnValue());
                    networkFilters.add(EMPTY_FILTER);

                    getNetworkFilter().setItems(networkFilters);

                    initSelectedNetworkFilter();
                    removeAsyncOperationProgress();
                }));
    }

    private void initFailoverVnicProfiles(Guid dcId) {
        addAsyncOperationProgress();
        AsyncDataProvider.getInstance().getVnicProfilesByDcId(new AsyncQuery<>(vnicProfiles -> {
            List<VnicProfile> filteredProfiles = vnicProfiles.stream()
                    .map(profileView -> (VnicProfile) profileView)
                    .filter(vnicProfileLocal -> vnicProfile == null || !Objects.equals(vnicProfile.getId(),
                            vnicProfileLocal.getId()))
                    .filter(vnicProfile -> !vnicProfile.isPassthrough())
                    .filter(vnicProfile -> {
                        Optional<Network> optNetwork = getNetworkForVnicProfile(vnicProfile.getNetworkId());
                        return !optNetwork.isPresent() || !optNetwork.get().isExternal();
                    })
                    .collect(Collectors.toList());
            filteredProfiles.add(EMPTY_FAILOVER_VNIC_PROFILE);
            getFailoverVnicProfile().setItems(filteredProfiles);
            initSelectedFailoverProfile();
            removeAsyncOperationProgress();
        }), dcId);
    }

    protected abstract void initSelectedNetworkFilter();

    protected abstract void initSelectedFailoverProfile();

    protected abstract void updateChangeabilityIfVmsUsingTheProfile();

    private void initPassthroughChangeListener() {
        getPassthrough().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getPassthrough().getEntity()) {
                getPortMirroring().setIsChangeable(false);
                getPortMirroring().setChangeProhibitionReason(constants.portMirroringNotChangedIfPassthrough());
                getPortMirroring().setEntity(false);

                getNetworkQoS().setIsChangeable(false);
                getNetworkQoS().setChangeProhibitionReason(constants.networkQosNotChangedIfPassthrough());
                getNetworkQoS().setSelectedItem(NetworkQoSModel.EMPTY_QOS);

                getNetworkFilter().setIsChangeable(false);
                getNetworkFilter().setChangeProhibitionReason(constants.networkFilterNotChangedIfPassthrough());
                getNetworkFilter().setSelectedItem(EMPTY_FILTER);
                getMigratable().setIsChangeable(true);
            } else {
                getPortMirroring().setIsChangeable(true);
                getNetworkQoS().setIsChangeable(true);
                getNetworkFilter().setIsChangeable(true);
                getMigratable().setIsChangeable(false);

                /*
                 * if passthrough is false, then all vnicprofiles are considered to be migratable. Migratable flag
                 * then has no meaning and it's unmodifiable. We're setting it to true, to indicate user, that
                 * !passthrough means that vnicprofile is always considered migratable.
                 * */
                getMigratable().setEntity(true);
            }
            updateFailoverChangeability();
        });
    }

    private void initMigratableChangeListener() {
        getMigratable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            updateFailoverChangeability();
        });
    }

    private void updateFailoverChangeability() {
        boolean isPassthrough = getPassthrough().getEntity();
        boolean isMigratable = getMigratable().getEntity();

        if (isPassthrough && isMigratable) {
            getFailoverVnicProfile().setIsChangeable(true);
        } else {
            getFailoverVnicProfile().setIsChangeable(false);
            getFailoverVnicProfile().setSelectedItem(EMPTY_FAILOVER_VNIC_PROFILE);
        }

    }

    private Optional<Network> getNetworkForVnicProfile(Guid profileId) {
        return getNetwork().getItems().stream().filter(network -> network.getId().equals(profileId)).findFirst();
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.NETWORK_NAME_SIZE) });

        return getName().getIsValid() && getCustomPropertySheet().validate();
    }

    protected abstract void initCustomProperties();

    protected abstract ActionType getActionType();

    protected ActionParametersBase getActionParameters() {
        return new VnicProfileParameters(vnicProfile);
    }
}
