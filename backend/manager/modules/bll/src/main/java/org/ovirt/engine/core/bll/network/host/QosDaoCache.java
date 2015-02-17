package org.ovirt.engine.core.bll.network.host;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

public class QosDaoCache {

    private final HostNetworkQosDao qosDao;
    private final Map<Guid, HostNetworkQos> cache = new HashMap<>();

    public QosDaoCache(HostNetworkQosDao qosDao) {
        this.qosDao = qosDao;

    }

    public HostNetworkQos get(Guid qosId) {

        if (qosId == null) {
            return null;
        }

        if (cache.containsKey(qosId)) {
            return cache.get(qosId);
        }

        HostNetworkQos result = qosDao.get(qosId);
        cache.put(qosId, result);

        return result;
    }
}
