package org.ovirt.engine.core.bll.network.vm;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VnicProfileParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class VnicProfileCommandBase<T extends VnicProfileParameters> extends CommandBase<T> {

    private Network network;

    public VnicProfileCommandBase(T parameters) {
        super(parameters);
    }

    protected VnicProfile getVnicProfile() {
        return getParameters().getVnicProfile();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__VNIC_PROFILE);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Guid vnicProfileId = getVnicProfile() == null ? null : getVnicProfile().getId();

        return Collections.singletonList(new PermissionSubject(vnicProfileId,
                VdcObjectType.VnicProfile,
                getActionType().getActionGroup()));
    }

    public String getVnicProfileName() {
        return getVnicProfile().getName();
    }

    public String getNetworkName() {
        return getNetwork().getName();
    }

    public String getDataCenterName() {
        return getStoragePoolDao().get(getNetwork().getDataCenterId()).getName();
    }

    private Network getNetwork() {
        if (network == null) {
            network = getNetworkDao().get(getVnicProfile().getNetworkId());
        }

        return network;
    }
}
