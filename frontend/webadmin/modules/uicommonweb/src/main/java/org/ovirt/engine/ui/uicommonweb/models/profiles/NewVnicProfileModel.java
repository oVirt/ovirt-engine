package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVnicProfileModel extends VnicProfileModel {

    public NewVnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion) {
        super(sourceModel, dcCompatibilityVersion);
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfileTitle());
        setHashName("new_vnic_profile"); //$NON-NLS-1$

        getPortMirroring().setEntity(false);
    }

    public VnicProfile getProfile() {
        // no profile for new
        return null;
    }

    @Override
    protected void initCustomProperties() {
        // Do nothing
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.AddVnicProfile;
    }

}
