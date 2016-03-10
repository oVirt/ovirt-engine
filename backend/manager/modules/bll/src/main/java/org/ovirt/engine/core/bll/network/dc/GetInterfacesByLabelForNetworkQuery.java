package org.ovirt.engine.core.bll.network.dc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;

public class GetInterfacesByLabelForNetworkQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetInterfacesByLabelForNetworkQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Network network = getDbFacade().getNetworkDao().get(getParameters().getId());
        Set<VdsNetworkInterface> interfacesByLabelForNetwork = new HashSet<>();

        if (network == null) {
            getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
            return;
        }

        List<NetworkCluster> clusters = getDbFacade().getNetworkClusterDao().getAllForNetwork(network.getId());

        if (clusters.isEmpty()) {
            getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
            return;
        }

        List<VdsNetworkInterface> labeledNics = new ArrayList<>();
        for (NetworkCluster networkCluster : clusters) {
            labeledNics.addAll(getDbFacade().getInterfaceDao()
                    .getAllInterfacesByLabelForCluster(networkCluster.getClusterId(), network.getLabel()));
        }

        if (labeledNics.isEmpty()) {
            getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
            return;
        }

        List<VdsNetworkInterface> networkNics =
                getDbFacade().getInterfaceDao().getVdsInterfacesByNetworkId(network.getId());
        Map<String, VdsNetworkInterface> labeledNicsByName = Entities.entitiesByName(labeledNics);

        for (VdsNetworkInterface networkNic : networkNics) {
            if (labeledNicsByName.containsKey(NetworkCommonUtils.stripVlan(networkNic))) {
                interfacesByLabelForNetwork.add(networkNic);
            }
        }

        getQueryReturnValue().setReturnValue(interfacesByLabelForNetwork);
    }
}
