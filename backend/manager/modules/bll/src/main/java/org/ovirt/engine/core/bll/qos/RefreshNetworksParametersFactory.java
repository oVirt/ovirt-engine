package org.ovirt.engine.core.bll.qos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PersistentHostSetupNetworksParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

/**
 * Factory creates parameters to be used to refresh out of sync networks using
 * {@link ActionType#PersistentHostSetupNetworks}.
 *
 * In contrast of {@link PersistentHostSetupNetworksParametersFactory}, which is called by this factory, this factory
 * does not work with given host ID. Parameters are created for all identified networks on all found hosts.
 */
public class RefreshNetworksParametersFactory {

    private final PersistentHostSetupNetworksParametersFactory persistentHostSetupNetworksParametersFactory;
    private final NetworkDao networkDao;
    private final VdsDao vdsDao;

    @Inject
    private RefreshNetworksParametersFactory(NetworkDao networkDao,
            VdsDao vdsDao,
            PersistentHostSetupNetworksParametersFactory persistentHostSetupNetworksParametersFactory) {
        this.persistentHostSetupNetworksParametersFactory = persistentHostSetupNetworksParametersFactory;
        this.networkDao = networkDao;
        this.vdsDao = vdsDao;
    }



    /**
     * @param qosId id of qos.
     * @return list of PersistentHostSetupNetworksParameters instances, to update all networks having given qosId,
     * on every host where they are used.
     */
    public ArrayList<ActionParametersBase> create(Guid qosId) {
        List<Network> networksHavingAlteredQos = networkDao.getAllForQos(qosId);
        return create(networksHavingAlteredQos);
    }

    /**
     * @param networks networks to be refreshed.
     * @return list of PersistentHostSetupNetworksParameters instances to update all given networks on all hosts where
     * they are used.
     */
    public ArrayList<ActionParametersBase> create(List<Network> networks) {
        Map<Guid, List<Network>> vdsIdToNetworksOfAlteredQos = mapNetworksByAttachedHosts(networks);

        ArrayList<ActionParametersBase> parameters = new ArrayList<>();
        for (Map.Entry<Guid, List<Network>> entry : vdsIdToNetworksOfAlteredQos.entrySet()) {
            Guid hostId = entry.getKey();
            List<Network> networksOfAlteredQos = entry.getValue();

            PersistentHostSetupNetworksParameters setupNetworkParams =
                    persistentHostSetupNetworksParametersFactory.create(hostId, networksOfAlteredQos);

            parameters.add(setupNetworkParams);
        }
        return parameters;
    }

    /**
     * method finds all VDS records having any of given networks and creates VdsID->NetworksIDs mapping for all such VDS
     * and Network records.
     * @param networks networks to search for
     *
     * @return mapping of VDS ID to list of network ids, where given network ids are only those of networks specified in
     * parameters.
     */
    private Map<Guid, List<Network>> mapNetworksByAttachedHosts(List<Network> networks) {
        Map<Guid, List<Network>> networksPerHostId = new HashMap<>();
        for (Network network : networks) {
            List<VDS> hostRecordsForNetwork = vdsDao.getAllForNetwork(network.getId());
            for (VDS host : hostRecordsForNetwork) {
                networksPerHostId.computeIfAbsent(host.getId(), k -> new ArrayList<>()).add(network);
            }
        }
        return networksPerHostId;
    }
}
