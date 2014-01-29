package org.ovirt.engine.ui.uicommonweb.models.profiles;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVnicProfileModel extends VnicProfileModel {

    public NewVnicProfileModel(EntityModel sourceModel,
            Version dcCompatibilityVersion,
            boolean customPropertiesVisible,
            Guid dcId) {
        super(sourceModel, dcCompatibilityVersion, customPropertiesVisible, dcId, null);
        setTitle(ConstantsManager.getInstance().getConstants().vnicProfileTitle());
        setHelpTag(HelpTag.new_vnic_profile);
        setHashName("new_vnic_profile"); //$NON-NLS-1$
        getPortMirroring().setEntity(false);
    }

    public NewVnicProfileModel(EntityModel sourceModel, Version dcCompatibilityVersion, Guid dcId) {
        this(sourceModel, dcCompatibilityVersion, true, dcId);
    }

    public NewVnicProfileModel() {
        this(null, null, false, null);
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
