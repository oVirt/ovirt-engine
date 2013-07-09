package org.ovirt.engine.core.dao.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkQoS;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

import java.util.List;

public interface NetworkQoSDao extends GenericDao<NetworkQoS, Guid> {

    public List<NetworkQoS> getAllForStoragePoolId(Guid storagePoolId);
}
