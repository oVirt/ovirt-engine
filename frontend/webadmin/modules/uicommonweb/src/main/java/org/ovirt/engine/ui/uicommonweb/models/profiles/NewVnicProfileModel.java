package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVnicProfileModel extends VnicProfileModel {

    public NewVnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion, boolean customPropertiesVisible) {
        super(sourceModel, dcCompatibilityVersion, customPropertiesVisible);
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfileTitle());
        setHashName("new_vnic_profile"); //$NON-NLS-1$

        getPortMirroring().setEntity(false);
    }

    public NewVnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion) {
        this(sourceModel, dcCompatibilityVersion, true);
    }

    @Override
    protected void initCustomProperties() {
        // Do nothing
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.AddVnicProfile;
    }

    @Override
    protected VdcActionParametersBase getActionParameters() {
        VnicProfileParameters parameters = new VnicProfileParameters(getProfile());
        parameters.setPublicUse((Boolean) getPublicUse().getEntity());
        return parameters;
    }

}
