package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetNetworkDisplayByClusterIdQuery<P extends VdsGroupQueryParamenters> extends QueriesCommandBase<P> {
    public GetNetworkDisplayByClusterIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid vdsgroupid = getParameters().getVdsGroupId();
        List<network_cluster> all = DbFacade.getInstance().getNetworkClusterDAO().getAllForCluster(vdsgroupid);
        final network_cluster nc = LinqUtils.firstOrNull(all, new Predicate<network_cluster>() {
            @Override
            public boolean eval(network_cluster network_cluster) {
                return network_cluster.getis_display();
            }
        });
        if (nc != null) {
            getQueryReturnValue().setReturnValue(
                    LinqUtils.firstOrNull(DbFacade.getInstance().getNetworkDAO().getAllForCluster(vdsgroupid),
                            new Predicate<Network>() {
                                @Override
                                public boolean eval(Network network) {
                                    return network.getId().equals(nc.getnetwork_id());
                                }
                            }));
        }
    }
}
