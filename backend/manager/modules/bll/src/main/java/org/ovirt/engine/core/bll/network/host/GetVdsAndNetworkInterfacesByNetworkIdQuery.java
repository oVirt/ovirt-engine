package org.ovirt.engine.core.bll.network.host;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.HostLocking;
import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkAttachmentDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.vdsbroker.NetworkImplementationDetailsUtils;

/**
 * A query to retrieve all Host-Network Interface pairs that the given Network is attached to.
 */
public class GetVdsAndNetworkInterfacesByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private InterfaceDao interfaceDao;

    @Inject
    private NetworkDao networkDao;

    @Inject
    private HostNetworkQosDao hostNetworkQosDao;

    @Inject
    private NetworkAttachmentDao networkAttachmentDao;

    @Inject
    private NetworkImplementationDetailsUtils networkImplementationDetailsUtils;

    @Inject
    private LockManager lockManager;
    @Inject
    private HostLocking hostLocking;

    public GetVdsAndNetworkInterfacesByNetworkIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<VDS> vdsList = vdsDao.getAllForNetwork(getParameters().getId());
        List<VdsNetworkInterface> vdsNetworkInterfaceList =
                interfaceDao.getVdsInterfacesByNetworkId(getParameters().getId());
        final Map<Guid, VDS> vdsById = Entities.businessEntitiesById(vdsList);
        List<PairQueryable<VdsNetworkInterface, VDS>> vdsInterfaceVdsPairs =
                new ArrayList<>();
        Network network = networkDao.get(getParameters().getId());
        for (final VdsNetworkInterface vdsNetworkInterface : vdsNetworkInterfaceList) {
            vdsInterfaceVdsPairs.add(new PairQueryable<>(vdsNetworkInterface,
                vdsById.get(vdsNetworkInterface.getVdsId())));

            NetworkImplementationDetails vdsInterfaceNetworkImplementationDetails =
                networkImplementationDetailsUtils.calculateNetworkImplementationDetails(vdsNetworkInterface,
                    network);
            vdsNetworkInterface.setNetworkImplementationDetails(vdsInterfaceNetworkImplementationDetails);
        }
        for (VDS vds : vdsById.values()) {
            vds.setNetworkOperationInProgress(lockManager.isExclusiveLockPresent(new EngineLock(hostLocking.getSetupNetworksLock(vds.getId()))));
        }

        getQueryReturnValue().setReturnValue(vdsInterfaceVdsPairs);
    }
}
