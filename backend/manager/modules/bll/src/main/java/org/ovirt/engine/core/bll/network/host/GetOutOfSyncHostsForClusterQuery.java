package org.ovirt.engine.core.bll.network.host;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

public class GetOutOfSyncHostsForClusterQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private InterfaceDao interfaceDao;
    @Inject
    private NetworkDao networkDao;
    @Inject
    private NetworkImplementationDetailsUtils util;

    public GetOutOfSyncHostsForClusterQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Map<String, Network> clusterNetworksByName = networkDao
            .getAllForCluster(getParameters().getId())
            .stream()
            .collect(Collectors.toMap(Network::getName, Function.identity()));

        Set<Guid> vdsIdsWithOutOfSyncInterface = interfaceDao
            .getAllInterfacesByClusterId(getParameters().getId())
            .stream()
            .map(iface -> new Pair<>(iface.getVdsId(),
                util.calculateNetworkImplementationDetails(iface, clusterNetworksByName.get(iface.getNetworkName()))))
            .filter(pair -> pair.getSecond() != null && pair.getSecond().isManaged() && !pair.getSecond().isInSync())
            .map(Pair::getFirst)
            .collect(Collectors.toSet());

        getQueryReturnValue().setReturnValue(vdsIdsWithOutOfSyncInterface);
    }

}
