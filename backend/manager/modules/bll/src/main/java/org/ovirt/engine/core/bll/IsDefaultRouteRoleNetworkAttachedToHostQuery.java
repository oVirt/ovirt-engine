package org.ovirt.engine.core.bll;

import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.queries.IsDefaultRouteRoleNetworkAttachedToHostQueryParameters;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class IsDefaultRouteRoleNetworkAttachedToHostQuery<P extends IsDefaultRouteRoleNetworkAttachedToHostQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkDao networkDao;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    public IsDefaultRouteRoleNetworkAttachedToHostQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Optional<Network> defRouteNetwork = networkDao
            .getAllForCluster(getParameters().getClusterId()).stream()
            .filter(n -> n.getCluster().isDefaultRoute())
            .findFirst();
        Optional<NetworkAttachment> attachment = networkAttachmentDao
            .getAllForHost(getParameters().getHostId()).stream()
            .filter(na -> defRouteNetwork.isPresent() && Objects.equals(na.getNetworkId(), defRouteNetwork.get().getId()))
            .findFirst();
        getQueryReturnValue().setReturnValue(attachment.isPresent());
    }
}
