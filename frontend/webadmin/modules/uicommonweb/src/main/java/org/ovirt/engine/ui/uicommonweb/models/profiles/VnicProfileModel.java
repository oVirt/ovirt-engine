package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.GetDeviceCustomPropertiesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public abstract class VnicProfileModel extends Model {


    private EntityModel<String> name;
    private EntityModel<Boolean> portMirroring;
    private KeyValueModel customPropertySheet;
    private EntityModel<Boolean> publicUse;
    private EntityModel<String> description;
    private final EntityModel sourceModel;
    private ListModel<Network> network;
    private ListModel<NetworkQoS> networkQoS;
    private VnicProfile vnicProfile = null;
    private final boolean customPropertiesVisible;
    private final Guid defaultQosId;

    public EntityModel<String> getName()
    {
        return name;
    }

    private void setName(EntityModel<String> value)
    {
        name = value;
    }

    public EntityModel<Boolean> getPortMirroring()
    {
        return portMirroring;
    }

    public void setPortMirroring(EntityModel<Boolean> value)
    {
        portMirroring = value;
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

    public VnicProfileModel(EntityModel sourceModel,
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
        setPortMirroring(new EntityModel<Boolean>());
        setCustomPropertySheet(new KeyValueModel());
        EntityModel<Boolean> publicUse = new EntityModel<Boolean>();
        publicUse.setEntity(true);
        setPublicUse(publicUse);
        setDescription(new EntityModel<String>());

        getNetwork().getSelectedItemChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                Network network = getNetwork().getSelectedItem();
                boolean portMirroringAllowed = (network == null || !network.isExternal());
                if (!portMirroringAllowed) {
                    getPortMirroring().setEntity(false);
                    getPortMirroring().setChangeProhibitionReason(ConstantsManager.getInstance()
                            .getConstants()
                            .portMirroringNotSupportedExternalNetworks());
                }
                getPortMirroring().setIsChangable(portMirroringAllowed);
            }
        });

        initCustomPropertySheet(dcCompatibilityVersion);
        initNetworkQoSList(dcId);
        initCommands();
    }

    protected void initCommands() {
        UICommand okCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        okCommand.setTitle(ConstantsManager.getInstance().getConstants().ok());
        okCommand.setIsDefault(true);
        getCommands().add(okCommand);
        UICommand cancelCommand = new UICommand("Cancel", this); //$NON-NLS-1$
        cancelCommand.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        cancelCommand.setIsCancel(true);
        getCommands().add(cancelCommand);
    }

    private void onSave()
    {
        if (getProgress() != null)
        {
            return;
        }

        if (!validate())
        {
            return;
        }

        // Save changes.
        flush();

        startProgress(null);

        Frontend.getInstance().runAction(getVdcActionType(),
                getActionParameters(),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void executed(FrontendActionAsyncResult result) {
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        stopProgress();

                        if (returnValue != null && returnValue.getSucceeded())
                        {
                            cancel();
                        }
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
        vnicProfile.setPortMirroring(getPortMirroring().getEntity());

        if (customPropertiesVisible) {
            vnicProfile.setCustomProperties(KeyValueModel.convertProperties(getCustomPropertySheet().serialize()));
        } else {
            vnicProfile.setCustomProperties(null);
        }

        vnicProfile.setDescription(getDescription().getEntity());
    }

    private void cancel()
    {
        sourceModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) //$NON-NLS-1$
        {
            onSave();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
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
        startProgress(null);
        Frontend.getInstance().runQuery(VdcQueryType.GetDeviceCustomProperties,
                params,
                new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object target, Object returnValue) {
                                if (returnValue != null) {
                                    Map<String, String> customPropertiesList =
                                            ((VdcQueryReturnValue) returnValue).getReturnValue();

                                    getCustomPropertySheet().setKeyValueMap(customPropertiesList);
                                    getCustomPropertySheet().setIsChangable(!customPropertiesList.isEmpty());

                                    initCustomProperties();
                                }
                                stopProgress();
                            }
                        }));
    }

    public void initNetworkQoSList(Guid dcId) {
        if (dcId == null) {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                List<NetworkQoS> networkQoSes = (List<NetworkQoS>) ReturnValue;
                getNetworkQoS().setItems(networkQoSes);
                getNetworkQoS().setSelectedItem(Linq.findNetworkQosById(networkQoSes, defaultQosId));
            }
        };

        AsyncDataProvider.getInstance().getAllNetworkQos(dcId, _asyncQuery);
    }

    public boolean validate()
    {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new AsciiNameValidation() });

        return getName().getIsValid() && getCustomPropertySheet().validate();
    }

    protected abstract void initCustomProperties();

    protected abstract VdcActionType getVdcActionType();

    protected VdcActionParametersBase getActionParameters() {
        return new VnicProfileParameters(vnicProfile);
    }
}
