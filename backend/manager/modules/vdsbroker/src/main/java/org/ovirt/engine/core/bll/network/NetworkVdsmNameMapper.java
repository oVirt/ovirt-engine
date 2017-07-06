package org.ovirt.engine.core.bll.network;

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;


@Singleton
public class NetworkVdsmNameMapper {

    @Inject
    private NetworkDao networkDao;

    public Map<String, String> createVdsmNameMapping(Guid clusterId) {
        return networkDao.getAllForCluster(clusterId).stream()
                .collect(Collectors.toMap(Network::getVdsmName, Network::getName));
    }
}

