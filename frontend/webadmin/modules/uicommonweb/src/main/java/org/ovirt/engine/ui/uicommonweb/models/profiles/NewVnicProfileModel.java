package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVnicProfileModel extends VnicProfileModel {

    public NewVnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion, boolean customPropertiesVisible,
                               Guid dcId) {
        super(sourceModel, dcCompatibilityVersion, customPropertiesVisible, dcId);
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfileTitle());
        setHashName("new_vnic_profile"); //$NON-NLS-1$
        initNetworkQoSList(null);
        getPortMirroring().setEntity(false);
    }

    public NewVnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion, Guid dcId) {
        this(sourceModel, dcCompatibilityVersion, true, dcId);
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
