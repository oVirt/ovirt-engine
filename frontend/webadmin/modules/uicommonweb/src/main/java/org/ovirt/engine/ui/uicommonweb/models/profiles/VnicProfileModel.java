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
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetDeviceCustomPropertiesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public abstract class VnicProfileModel extends Model {

    private EntityModel name;
    private EntityModel portMirroring;
    private KeyValueModel customPropertySheet;
    private EntityModel publicUse;
    private final EntityModel sourceModel;
    private final Version dcCompatibilityVersion;
    private final boolean customPropertiesSupported;
    private ListModel network;
    private VnicProfile vnicProfile = null;
    private boolean customPropertiesVisible;

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

    public Version getDcCompatibilityVersion() {
        return dcCompatibilityVersion;
    }

    public ListModel getNetwork() {
        return network;
    }

    public void setNetwork(ListModel network) {
        this.network = network;
    }

    public EntityModel getSourceModel() {
        return sourceModel;
    }

    public void setProfile(VnicProfile vnicProfile) {
        this.vnicProfile = vnicProfile;
    }

    public VnicProfile getProfile() {
        return vnicProfile;
    }

    public VnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion, boolean customPropertiesVisible) {
        this.sourceModel = sourceModel;
        this.dcCompatibilityVersion = dcCompatibilityVersion;
        this.customPropertiesVisible = customPropertiesVisible;

        customPropertiesSupported =
                (Boolean) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.SupportCustomDeviceProperties,
                        dcCompatibilityVersion.toString());

        setName(new EntityModel());
        setNetwork(new ListModel());
        setPortMirroring(new EntityModel());
        setCustomPropertySheet(new KeyValueModel());
        EntityModel publicUse = new EntityModel();
        publicUse.setEntity(true);
        setPublicUse(publicUse);
        getPortMirroring().setIsChangable(isPortMirroringSupported());
        initCustomPropertySheet();

        initCommands();
    }

    public VnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion) {
        this(sourceModel, dcCompatibilityVersion, true);
    }

    protected boolean isPortMirroringSupported() {
        Version v31 = new Version(3, 1);
        boolean isLessThan31 = getDcCompatibilityVersion().compareTo(v31) < 0;

        return !isLessThan31;
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

        Frontend.RunAction(getVdcActionType(),
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
        vnicProfile.setPortMirroring((Boolean) getPortMirroring().getEntity());

        if (customPropertiesVisible) {
            vnicProfile.setCustomProperties(KeyValueModel.convertProperties(getCustomPropertySheet().getEntity()));
        } else {
            vnicProfile.setCustomProperties(null);
        }
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

    private void initCustomPropertySheet() {
        if (!customPropertiesVisible) {
            return;
        }

        if (customPropertiesSupported) {
            GetDeviceCustomPropertiesParameters params = new GetDeviceCustomPropertiesParameters();
            params.setVersion(getDcCompatibilityVersion());
            params.setDeviceType(VmDeviceGeneralType.INTERFACE);
            startProgress(null);
            Frontend.RunQuery(VdcQueryType.GetDeviceCustomProperties,
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
        } else {
            getCustomPropertySheet().setIsChangable(false);
        }
    }

    public boolean validate()
    {
        getName().validateEntity(new IValidation[] { new NotEmptyValidation(), new I18NNameValidation() });

        return getName().getIsValid() && getCustomPropertySheet().validate();
    }

    protected abstract void initCustomProperties();

    protected abstract VdcActionType getVdcActionType();

    protected VdcActionParametersBase getActionParameters() {
        return new VnicProfileParameters(vnicProfile);
    }
}
