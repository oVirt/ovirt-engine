package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class GetVdsInterfacesByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsInterfacesByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<VdsNetworkInterface> list = getDbFacade().getInterfaceDao()
                .getAllInterfacesForVds(getParameters().getId(), getUserID(), getParameters().isFiltered());

        // 1. here we return all interfaces (eth0, eth1, eth2) - the first
        // condition
        // 2. we also return bonds that connected to network and has interfaces
        // - the second condition
        // i.e.
        // we have:
        // Network | Interface
        // -------------------
        // red-> |->eth0
        // |->eth1
        // | |->eth2
        // blue-> |->bond0->|->eth3
        // |->bond1
        //
        // we return: eth0, eth1, eth2, eth3, bond0
        // we don't return bond1 because he is not connected to network and has
        // no child interfaces

        List<VdsNetworkInterface> interfaces = new ArrayList<VdsNetworkInterface>(list.size());

        if (!list.isEmpty()) {
            VdsStatic vdsStatic = getDbFacade().getVdsStaticDao().get(getParameters().getId());
            HostNetworkQosDao qosDao = getDbFacade().getHostNetworkQosDao();
            Map<String, Network> networks = Entities.entitiesByName(
                    getDbFacade().getNetworkDao().getAllForCluster(vdsStatic.getVdsGroupId()));
            for (final VdsNetworkInterface i : list) {
                if (!Boolean.TRUE.equals(i.getBonded())
                        || LinqUtils.filter(list, new Predicate<VdsNetworkInterface>() {
                                @Override
                                public boolean eval(VdsNetworkInterface bond) {
                                    return StringUtils.equals(bond.getBondName(), i.getName());
                                }
                            }).size() > 0) {
                    interfaces.add(i);
                    Network network = networks.get(i.getNetworkName());
                    i.setNetworkImplementationDetails(NetworkUtils.calculateNetworkImplementationDetails(network,
                            network == null ? null : qosDao.get(network.getQosId()),
                            i));
                }
            }
        }

        getQueryReturnValue().setReturnValue(interfaces);
    }
}
