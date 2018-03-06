package org.ovirt.engine.core.bll.network.cluster;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetAllVmNetworksByClusterIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private NetworkDao networkDao;

    @Inject
    ClusterDao clusterDao;

    private Cluster cluster;

    public GetAllVmNetworksByClusterIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        final Cluster cluster = getCluster();
        Stream<Network> networkStream =
                networkDao.getAllForCluster(cluster.getId(), getUserID(), getParameters().isFiltered())
                        .stream()
                        .filter(Network::isVmNetwork);
        if (cluster.hasRequiredSwitchType(SwitchType.OVS)) {
            networkStream = networkStream.filter(Network::isExternal);
        }
        getQueryReturnValue().setReturnValue(networkStream.collect(Collectors.toList()));
    }

    @Override
    protected boolean validateInputs() {
        if (!super.validateInputs()) {
            return false;
        }

        if (getCluster() == null) {
            getQueryReturnValue().setExceptionString(EngineMessage.VM_CLUSTER_IS_NOT_VALID.name());
            getQueryReturnValue().setSucceeded(false);
            return false;
        }

        return true;
    }

    public Cluster getCluster() {
        if (cluster == null) {
            cluster = clusterDao.get(getParameters().getId());
        }
        return cluster;
    }
}
