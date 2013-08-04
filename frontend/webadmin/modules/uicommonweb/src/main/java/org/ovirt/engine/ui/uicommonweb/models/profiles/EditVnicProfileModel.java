package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.key_value.KeyValueModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class EditVnicProfileModel extends VnicProfileModel {

    public EditVnicProfileModel(EntityModel sourceModel,
            Version dcCompatibilityVersion,
            VnicProfile profile,
            boolean customPropertiesVisible) {
        super(sourceModel, dcCompatibilityVersion, customPropertiesVisible);
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfileTitle());
        setHashName("edit_vnic_profile"); //$NON-NLS-1$

        setProfile(profile);

        getName().setEntity(profile.getName());
        getPortMirroring().setEntity(getProfile().isPortMirroring());

        getPublicUse().setIsAvailable(false);
    }

    public EditVnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion, VnicProfile profile) {
        this(sourceModel, dcCompatibilityVersion, profile, true);
    }

    @Override
    protected void initCustomProperties() {
        getCustomPropertySheet().setEntity(KeyValueModel
                .convertProperties(getProfile().getCustomProperties()));
    }

    @Override
    protected VdcActionType getVdcActionType() {
        return VdcActionType.UpdateVnicProfile;
    }
}
