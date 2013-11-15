package org.ovirt.engine.ui.uicommonweb.models.profiles;

import java.util.ArrayList;
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
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public abstract class VnicProfileModel extends Model {

    private static NetworkQoS emptyQos;

    private EntityModel name;
    private EntityModel portMirroring;
    private KeyValueModel customPropertySheet;
    private EntityModel publicUse;
    private EntityModel description;
    private final EntityModel sourceModel;
    private ListModel network;
    private ListModel networkQoS;
    private VnicProfile vnicProfile = null;
    private final boolean customPropertiesVisible;
    private final Guid defaultQosId;

    private static NetworkQoS getEmptyQos() {
        if (emptyQos == null) {
            emptyQos = new NetworkQoS();
            emptyQos.setName(ConstantsManager.getInstance().getConstants().unlimitedQoSTitle());
            emptyQos.setId(Guid.Empty);
        }
        return emptyQos;
    }

    public EntityModel getName()
    {
        return name;
    }

    private void setName(EntityModel value)
    {
        name = value;
    }

    public EntityModel getPortMirroring()
    {
        return portMirroring;
    }

    public void setPortMirroring(EntityModel value)
    {
        portMirroring = value;
    }

    public KeyValueModel getCustomPropertySheet() {
        return customPropertySheet;
    }

    public void setCustomPropertySheet(KeyValueModel customPropertySheet) {
        this.customPropertySheet = customPropertySheet;
    }

    public EntityModel getPublicUse() {
        return publicUse;
    }

    public void setPublicUse(EntityModel publicUse) {
        this.publicUse = publicUse;
    }

    public EntityModel getDescription() {
        return description;
    }

    public void setDescription(EntityModel description) {
        this.description = description;
    }

    public ListModel getNetwork() {
        return network;
    }

    public void setNetwork(ListModel network) {
        this.network = network;
    }

    public void setProfile(VnicProfile vnicProfile) {
        this.vnicProfile = vnicProfile;
    }

    public VnicProfile getProfile() {
        return vnicProfile;
    }

    public ListModel getNetworkQoS() {
        return networkQoS;
    }

    public void setNetworkQoS(ListModel networkQoS) {
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

        setName(new EntityModel());
        setNetwork(new ListModel());
        setNetworkQoS(new ListModel());
        setPortMirroring(new EntityModel());
        setCustomPropertySheet(new KeyValueModel());
        EntityModel publicUse = new EntityModel();
        publicUse.setEntity(true);
        setPublicUse(publicUse);
        setDescription(new EntityModel());

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
        vnicProfile.setName((String) getName().getEntity());
        Network network = (Network) getNetwork().getSelectedItem();
        vnicProfile.setNetworkId(network != null ? network.getId() : null);
        NetworkQoS networkQoS = (NetworkQoS) getNetworkQoS().getSelectedItem();
        vnicProfile.setNetworkQosId(networkQoS != null
                && networkQoS.getId() != null
                && !networkQoS.getId().equals(Guid.Empty)
                ? networkQoS.getId() : null);
        vnicProfile.setPortMirroring((Boolean) getPortMirroring().getEntity());

        if (customPropertiesVisible) {
            vnicProfile.setCustomProperties(KeyValueModel.convertProperties(getCustomPropertySheet().getEntity()));
        } else {
            vnicProfile.setCustomProperties(null);
        }

        vnicProfile.setDescription((String) getDescription().getEntity());
    }

    private void cancel()
    {
        sourceModel.setWindow(null);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            onSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
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
                                            ((Map<String, String>) ((VdcQueryReturnValue) returnValue).getReturnValue());

                                    List<String> lines = new ArrayList<String>();

                                    for (Map.Entry<String, String> keyValue : customPropertiesList.entrySet()) {
                                        lines.add(keyValue.getKey() + '=' + keyValue.getValue());
                                    }
                                    getCustomPropertySheet().setKeyValueString(lines);
                                    getCustomPropertySheet().setIsChangable(!lines.isEmpty());

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
                ArrayList<NetworkQoS> networkQoSes =
                        (ArrayList<NetworkQoS>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                networkQoSes.add(0, getEmptyQos());
                getNetworkQoS().setItems(networkQoSes);
                setSelectedNetworkQoSId(defaultQosId);
            }
        };

        IdQueryParameters queryParams = new IdQueryParameters(dcId);
        Frontend.getInstance().runQuery(VdcQueryType.GetAllNetworkQosByStoragePoolId, queryParams, _asyncQuery);
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

    private void setSelectedNetworkQoSId(Guid networkQoSId) {
        for (Object item : getNetworkQoS().getItems()) {
            if (((NetworkQoS) item).getId().equals(networkQoSId)) {
                getNetworkQoS().setSelectedItem(item);
                return;
            }
        }
        getNetworkQoS().setSelectedItem(getEmptyQos());
    }
}
