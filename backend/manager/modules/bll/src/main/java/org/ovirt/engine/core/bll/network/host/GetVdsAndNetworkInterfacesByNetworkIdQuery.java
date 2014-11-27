package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * A query to retrieve all Host-Network Interface pairs that the given Network is attached to.
 */
public class GetVdsAndNetworkInterfacesByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VdsDAO vdsDAO;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private HostNetworkQosDao hostNetworkQosDao;

    public GetVdsAndNetworkInterfacesByNetworkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    public VdsDAO getVdsDao() {
        return vdsDAO;
    }

    InterfaceDao getInterfaceDao() {
        return interfaceDao;
    }

    NetworkDao getNetworkDao() {
        return networkDao;
    }

    HostNetworkQosDao getHostNetworkQosDao() {
        return hostNetworkQosDao;
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> vdsList = getVdsDao().getAllForNetwork(getParameters().getId());
        List<VdsNetworkInterface> vdsNetworkInterfaceList =
                getInterfaceDao().getVdsInterfacesByNetworkId(getParameters().getId());
        final Map<Guid, VDS> vdsById = Entities.businessEntitiesById(vdsList);
        List<PairQueryable<VdsNetworkInterface, VDS>> vdsInterfaceVdsPairs =
                new ArrayList<PairQueryable<VdsNetworkInterface, VDS>>();
        Network network = getNetworkDao().get(getParameters().getId());
        HostNetworkQos qos = getHostNetworkQosDao().get(network.getQosId());
        for (final VdsNetworkInterface vdsNetworkInterface : vdsNetworkInterfaceList) {
            vdsInterfaceVdsPairs.add(new PairQueryable<VdsNetworkInterface, VDS>(vdsNetworkInterface,
                    vdsById.get(vdsNetworkInterface.getVdsId())));
            VdsNetworkInterface.NetworkImplementationDetails vdsInterfaceNetworkImplementationDetails =
                    NetworkUtils.calculateNetworkImplementationDetails(network, qos, vdsNetworkInterface);
            vdsNetworkInterface.setNetworkImplementationDetails(vdsInterfaceNetworkImplementationDetails);
        }

        getQueryReturnValue().setReturnValue(vdsInterfaceVdsPairs);
    }

}
