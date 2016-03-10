package org.ovirt.engine.core.vdsbroker;

import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.NetworkCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;

@Singleton
public class CalculateBaseNic {
    private final InterfaceDao interfaceDao;

    @Inject
    public CalculateBaseNic(InterfaceDao interfaceDao) {
        this.interfaceDao = interfaceDao;
    }

    /**
     * @param nic any nic.
     * @param interfacesByName collections of all nics. This collection is used to get base nic to avoid db
     * queries. Can be null. If this map is null or simply does not contain required nic, db will be queried.
     * @return nic passed via parameter if it's a base nic, or it's base nic if it's a vlan.
     */
    public VdsNetworkInterface getBaseNic(VdsNetworkInterface nic, Map<String, VdsNetworkInterface> interfacesByName) {
        Objects.requireNonNull(nic, "nic cannot be null");

        String baseNicName = NetworkCommonUtils.stripVlan(nic);
        if (nic.getName().equals(baseNicName)) {
            return nic;
        } else {
            return getNicByName(nic.getVdsId(), baseNicName, interfacesByName);
        }
    }

    public VdsNetworkInterface getBaseNic(VdsNetworkInterface nic) {
        return getBaseNic(nic, null);
    }

    private VdsNetworkInterface getNicByName(Guid hostId, String baseNicName, Map<String, VdsNetworkInterface> interfacesByName) {
        if (interfacesByName == null || !interfacesByName.containsKey(baseNicName)) {
            return interfaceDao.get(hostId, baseNicName);
        } else {
            return interfacesByName.get(baseNicName);
        }
    }
}
