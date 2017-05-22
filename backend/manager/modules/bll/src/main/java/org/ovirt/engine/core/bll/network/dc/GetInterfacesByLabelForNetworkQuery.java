package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

public class GetInterfacesByLabelForNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    @Inject
    private NetworkClusterDao networkClusterDao;

    @Inject
    private InterfaceDao interfaceDao;

    public GetInterfacesByLabelForNetworkQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        Network network = networkDao.get(getParameters().getId());
        Set<VdsNetworkInterface> interfacesByLabelForNetwork = new HashSet<>();

        if (network == null) {
            getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
            return;
        }

        List<NetworkCluster> clusters = networkClusterDao.getAllForNetwork(network.getId());

        if (clusters.isEmpty()) {
            getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
            return;
        }

        List<VdsNetworkInterface> labeledNics = new ArrayList<>();
        for (NetworkCluster networkCluster : clusters) {
            labeledNics.addAll(
                    interfaceDao.getAllInterfacesByLabelForCluster(networkCluster.getClusterId(), network.getLabel()));
        }

        if (labeledNics.isEmpty()) {
            getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
            return;
        }

        List<VdsNetworkInterface> networkNics = interfaceDao.getVdsInterfacesByNetworkId(network.getId());
        Map<String, VdsNetworkInterface> labeledNicsByName = Entities.entitiesByName(labeledNics);

        for (VdsNetworkInterface networkNic : networkNics) {
            if (labeledNicsByName.containsKey(NetworkCommonUtils.stripVlan(networkNic))) {
                interfacesByLabelForNetwork.add(networkNic);
            }
        }

        getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
    }
}
