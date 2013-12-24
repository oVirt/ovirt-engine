package org.ovirt.engine.core.bll.network;

import java.util.List;

import org.ovirt.engine.core.common.action.SetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public abstract class NetworkParametersBuilder {
    protected SetupNetworksParameters createSetupNetworksParameters(Guid hostId) {
        VDS host = new VDS();
        host.setId(hostId);
        NetworkConfigurator configurator = new NetworkConfigurator(host);
        List<VdsNetworkInterface> nics = configurator.filterBondsWithoutSlaves(getHostInterfaces(hostId));
        return configurator.createSetupNetworkParams(nics);
    }

    private List<VdsNetworkInterface> getHostInterfaces(Guid hostId) {
        return DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(hostId);
    }
}
