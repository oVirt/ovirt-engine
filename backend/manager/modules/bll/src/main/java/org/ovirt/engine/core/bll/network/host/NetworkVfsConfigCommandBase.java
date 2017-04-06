package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VfsConfigNetworkParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;

public abstract class NetworkVfsConfigCommandBase extends VfsConfigCommandBase<VfsConfigNetworkParameters> {

    @Inject
    private NetworkDao networkDao;

    public NetworkVfsConfigCommandBase(VfsConfigNetworkParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validate() {
        return super.validate() && validate(getVfsConfigValidator().settingSpecificNetworksAllowed())
                && validate(getVfsConfigValidator().networkExists(getNetworkId()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__HOST_NIC_VFS_CONFIG_NETWORK);
    }

    protected Guid getNetworkId() {
        return getParameters().getNetworkId();
    }

    protected Network getNetwork() {
        return networkDao.get(getNetworkId());
    }

    public String getNetworkName() {
        return getNetwork().getName();
    }
}
