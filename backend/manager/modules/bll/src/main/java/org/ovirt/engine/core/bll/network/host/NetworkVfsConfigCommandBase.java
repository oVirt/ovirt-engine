package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VfsConfigNetworkParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public abstract class NetworkVfsConfigCommandBase extends VfsConfigCommandBase<VfsConfigNetworkParameters> {

    public NetworkVfsConfigCommandBase(VfsConfigNetworkParameters parameters) {
        this(parameters, null);
    }

    public NetworkVfsConfigCommandBase(VfsConfigNetworkParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean canDoAction() {
        return super.canDoAction() && validate(getVfsConfigValidator().settingSpecificNetworksAllowed())
                && validate(getVfsConfigValidator().networkExists(getNetworkId()));
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__HOST_NIC_VFS_CONFIG_NETWORK);
    }

    protected Guid getNetworkId() {
        return getParameters().getNetworkId();
    }

    protected Network getNetwork() {
        return getNetworkDAO().get(getNetworkId());
    }

    public String getNetworkName() {
        return getNetwork().getName();
    }
}
