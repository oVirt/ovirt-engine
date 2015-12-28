package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

public class GetVdsInterfacesByVdsIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    public GetVdsInterfacesByVdsIdQuery(P parameters) {
        super(parameters);
    }

    @Inject
    private VdsStaticDao vdsStaticDao;

    @Inject
    private HostNetworkQosDao qosDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;


    @Override
    protected void executeQueryCommand() {
        final List<VdsNetworkInterface> vdsInterfaces =
            interfaceDao.getAllInterfacesForVds(getParameters().getId(), getUserID(), getParameters().isFiltered());

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

        List<VdsNetworkInterface> interfaces = new ArrayList<>(vdsInterfaces.size());

        if (!vdsInterfaces.isEmpty()) {
            VdsStatic vdsStatic = vdsStaticDao.get(getParameters().getId());
            Map<String, Network> networks =
                Entities.entitiesByName(networkDao.getAllForCluster(vdsStatic.getClusterId()));

            for (final VdsNetworkInterface nic : vdsInterfaces) {
                if (!nic.isBond() || nicDoesHaveSlaves(vdsInterfaces, nic)) {
                    interfaces.add(nic);
                    Network network = networks.get(nic.getNetworkName());

                    NetworkImplementationDetails networkImplementationDetails =
                        networkImplementationDetailsUtils.calculateNetworkImplementationDetails(nic, network);
                    nic.setNetworkImplementationDetails(networkImplementationDetails);
                }
            }
        }

        getQueryReturnValue().setReturnValue(interfaces);
    }

    private boolean nicDoesHaveSlaves(List<VdsNetworkInterface> vdsInterfaces, VdsNetworkInterface nic) {
        return getSlavesOfBond(vdsInterfaces, nic).size() > 0;
    }

    private List<VdsNetworkInterface> getSlavesOfBond(List<VdsNetworkInterface> vdsInterfaces,
        final VdsNetworkInterface nic) {
        return vdsInterfaces.stream().filter(bond -> StringUtils.equals(bond.getBondName(), nic.getName()))
                .collect(Collectors.toList());
    }
}
