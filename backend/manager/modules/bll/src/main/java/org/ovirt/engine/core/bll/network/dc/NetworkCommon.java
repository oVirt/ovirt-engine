package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public abstract class NetworkCommon<T extends VdcActionParametersBase> extends CommandBase<T> {

    public NetworkCommon(Guid id) {
        super(id);
    }

    public NetworkCommon(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected abstract Network getNetwork();

    public String getNetworkName() {
        return getNetwork().getName();
    }

    protected void removeVnicProfiles() {
        List<VnicProfile> profiles = getVnicProfileDao().getAllForNetwork(getNetwork().getId());
        for (VnicProfile vnicProfile : profiles) {
            getCompensationContext().snapshotEntity(vnicProfile);
            getVnicProfileDao().remove(vnicProfile.getId());
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__NETWORK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        Network network = getNetwork();
        Guid networkId = network == null ? null : network.getId();

        return Collections.singletonList(new PermissionSubject(networkId,
                VdcObjectType.Network,
                getActionType().getActionGroup()));
    }
}
