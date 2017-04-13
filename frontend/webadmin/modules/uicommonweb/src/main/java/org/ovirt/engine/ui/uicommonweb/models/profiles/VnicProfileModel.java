package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VersionQueryParameters;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkFilter;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.GetDeviceCustomPropertiesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.IModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.NetworkQoSModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public abstract class VnicProfileModel extends Model {


    private EntityModel<String> name;
    private EntityModel<Boolean> portMirroring;
    private EntityModel<Boolean> passthrough;
    private EntityModel<Boolean> migratable;
    private KeyValueModel customPropertySheet;
    private EntityModel<Boolean> publicUse;
    private EntityModel<String> description;
    private final IModel sourceModel;
    private ListModel<Network> network;
    private ListModel<NetworkQoS> networkQoS;
    private ListModel<NetworkFilter> networkFilter;
    private VnicProfile vnicProfile = null;
    private final boolean customPropertiesVisible;
    private final Guid defaultQosId;
    private NetworkQoS defaultQos;

    private static final NetworkFilter EMPTY_FILTER = new NetworkFilter();

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

    public VnicProfileModel(IModel sourceModel,
            Version dcCompatibilityVersion,
            boolean customPropertiesVisible,
            Guid dcId,
            Guid defaultQosId) {
        this.sourceModel = sourceModel;
        this.customPropertiesVisible = customPropertiesVisible;
        this.defaultQosId = defaultQosId;

        setName(new EntityModel<String>());
        setNetwork(new ListModel<Network>());
        setNetworkQoS(new ListModel<NetworkQoS>());
        setNetworkFilter(new ListModel<NetworkFilter>());
        setPortMirroring(new EntityModel<Boolean>());
        setPassthrough(new EntityModel<Boolean>());
        setMigratable(new EntityModel<Boolean>());
        setCustomPropertySheet(new KeyValueModel());
        EntityModel<Boolean> publicUse = new EntityModel<>();
        publicUse.setEntity(true);
        setPublicUse(publicUse);
        setDescription(new EntityModel<String>());

        getNetwork().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            Network network = getNetwork().getSelectedItem();
            boolean portMirroringAllowed = network == null || !network.isExternal();
            if (!portMirroringAllowed) {
                getPortMirroring().setEntity(false);
                getPortMirroring().setChangeProhibitionReason(ConstantsManager.getInstance()
                        .getConstants()
                        .portMirroringNotSupportedExternalNetworks());
            }
            getPortMirroring().setIsChangeable(portMirroringAllowed);
        });


        initPassthroughChangeListener();

        getPassthrough().setEntity(false);
        getMigratable().setEntity(false);

        initCustomPropertySheet(dcCompatibilityVersion);
        initNetworkQoSList(dcId);
        initNetworkFilterList(dcCompatibilityVersion);
        initCommands();
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

        Frontend.getInstance().runAction(getVdcActionType(),
                getActionParameters(),
                result -> {
                    VdcReturnValueBase returnValue = result.getReturnValue();
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
        }
        else if ("Cancel".equals(command.getName())) { //$NON-NLS-1$
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
        startProgress();
        Frontend.getInstance().runQuery(VdcQueryType.GetDeviceCustomProperties,
                params,
                new AsyncQuery<VdcQueryReturnValue>(returnValue -> {
                            if (returnValue != null) {
                                Map<String, String> customPropertiesList = returnValue.getReturnValue();

                                getCustomPropertySheet().setKeyValueMap(customPropertiesList);
                                getCustomPropertySheet().setIsChangeable(!customPropertiesList.isEmpty());

                                initCustomProperties();
                            }
                            stopProgress();
                        }));
    }

    public void initNetworkQoSList(Guid dcId) {
        if (dcId == null) {
            return;
        }

        AsyncDataProvider.getInstance().getAllNetworkQos(dcId, new AsyncQuery<>(networkQoSes -> {
            getNetworkQoS().setItems(networkQoSes);
            defaultQos =
                    networkQoSes.stream()
                            .filter(new Linq.IdPredicate<>(defaultQosId))
                            .findFirst()
                            .orElse(NetworkQoSModel.EMPTY_QOS);
            getNetworkQoS().setSelectedItem(defaultQos);
        }));
    }

    public void initNetworkFilterList(Version dcCompatibilityVersion) {
        Frontend.getInstance().runQuery(VdcQueryType.GetAllSupportedNetworkFiltersByVersion,
                new VersionQueryParameters(dcCompatibilityVersion),
                new AsyncQuery<VdcQueryReturnValue>(returnValue -> {
                    List<NetworkFilter> networkFilters =
                            new ArrayList((Collection<NetworkFilter>) returnValue.getReturnValue());
                    networkFilters.add(EMPTY_FILTER);

                    getNetworkFilter().setItems(networkFilters);

                    initSelectedNetworkFilter();
                }));
    }

    protected abstract void initSelectedNetworkFilter();

    private void initPassthroughChangeListener() {
        getPassthrough().getEntityChangedEvent().addListener((ev, sender, args) -> {
            if (getPassthrough().getEntity()) {
                getPortMirroring().setChangeProhibitionReason(ConstantsManager.getInstance()
                        .getConstants()
                        .portMirroringNotChangedIfPassthrough());
                getPortMirroring().setIsChangeable(false);
                getPortMirroring().setEntity(false);

                getNetworkQoS().setChangeProhibitionReason(ConstantsManager.getInstance()
                        .getConstants()
                        .networkQosNotChangedIfPassthrough());
                getNetworkQoS().setIsChangeable(false);
                getNetworkQoS().setSelectedItem(NetworkQoSModel.EMPTY_QOS);

                getNetworkFilter().setChangeProhibitionReason(ConstantsManager.getInstance()
                        .getConstants()
                        .networkFilterNotChangedIfPassthrough());
                getNetworkFilter().setIsChangeable(false);
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
        });
    }

    public boolean validate() {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });

        return getName().getIsValid() && getCustomPropertySheet().validate();
    }

    protected abstract void initCustomProperties();

    protected abstract VdcActionType getVdcActionType();

    protected VdcActionParametersBase getActionParameters() {
        return new VnicProfileParameters(vnicProfile);
    }
}
